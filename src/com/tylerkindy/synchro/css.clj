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
  (letfn [(url [state modifier]
            (str "url(" (get-in checkbox-urls [state modifier]) ")"))]
    (->> [:checked :unchecked :ifneedbe]
         (map (fn [state]
                [(str "&." (name state))
                 [:&.active {:background-image (url state :active)}
                  [:&:hover {:background-image (url state :hover)}]]
                 [:&.inactive {:background-image (url state :inactive)}]])))))

(def plan-css
  (css [:td.date-checkbox-cell {:padding "0 20px"}
        [:&.available {:background-color "green"}]
        [:&.ifneedbe {:background-color "gold"}]]
       [:th
        [".all-available" {:background-color "green"
                           :color "white"}]
        [".all-available-ifneedbe" {:background-color "gold"}]
        [:div {:padding "1px 0"}]
        [".day-of-month" {:font-size 18}]]
       [".new-person-name" {:width "125px"}]
       (into []
             (concat
              [".checkbox" {:width "14px" :height "14px"
                            :background-position "center"
                            :background-size "contain"}]
              checkbox-state-rules))))
