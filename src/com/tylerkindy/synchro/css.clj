(ns com.tylerkindy.synchro.css
  [:require
   [garden.core :refer [css]]])

(def home-css
  (css [:h1 :h2 {:text-align :center}]))

(def plan-css
  (css [:td.date-checkbox-cell {:padding "0 30px"}]))
