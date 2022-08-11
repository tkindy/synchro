(ns com.tylerkindy.synchro.css
  [:require
   [garden.core :refer [css]]])

(def home-css
  (css [:h1 :h2 {:text-align :center}]))

(def plan-css
  (css [:td.date-checkbox-cell {:padding "0 20px"}
        [:&.available {:background-color "green"}]]
       [:th
        [".all-available" {:background-color "green"}]
        [:div {:padding "1px 0"}]
        [".day-of-month" {:font-size 18}]]
       [".new-person-name" {:width "125px"}]))
