(ns com.tylerkindy.synchro.plans
  [:require
   [hiccup.page :refer [html5]]
   [hiccup.util :refer [escape-html]]
   [com.tylerkindy.synchro.data :refer [plans]]
   [com.tylerkindy.synchro.db.core :refer [ds]]
   [com.tylerkindy.synchro.db.plans :refer [insert-plan insert-dates
                                            get-plan get-plan-dates]]
   [com.tylerkindy.synchro.db.people :refer [get-people get-people-dates]]
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
                   (map (fn [[_ v]] (java.time.LocalDate/parse v)))
                   sort)]
    (insert-plan ds {:id id, :description description})
    (insert-dates ds {:dates (map (fn [date] [id date]) dates)})
    {:status 303
     :headers {"Location" (str "/plans/" id)}}))

(defn format-date-component [component]
  (.getDisplayName component
                   java.time.format.TextStyle/SHORT
                   java.util.Locale/US))

(defn answers [date people]
  (->> people
       vals
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

(def toggle-available (-> "toggle-available.js"
                          io/resource
                          slurp))

(defn available-control [{:keys [state date]}]
  (let [state-class (case state
                      :available   "checked"
                      :unavailable "unchecked"
                      :ifneedbe    "ifneedbe")
        modifier-class (if date "active" "inactive")
        class (str/join " " ["checkbox" state-class modifier-class])]
    (list
     [:div {:class class
            :onclick (when date toggle-available)}]
     (when date
       [:input {:type :hidden
                :name (str "date-" date)
                :value (name state)}]))))

(defn build-people-rows [dates people]
  (for [[person-name availabilities] people]
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
          (available-control {:state availability})]))]))

(defn build-new-person-row [dates]
  [:tr
   [:td [:input.new-person-name {:type :text
                                 :name :person-name
                                 :required ""
                                 :maxlength 16}]]

   (for [date dates]
     [:td.date-checkbox-cell
      (available-control {:state :unavailable
                          :date date})])

   [:td [:button "Submit"]]])

(def preloads
  (->> checkbox-urls
       vals
       (mapcat vals)
       (map (fn [url]
              [:link {:rel :preload
                      :href url
                      :as :image}]))))

(defn found-plan-page [{:keys [description dates people]}]
  [:html
   [:head
    [:title (str (escape-html description) " | Synchro")]
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
       (build-people-rows dates people)
       (build-new-person-row dates)]]
     (anti-forgery-field)]]])

(defn found-plan-response [plan]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html5 (found-plan-page plan))})

(defn associate-by [f coll]
  (zipmap (map f coll) coll))

(defn find-plan [id]
  (let [plan-info (get-plan ds {:id id})]
    (when plan-info
      (let [dates-info (get-plan-dates ds {:id id})
            people-info (get-people ds {:plan-id id})
            people-dates-info (get-people-dates ds {:plan-id id})
            dates-by-person (associate-by :person-id people-dates-info)]
        (assoc plan-info
               :dates (map (comp (fn [d] (.toLocalDate d)) :date)
                           dates-info)
               :people (->> people-info
                            (map (fn [{:keys [id name]}] {name (dates-by-person id)}))
                            (apply merge)))))))

(defn plan-page [id]
  (let [plan (find-plan id)]
    (if plan
      (found-plan-response plan)
      unknown-plan-page)))

(defn add-person [{:keys [plan-id person-name] :as params}]
  (let [plan-id (java.util.UUID/fromString plan-id)]
    (if (@plans plan-id)
      (let [dates (->> params
                       (filter (fn [[k]] (str/starts-with? (name k) "date-")))
                       (map (fn [[k v]] [(-> k
                                             name
                                             (str/replace-first "date-" "")
                                             java.time.LocalDate/parse)
                                         (keyword v)]))
                       (into {}))]
        (swap! plans assoc-in [plan-id :people person-name] dates)
        {:status 303
         :headers {"Location" (str "/plans/" plan-id)}})
      unknown-plan-page)))
