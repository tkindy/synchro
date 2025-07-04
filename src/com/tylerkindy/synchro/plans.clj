(ns com.tylerkindy.synchro.plans
  [:require
   [mount.core :refer [defstate]]
   [hiccup.page :refer [html5]]
   [hiccup.util :refer [escape-html]]
   [com.tylerkindy.synchro.common :refer [viewport-tag]]
   [com.tylerkindy.synchro.db.core :refer [ds]]
   [com.tylerkindy.synchro.db.plans :refer [insert-plan insert-plan-dates
                                            get-plan get-plan-dates]]
   [com.tylerkindy.synchro.db.people :refer [insert-person upsert-person-dates
                                             get-people get-people-dates
                                             update-person]]
   [com.tylerkindy.synchro.css :refer [plan-css checkbox-urls]]
   [com.tylerkindy.synchro.email :refer [queue-send]]
   [com.tylerkindy.synchro.config :refer [config]]
   [com.tylerkindy.synchro.time :refer [now]]
   [clojure.string :as str]
   [clojure.java.io :as io]
   [ring.util.anti-forgery :refer [anti-forgery-field]]])

(def unknown-plan-page
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (html5 [:html {:lang :en} [:body [:p "Unknown plan"]]])})
(def unknown-person-page
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (html5 [:p "Unknown person"])})

