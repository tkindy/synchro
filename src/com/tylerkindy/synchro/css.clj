(ns com.tylerkindy.synchro.css
  [:require
   [garden.core :refer [css]]
   [garden.stylesheet :refer [at-font-face]]])

(def base
  (list (at-font-face {:font-family "Domine"
                       :src "url(\"/public/domine.ttf\") format(\"truetype\")"})
        [:body :input :button {:font-family "Domine"}]))

(def home-css
  (css base
       [:h1 :h2 {:text-align :center}]
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
       [".dates-wrapper" {:grid-area "dates"
                          :width "100%"
                          :max-width "360px"
                          :margin "0 auto"}
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
        [".calendar" {:border "1px solid #ccc"
                      :border-radius "6px"
                      :max-height "60vh"
                      :overflow-y :auto
                      :background "white"}
         [".weekday-headers" {:display :grid
                              :grid-template-columns "repeat(7, 1fr)"
                              :position :sticky
                              :top 0
                              :background "white"
                              :z-index 1
                              :border-bottom "1px solid #ccc"
                              :text-align :center
                              :font-weight :bold
                              :font-size "0.85rem"
                              :padding "8px 0"}]
         [".month-grid" {:padding "0 6px"}
          [".month-label" {:text-align :center
                           :font-size "1rem"
                           :font-weight :bold
                           :margin "12px 0 6px 0"}]
          [".month-dates" {:display :grid
                           :grid-template-columns "repeat(7, 1fr)"
                           :gap "2px"}
           [".empty" {:aspect-ratio "1 / 1"}]
           [".calendar-date" {:aspect-ratio "1 / 1"
                              :display :flex
                              :align-items :center
                              :justify-content :center
                              :background "white"
                              :border "1px solid transparent"
                              :border-radius "50%"
                              :cursor :pointer
                              :font-family "Domine"
                              :font-size "0.9rem"
                              :padding 0}
            ["&:hover:not(:disabled)" {:background "#e6f0ff"}]
            [:&.selected {:background "#2e7d32"
                          :color "white"
                          :border-color "#1b5e20"}]
            [:&.selected:hover {:background "#256528"}]
            [:&:disabled {:color "#bbb"
                          :cursor :not-allowed}]
            ["&.past:not(.selected)" {:color "#bbb"}]]]]
         [".selected-dates" {:display :none}]]]
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
  (css base
       [:table.availability {:display :block
                             :overflow-x :auto
                             :padding "4px 0"}]
       [:td
        [:&:last-child {:text-align :center}]
        [:&.person-name {:font-size "1rem"}]
        [:&.date-checkbox-cell {:height "28px", :padding "0 20px"}
         [:&.available {:background-color "green"}]
         [:&.ifneedbe {:background-color "gold"}]]]
       [:th
        ["&.all-available" {:background-color "green"
                            :color "white"}]
        ["&.all-available-ifneedbe" {:background-color "gold"}]
        [:div {:padding "1px 0"}]
        [".day-of-month" {:font-size "1.25rem"}]]
       [".new-person-name" {:width "125px", :font-size "1rem"}]
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
