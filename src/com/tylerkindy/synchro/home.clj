(ns com.tylerkindy.synchro.home
  [:require
   [mount.core :refer [defstate]]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [clojure.java.io :as io]
   [com.tylerkindy.synchro.css :refer [home-css]]
   [com.tylerkindy.synchro.common :refer [viewport-tag]]])

(def date-input [:input {:name "date"
                         :type "date"
                         :disabled true}])

(def first-date-input
  (-> date-input
      (assoc-in [1 :required] "")))

(def starting-dates
  (->> date-input
       (repeat 5)
       (cons first-date-input)))

(defn weekday [day]
  (let [id (str "linear-" day)]
    [:div
     [:label {:for id} day]
     [:input {:id id, :type :checkbox, :checked true}]]))

(declare home-js)
(defstate home-js
  :start (-> "home.js"
             io/resource
             slurp))

(defn home []
  [:html
   [:head
    [:title "Synchro"]
    viewport-tag
    [:style home-css]]
   [:body
    [:h1 "Synchro"]
    [:h2 "Make plans with friends"]
    [:form.new-plan-form {:method :post}
     [:div.description-wrapper
      [:label {:for "description"} "Description "]
      [:input#description {:name "description" :required ""}]]

     [:div.dates-wrapper
      [:select.date-input-select {:autocomplete :off}
       [:option {:value "linear"} "Linear"]
       [:option {:value "manual"} "Manual"]]

      [:div.date-input-wrapper.linear.active
       [:label {:for "linear-start-date"} "Start"]
       [:input#linear-start-date {:name "start-date" :type :date}]

       [:label {:for "linear-end-date"} "End"]
       [:input#linear-end-date {:name "end-date" :type :date}]

       [:div.weekdays
        [:input.all-weekdays {:type :checkbox, :checked true}]
        (map weekday ["Monday" "Tuesday" "Wednesday" "Thursday" "Friday" "Saturday" "Sunday"])]]

      [:div.date-input-wrapper.manual
       [:button#add-manual-dates {:type "button"} "Add more dates"]
       [:div.dates starting-dates]]]

     (anti-forgery-field)

     [:button#submit {:type "submit"} "Submit"]]

    [:script home-js]]])
