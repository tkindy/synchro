(ns com.tylerkindy.synchro.css
  [:require
   [garden.core :refer [css]]])

(def home-css
  (css [:h1 :h2 {:text-align :center}]
       [".new-plan-form" {:max-width "800px"
                          :margin :auto
                          :display :grid
                          :grid-template-columns "1fr"
                          :grid-template-rows :auto
                          :grid-template-areas "\"description\" \"dates\" \"submit\""
                          :row-gap "10px"
                          :justify-items :center}]
       [".description-wrapper" {:grid-area "description"
                                :justify-self :center}]
       [".dates-wrapper" {:grid-area "dates"}]
       [".dates" {:display :flex
                  :flex-wrap :wrap
                  :justify-content :space-evenly
                  :gap "5px"}]
       ["#submit" {:grid-area "submit"}]))

(def checkbox-urls
  (letfn [(url-entry [state modifier]
            [modifier (str "/public/" (name state) "-" (name modifier) ".svg")])]
    (->> [:checked :unchecked :ifneedbe]
         (map (fn [state] {state (into {} (list (url-entry state :active)
                                                (url-entry state :hover)))}))
         (apply merge))))

(def checkbox-state-rules
  (letfn [(url [state modifier]
            (str "url(" (get-in checkbox-urls [state modifier]) ")"))]
    (->> [{:state :checked
           :inactive-filter "grayscale(1) brightness(150%)"}
          {:state :unchecked
           :inactive-filter "grayscale(1)"}
          {:state :ifneedbe
           :inactive-filter "grayscale(1)"}]
         (map (fn [{:keys [state inactive-filter]}]
                [(str "&." (name state))
                 [:&.active {:background-image (url state :active)}
                  [:&:hover {:background-image (url state :hover)}]]
                 [:&.inactive {:background-image (url state :active)
                               :filter inactive-filter}]])))))

(def plan-css
  (css [:td
        [:&:last-child {:text-align :center}]
        [:&.date-checkbox-cell {:padding "0 20px"}
         [:&.available {:background-color "green"}]
         [:&.ifneedbe {:background-color "gold"}]]]
       [:th
        ["&.all-available" {:background-color "green"
                            :color "white"}]
        ["&.all-available-ifneedbe" {:background-color "gold"}]
        [:div {:padding "1px 0"}]
        [".day-of-month" {:font-size 18}]]
       [".new-person-name" {:width "125px"}]
       (into []
             (concat
              [".checkbox" {:width "20px" :height "20px"
                            :padding "4px 0"
                            :background-position "center"
                            :background-size "contain"
                            :background-clip "content-box"}
               [:&.inactive {:opacity "50%"}]]
              checkbox-state-rules))))
