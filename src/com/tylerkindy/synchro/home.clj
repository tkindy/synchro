(ns com.tylerkindy.synchro.home
  [:require
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [clojure.java.io :as io]
   [com.tylerkindy.synchro.css :refer [home-css]]])

(def starting-dates
  (for [i (range 5)]
    [:input {:name (str "date-" i)
             :type "date"}]))

(def home-js (-> "home.js"
                 io/resource
                 slurp))

(defn home []
  [:html
   [:head
    [:title "Synchro"]
    [:style home-css]]
   [:body
    [:h1 "Synchro"]
    [:h2 "Make plans with friends"]
    [:form {:class "new-plan-form" :method :post}
     [:label {:for "description"} "Description"]
     [:input {:id "description" :name "description"}]

     [:div {:class "dates"} starting-dates]
     [:button#add-dates {:type "button"} "Add more dates"]

     (anti-forgery-field)

     [:button {:type "submit"} "Submit"]]

    [:script home-js]]])
