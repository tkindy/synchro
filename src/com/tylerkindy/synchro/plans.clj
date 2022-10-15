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
                                             get-people get-people-dates]]
   [com.tylerkindy.synchro.css :refer [plan-css checkbox-urls]]
   [clojure.string :as str]
   [clojure.java.io :as io]
   [ring.util.anti-forgery :refer [anti-forgery-field]]])

(def unknown-plan-page
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (html5 [:html [:body [:p "Unknown plan"]]])})

(defn create-plan [{:keys [description] :as params}]
  (let [id (random-uuid)
        dates (->> params
                   (filter (fn [[k v]] (and (str/starts-with? (name k) "date")
                                            (not (str/blank? v)))))
                   (map (fn [[_ v]] (java.time.LocalDate/parse v))))]
    (insert-plan ds {:id id, :description description})
    (insert-plan-dates ds {:dates (map (fn [date] [id date]) dates)})
    {:status 303
     :headers {"Location" (str "/plans/" id)}}))

(defn format-date-component [component]
  (.getDisplayName component
                   java.time.format.TextStyle/SHORT
                   java.util.Locale/US))

(defn answers [date people]
  (->> people
       (map :dates)
       (map #(get % date))
       set))

(defn build-date-headers [dates people]
  (for [date dates]
    (let [class (condp = (answers date people)
                  #{:available}           "all-available"
                  #{:available :ifneedbe} "all-available-ifneedbe"
                  #{:ifneedbe}            "all-available-ifneedbe"
                  nil)]
      [:th
       [:div {:class class}
        [:div (-> date
                  .getMonth
                  format-date-component)]
        [:div.day-of-month (.getDayOfMonth date)]
        [:div (-> date
                  .getDayOfWeek
                  format-date-component)]]])))

(defn available-control [{:keys [state date]}]
  (let [state-class (case state
                      :available   "checked"
                      :unavailable "unchecked"
                      :ifneedbe    "ifneedbe")
        modifier-class (if date "active" "inactive")
        class (str/join " " ["checkbox" state-class modifier-class])]
    (list
     [:div {:class class}]
     (when date
       [:input {:type :hidden
                :name (str "date-" date)
                :value (name state)}]))))

(defn build-new-person-row [dates existing-person]
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

(defn build-people-rows [dates people editing-person]
  (for [{id :id
         person-name :name
         availabilities :dates} people]
    (if (= id (:id editing-person))
      (build-new-person-row dates editing-person)
      [:tr
       [:td (escape-html person-name)]
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

       [:td [:a {:href "edit"} "Edit"]]])))

(def preloads
  (->> checkbox-urls
       vals
       (mapcat vals)
       (map (fn [url]
              [:link {:rel :preload
                      :href url
                      :as :image}]))))

(declare js)
(defstate js
  :start (-> "plan.js"
             io/resource
             slurp))

(defn found-plan-page [{:keys [description dates people]}
                       editing-person]
  [:html
   [:head
    [:title (str (escape-html description) " | Synchro")]
    viewport-tag
    [:style plan-css]
    preloads]
   [:body
    [:h1 description]
    [:p [:i "Click a checkbox once for 'yes', twice for 'if need be', three times to reset."]]
    [:form {:method :post}
     [:table
      [:thead
       [:tr
        [:th "Name"]
        (build-date-headers dates people)]]
      [:tbody
       (build-people-rows dates people editing-person)
       (when (not editing-person)
         (build-new-person-row dates nil))]]
     (anti-forgery-field)]
    [:script js]]])

(defn found-plan-response [plan]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html5 (found-plan-page plan nil))})

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

(defn build-person-dates-tuples [person-id params]
  (->> params
       (filter (fn [[k]] (str/starts-with? (name k) "date-")))
       (map (fn [[k v]] [person-id
                         (-> k
                             name
                             (str/replace-first "date-" "")
                             java.time.LocalDate/parse)
                         v]))))

(defn add-person [{:keys [plan-id person-name] :as params}]
  (let [plan-id (java.util.UUID/fromString plan-id)]
    (if (get-plan ds {:id plan-id})
      (let [person-id (-> (insert-person ds {:plan-id plan-id, :name person-name})
                          :id)
            dates (build-person-dates-tuples person-id params)]
        (upsert-person-dates ds {:people-dates dates})
        {:status 303
         :headers {"Location" (str "/plans/" plan-id)}})
      unknown-plan-page)))

(defn get-person [plan person-id]
  (->> (:people plan)
       (filter #(= (:id %) person-id))
       first))

(defn found-edit-page [plan person]
  [:p "Edit page"])

(defn found-edit-response [plan person]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html5 (found-edit-page plan person))})

(defn edit-page [{:keys [plan-id person-id]}]
  (let [plan-id (parse-uuid plan-id)
        person-id (Integer/parseInt person-id)
        plan (find-plan plan-id)
        person (get-person plan person-id)]
    (cond
      (not plan) unknown-plan-page
      (not person) {:status 404
                    :headers {"Content-Type" "text/html"}
                    :body (html5 [:p "Unknown person"])}
      :else (found-edit-response plan person))))
