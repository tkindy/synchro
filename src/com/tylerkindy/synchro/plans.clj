(ns com.tylerkindy.synchro.plans
  [:require
   [hiccup.core :refer [html]]
   [hiccup.util :refer [escape-html]]
   [com.tylerkindy.synchro.data :refer [plans]]
   [com.tylerkindy.synchro.css :refer [plan-css]]
   [clojure.string :as str]
   [ring.util.anti-forgery :refer [anti-forgery-field]]])

(def unknown-plan-page
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (html [:html [:body [:p "Unknown plan"]]])})

(defn create-plan [{:keys [description creator-name] :as params}]
  (let [id (random-uuid)
        dates (->> params
                   (filter (fn [[k v]] (and (str/starts-with? (name k) "date")
                                            (not (str/blank? v)))))
                   (map (fn [[_ v]] (java.time.LocalDate/parse v)))
                   sort)]
    (swap! plans assoc id {:description description
                           :creator-name creator-name
                           :dates dates
                           :people {}})
    {:status 303
     :headers {"Location" (str "/plans/" id)}}))

(defn format-date-component [component]
  (.getDisplayName component
                   java.time.format.TextStyle/SHORT
                   java.util.Locale/US))

(defn build-date-headers [dates]
  (for [date dates]
    [:th
     [:div
      [:div (-> date
                .getMonth
                format-date-component)]
      [:div.day-of-month (.getDayOfMonth date)]
      [:div (-> date
                .getDayOfWeek
                format-date-component)]]]))

(defn build-people-rows [dates people]
  (for [[name available-dates] people]
    [:tr
     [:td (escape-html name)]
     (for [date dates]
       [:td.date-checkbox-cell
        [:input
         {:type :checkbox
          :disabled ""
          :checked (and (available-dates date) "")}]])]))

(defn build-new-person-row [dates]
  [:tr
   [:td [:input {:type :text :name :person-name}]]

   (for [date dates]
     [:td.date-checkbox-cell
      [:input {:type :checkbox :name (str "date-" date)}]])

   [:td [:button "Submit"]]])

(defn found-plan-page [{:keys [description dates people]}]
  [:html
   [:head
    [:title (str (escape-html description) " | Synchro")]
    [:style plan-css]]
   [:body
    [:h1 description]
    [:form {:method :post}
     [:table
      [:thead
       [:tr
        [:th "Name"]
        (build-date-headers dates)]]
      [:tbody
       (build-people-rows dates people)
       (build-new-person-row dates)]]
     (anti-forgery-field)]]])

(defn found-plan-response [plan]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html (found-plan-page plan))})

(defn plan-page [id]
  (let [plan (@plans id)]
    (if plan
      (found-plan-response plan)
      unknown-plan-page)))

(defn add-person [{:keys [game-id person-name] :as params}]
  (let [game-id (java.util.UUID/fromString game-id)]
    (if (@plans game-id)
      (let [dates (->> params
                       (filter (fn [[k]] (str/starts-with? (name k) "date-")))
                       (map (fn [[k]] (str/replace-first (name k) "date-" "")))
                       (map (fn [date] (java.time.LocalDate/parse date)))
                       set)]
        (swap! plans assoc-in [game-id :people person-name] dates)
        {:status 303
         :headers {"Location" (str "/plans/" game-id)}})
      unknown-plan-page)))
