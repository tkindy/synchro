(ns com.tylerkindy.synchro.css
  [:require
   [garden.core :refer [css]]])

(def home-css
  (css [:h1 :h2 {:text-align :center}]))

(def checkbox-urls
  (letfn [(url-entry [state modifier]
            [modifier (str "/public/" (name state) "-" (name modifier) ".svg")])]
    (->> [:checked :unchecked :ifneedbe]
         (map (fn [state] {state (into {} (list (url-entry state :active)
                                                (url-entry state :hover)
                                                (url-entry state :inactive)))}))
         (apply merge))))

(def checkbox-state-rules
  (->> [:checked :unchecked :ifneedbe]
       (map (fn [state]
              [(str "&." (name state))
               [:&.active {:background-image (get-in checkbox-urls [state :active])}
                [:&:hover {:background-image (get-in checkbox-urls [state :hover])}]]
               [:&.inactive {:background-image (get-in checkbox-urls [state :inactive])}]]))))

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
