(ns com.tylerkindy.synchro.css
  [:require
   [garden.core :refer [css]]])

(def main-css
  (css [:h1 :h2 {:text-align :center}]))