; https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/email#basic_validation
(def email-regex #"^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$")
(defn valid-email? [email]
  (and email
       (re-matches email-regex email)
       (<= (count email) 50)))

(defn build-linear-dates [{:keys [start-date end-date]
                           weekdays :weekday}]
  (let [weekdays (if (vector? weekdays) weekdays (vector weekdays))
        start-date (java.time.LocalDate/parse start-date)
        end-date (java.time.LocalDate/parse end-date)
        weekdays (->>  weekdays
                       (map (comp #(java.time.DayOfWeek/valueOf %) str/upper-case))
                       set)]
    (when (> (compare start-date end-date) 0)
      (throw (RuntimeException. "Start date after end date")))

    (->> (.datesUntil start-date (.plusDays end-date 1))
         .iterator
         iterator-seq
         (filter #(weekdays (.getDayOfWeek %))))))

(defn build-manual-dates [{dates :date}]
  (->> dates
       (filter (comp not str/blank?))
       (map #(java.time.LocalDate/parse %))))

(defn build-dates [{:keys [date-input-type] :as params}]
  (case date-input-type
    "linear" (build-linear-dates params)
    "manual" (build-manual-dates params)))

(def max-dates-per-plan 30)

(defn create-plan [{:keys [description email] :as params}]
  (let [id (random-uuid)
        description (escape-html description)
        dates (->> (build-dates params)
                   (take (inc max-dates-per-plan)))
        email (if (= email "") nil email)]
    (cond
      (not (or (nil? email)
               (valid-email? email)))
      {:status 400
       :headers {"Content-Type" "text/html"}
       :body (html5 [:body [:p "Invalid email " (escape-html email)]])}

      (> (count dates) max-dates-per-plan)
      {:status 400
       :headers {"Content-Type" "text/html"}
       :body (html5
              [:body
               [:p
                (str "Plans can only have up to " max-dates-per-plan " dates")]])}

      :else (do
              (insert-plan ds {:id id, :description description,
                               :email email, :created-at (now)})
              (insert-plan-dates ds {:dates (map (fn [date] [id date]) dates)})
              {:status 303
               :headers {"Location" (str "/plans/" id)}}))))

(defn format-date-component [component]
  (.getDisplayName component
                   java.time.format.TextStyle/SHORT
                   java.util.Locale/US))

(defn build-date-headers [dates people]
  (let [all-answers (map :dates people)]
    (for [date dates]
      (let [answers (->> all-answers
                         (map #(get % date))
                         set)
            class (condp = answers
                    #{:available}           "all-available"
                    #{:available :ifneedbe} "all-available-ifneedbe"
                    #{:ifneedbe}            "all-available-ifneedbe"
                    nil)]
        [:th {:class class, :aria-label date}
         [:div (-> date
                   .getMonth
                   format-date-component)]
         [:div.day-of-month (.getDayOfMonth date)]
         [:div (-> date
                   .getDayOfWeek
                   format-date-component)]]))))

(defn available-control [{:keys [state date]}]
  (let [[state-class, state-label] (case state
                                     :available   ["checked"   "Available"]
                                     :unavailable ["unchecked" "Unavailable"]
                                     :ifneedbe    ["ifneedbe"  "If need be"])
        modifier-class (if date "active" "inactive")
        class (str/join " " ["checkbox" state-class modifier-class])]
    (list
     [:button {:type :button, :class class, :aria-label state-label}]
     (when date
       [:input {:type :hidden
                :name (str "date-" date)
                :value (name state)}]))))

(defn build-editable-row [dates existing-person]
  [:tr
   [:td [:input.new-person-name {:type :text
                                 :name :person-name
                                 :required ""
                                 :maxlength 16
                                 :value (:name existing-person)}]]

   (for [date dates]
     [:td.date-checkbox-cell
      (available-control {:state (get (:dates existing-person) date :unavailable)
                          :date date})])

   [:td [:button "Submit"]]])

(defn build-people-rows [{:keys [id dates people]} editing-person]
  (for [{person-id :id
         person-name :name
         availabilities :dates} people]
    (if (= person-id (:id editing-person))
      (build-editable-row dates editing-person)
      [:tr
       [:td person-name]
       (for [date dates]
         (let [availability (availabilities date)
               classes (->> (list "date-checkbox-cell"
                                  (when (#{:available :ifneedbe} availability)
                                    (name availability)))
                            (filter some?)
                            (str/join " "))]
           [:td
            {:class classes}
            (available-control {:state availability})]))

       (when (not editing-person)
         [:td [:a {:href (str "/plans/" id "/edit/" person-id)} "Edit"]])])))

(def preloads
  (->> checkbox-urls
       vals
       (mapcat vals)
       (map (fn [url]
              [:link {:rel :preload
                      :href url
                      :as :image}]))))

(defstate js
  :start (-> "plan.js"
             io/resource
             slurp))

(defn found-plan-page [{:keys [description dates people] :as plan}
                       editing-person]
  (html5
   {:lang :en}
   [:head
    [:title (str description " | Synchro")]
    viewport-tag
    [:style plan-css]
    preloads]
   [:body
    [:h1 description]
    [:p [:i "Click a checkbox once for 'yes', twice for 'if need be', three times to reset."]]
    [:form {:method :post}
     [:table.availability {:aria-label "Availability"}
      [:thead
       [:tr
        [:th "Name"]
        (build-date-headers dates people)]]
      [:tbody
       (build-people-rows plan editing-person)
       (when (not editing-person)
         (build-editable-row dates nil))]]
     (anti-forgery-field)]
    [:p.cta [:a {:href "/"} "Make your own poll"]]
    [:script js]]))

(defn found-plan-response [plan]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (found-plan-page plan nil)})

(defn find-plan [id]
  (let [plan-info (get-plan ds {:id id})]
    (when plan-info
      (let [dates-info (get-plan-dates ds {:id id})
            people-info (get-people ds {:plan-id id})
            people-dates-info (get-people-dates ds {:plan-id id})
            dates-by-person (->> people-dates-info
                                 (map (fn [{:keys [person-id date state]}]
                                        {person-id {(.toLocalDate date) (keyword state)}}))
                                 (apply merge-with into))]
        (assoc plan-info
               :dates (map (comp (fn [d] (.toLocalDate d)) :date)
                           dates-info)
               :people (->> people-info
                            (map (fn [{:keys [id] :as person}]
                                   (assoc person :dates (dates-by-person id))))))))))

(defn plan-page [id]
  (let [plan (find-plan id)]
    (if plan
      (found-plan-response plan)
      unknown-plan-page)))

(def availabilities #{"available" "unavailable" "ifneedbe"})

(defn parse-availability [a]
  (or (availabilities a) "unavailable"))

(defn build-person-dates-tuples [person-id params]
  (->> params
       (filter (fn [[k]] (str/starts-with? (name k) "date-")))
       (map (fn [[k v]] [person-id
                         (-> k
                             name
                             (str/replace-first "date-" "")
                             java.time.LocalDate/parse)
                         (parse-availability v)]))))

(defn upsert-availabilities [person-id params]
  (upsert-person-dates ds
                       {:people-dates (build-person-dates-tuples person-id
                                                                 params)}))

(defn send-notification [plan person-name]
  (let [{plan-id :id, :keys [email description]} plan]
    (when email
      (queue-send (:email @config)
                  {:to email
                   :subject (str "New submission on '" description "'")
                   :message (str person-name
                                 " submitted their availability on '"
                                 description
                                 "'.\n"
                                 "https://synchro.tylerkindy.com/plans/"
                                 plan-id)}))))

(defn redirect-to-plan [plan-id]
  {:status 303
   :headers {"Location" (str "/plans/" plan-id)}})

(defn add-person [{:keys [plan-id person-name] :as params}]
  (let [plan-id (java.util.UUID/fromString plan-id)]
    (if-let [plan (get-plan ds {:id plan-id})]
      (let [person-name (escape-html person-name)
            person-id (-> (insert-person ds {:plan-id plan-id, :name person-name})
                          :id)]
        (upsert-availabilities person-id params)
        (send-notification plan person-name)
        (redirect-to-plan plan-id))
      unknown-plan-page)))

(defn get-person [plan person-id]
  (->> (:people plan)
       (filter #(= (:id %) person-id))
       first))

(defn found-edit-page-response [plan person]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (found-plan-page plan person)})

(defn edit-page [{:keys [plan-id person-id]}]
  (let [plan-id (parse-uuid plan-id)
        person-id (Integer/parseInt person-id)
        plan (find-plan plan-id)
        person (get-person plan person-id)]
    (cond
      (not plan) unknown-plan-page
      (not person) unknown-person-page
      :else (found-edit-page-response plan person))))

(defn edit-submission [{:keys [plan-id person-id person-name] :as params}]
  (let [plan-id (parse-uuid plan-id)
        person-id (Integer/parseInt person-id)
        plan (find-plan plan-id)
        person (get-person plan person-id)]
    (cond
      (not plan) unknown-plan-page
      (not person) unknown-person-page
      :else (let [person-name (escape-html person-name)]
              (update-person ds {:id person-id, :name person-name})
              (upsert-availabilities person-id params)
              (redirect-to-plan plan-id)))))
