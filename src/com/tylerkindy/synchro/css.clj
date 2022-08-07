(ns com.tylerkindy.synchro.css
  [:require
   [garden.core :refer [css]]])

(def main-css
  (css [:body {:margin "auto"
               :max-width "375px"}]
       [:h1 :h2 {:text-align :center}]
       [".new-plan-form" {:display :grid
                          :grid-template-rows "25px 25px 25px"
                          :grid-template-columns "auto auto"
                          :gap "10px"
                          :align-items :baseline}
        [:label {:text-align :end}]
        [:button {:grid-column "1 / span 2"}]]))
