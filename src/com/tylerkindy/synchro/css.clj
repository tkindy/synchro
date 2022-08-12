(ns com.tylerkindy.synchro.css
  [:require
   [garden.core :refer [css]]])

(def home-css
  (css [:h1 :h2 {:text-align :center}]))

(def checkbox-state-rules
  (letfn [(url [state modifier]
            (str "url(/public/" state "-" modifier ".svg)"))]
    (->> ["checked" "unchecked" "ifneedbe"]
         (map (fn [state]
                [(str "&." state)
                 [:&.active {:background-image (url state "active")}
                  [:&:hover {:background-image (url state "hover")}]]
                 [:&.inactive {:background-image (url state "inactive")}]])))))

(def plan-css
  (css [:td.date-checkbox-cell {:padding "0 20px"}
        [:&.available {:background-color "green"}]]
       [:th
        [".all-available" {:background-color "green"
                           :color "white"}]
        [:div {:padding "1px 0"}]
        [".day-of-month" {:font-size 18}]]
       [".new-person-name" {:width "125px"}]
       (into []
             (concat
              [".checkbox" {:width "14px" :height "14px"
                            :background-position "center"
                            :background-size "contain"}]
              checkbox-state-rules))))
