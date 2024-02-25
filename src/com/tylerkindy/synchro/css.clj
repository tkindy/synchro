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
                          :grid-template-areas "\"description\" \"email\" \"date-input-type\" \"dates\" \"submit\""
                          :row-gap "10px"
                          :justify-items :center}]
       [".description-wrapper label, .email-wrapper label" {:display :block}]
       [".description-wrapper input, .email-wrapper input" {:min-width "185px"}]
       [".description-wrapper" {:grid-area "description"
                                :justify-self :center}]
       [".email-wrapper" {:grid-area "email"
                          :justify-self :center}
        [".subtext" {:font-size "0.75rem"}]]
       [".date-input-select" {:grid-area "date-input-type"}]
       [".dates-wrapper" {:grid-area "dates"}
        [".date-input-wrapper:not(.active)" {:display :none}]
        [".linear" {:display :grid
                    :grid-template-columns "1fr"
                    :grid-template-rows :auto
                    :grid-template-areas "\"start-date\" \"end-date\" \"weekdays\""
                    :row-gap "10px"}
         [".start" {:grid-area "start-date"}]
         [".end" {:grid-area "end-date"}]
         [".weekdays" {:grid-area "weekdays"}
          ["tr :nth-child(1)" {:text-align :right}]
          ["td" {:width "50%"}]]]
        [".manual" {:display :grid
                    :grid-template-columns "1fr"
                    :grid-template-rows :auto
                    :grid-template-areas "\"add-dates\" \"dates\""
                    :row-gap "10px"
                    :justify-items :center}
         [".dates" {:grid-area "dates"
                    :display :flex
                    :flex-wrap :wrap
                    :justify-content :space-evenly
                    :gap "5px"}]]]
       ["#add-manual-dates" {:grid-area "add-dates"}]
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
        [:&.date-checkbox-cell {:height "28px", :padding "0 20px"}
         [:&.available {:background-color "green"}]
         [:&.ifneedbe {:background-color "gold"}]]]
       [:th
        ["&.all-available" {:background-color "green"
                            :color "white"}]
        ["&.all-available-ifneedbe" {:background-color "gold"}]
        [:div {:padding "1px 0"}]
        [".day-of-month" {:font-size 18}]]
       [".new-person-name" {:width "125px"}]
       [".cta" {:font-size "12px"
                :font-style :italic}]
       (into []
             (concat
              [".checkbox" {:width "20px" :height "20px"
                            :border 0 :padding 0
                            :vertical-align :middle
                            :background-position "center"
                            :background-size "contain"
                            :background-clip "content-box"
                            :background-color :transparent}
               [:&.inactive {:opacity "50%"}]]
              checkbox-state-rules))))
