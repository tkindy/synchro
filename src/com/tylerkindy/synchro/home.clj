(ns com.tylerkindy.synchro.home
  [:require
   [mount.core :refer [defstate]]
   [hiccup.page :refer [html5]]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [clojure.java.io :as io]
   [com.tylerkindy.synchro.css :refer [home-css]]
   [com.tylerkindy.synchro.common :refer [viewport-tag]]
   [clojure.string :as str]])

(defn linear-date [label]
  (let [lower (str/lower-case label)
        id (str "linear-" lower "-date")]
    [:div {:class lower}
     [:label {:for id} (str label " ")]
     [:input {:id id
              :name (str lower "-date")
              :type :date
              :required true
              :disabled true}]]))

(defn weekday [day]
  (let [lower (str/lower-case day)
        id (str "linear-weekday-" lower)]
    [:tr
     [:td [:label {:for id} day]]
     [:td  [:input.weekday {:id id
                            :name "weekday"
                            :value lower
                            :type :checkbox
                            :checked true
                            :disabled true}]]]))

(def weekday-letters ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"])

(defn weekday-headers []
  [:div.weekday-headers
   (for [d weekday-letters]
     [:div d])])

(defn month-label [ym]
  (str (.getDisplayName (.getMonth ym)
                        java.time.format.TextStyle/FULL
                        java.util.Locale/US)
       " "
       (.getYear ym)))

(defn leading-pad-count [first-day]
  ;; java.time DayOfWeek values are already MON=1..SUN=7; we want MON=0..SUN=6.
  (dec (.getValue (.getDayOfWeek first-day))))

(defn date-cell [date today]
  (let [past? (.isBefore date today)
        classes (->> ["calendar-date" (when past? "past")]
                     (filter some?)
                     (str/join " "))
        attrs (cond-> {:type "button"
                       :class classes
                       :data-date (str date)
                       :aria-pressed "false"
                       :aria-label (str date)}
                past? (assoc :disabled true :tabindex -1))]
    [:button attrs (.getDayOfMonth date)]))

(defn month-grid [ym today]
  (let [first-day (.atDay ym 1)
        pad (leading-pad-count first-day)
        days (range 1 (inc (.lengthOfMonth ym)))]
    [:section.month-grid {:aria-label (month-label ym)}
     [:h3.month-label (month-label ym)]
     [:div.month-dates
      (for [_ (range pad)] [:div.empty])
      (for [day days]
        (date-cell (.atDay ym day) today))]]))

(def months-to-show 12)

(defn calendar-body []
  (let [today (java.time.LocalDate/now)
        start-ym (java.time.YearMonth/from today)
        months (map #(.plusMonths start-ym %) (range months-to-show))]
    (list
     (weekday-headers)
     [:div.months
      (for [ym months] (month-grid ym today))]
     [:div.selected-dates {:aria-hidden "true"}])))

(defstate home-js
  :start (-> "home.js"
             io/resource
             slurp))

(defn home []
  (html5
   {:lang :en}
   [:head
    [:title "Synchro"]
    [:meta {:name "description"
            :content "Make plans with friends"}]
    viewport-tag
    [:style home-css]]
   [:body
    [:h1 "Synchro"]
    [:h2 "Make plans with friends"]
    [:form.new-plan-form {:action "/plans", :method :post, :autocomplete :off}
     [:div.description-wrapper
      [:label {:for "description"} "Description"]
      [:input#description {:name "description" :required ""}]]

     [:div.email-wrapper
      [:label {:for "email"}
       "Email "
       [:span.subtext "(optional, for notifications)"]]
      [:input#email {:name "email" :type "email"}]]

     [:select.date-input-select {:name "date-input-type"
                                 :autocomplete :off}
      [:option {:value "calendar"} "Calendar"]
      [:option {:value "linear"} "Linear"]]

     [:div.dates-wrapper
      [:div.date-input-wrapper.calendar.active
       (calendar-body)]

      [:div.date-input-wrapper.linear
       (linear-date "Start")
       (linear-date "End")

       [:table.weekdays
        [:tbody
         [:tr
          [:td]
          [:td [:input.all-weekdays {:type :checkbox
                                     :checked true
                                     :disabled true
                                     :aria-label "Toggle all weekdays"}]]]
         (map weekday ["Monday" "Tuesday" "Wednesday" "Thursday" "Friday" "Saturday" "Sunday"])]]]]

     (anti-forgery-field)

     [:button#submit {:type "submit"} "Submit"]]

    [:script home-js]]))
