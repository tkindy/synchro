(ns com.tylerkindy.synchro.home
  [:require
   [mount.core :refer [defstate]]
   [hiccup.page :refer [html5]]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [clojure.java.io :as io]
   [com.tylerkindy.synchro.css :refer [home-css]]
   [com.tylerkindy.synchro.common :refer [viewport-tag]]
   [clojure.string :as str]])

(def date-input [:input {:name "date"
                         :type "date"
                         :disabled true}])

(def first-date-input
  (-> date-input
      (assoc-in [1 :required] "")))

(def starting-dates
  (->> date-input
       (repeat 5)
       (cons first-date-input)))

(defn linear-date [label]
  (let [lower (str/lower-case label)
        id (str "linear-" lower "-date")]
    [:div {:class lower}
     [:label {:for id} (str label " ")]
     [:input {:id id
              :name (str lower "-date")
              :type :date
              :required true}]]))

(defn weekday [day]
  (let [lower (str/lower-case day)
        id (str "linear-weekday-" lower)]
    [:tr
     [:td [:label {:for id} day]]
     [:td  [:input.weekday {:id id
                            :name "weekday"
                            :value lower
                            :type :checkbox
                            :checked true}]]]))

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

     #_[:div.email-wrapper
        [:label {:for "email"}
         "Email "
         [:span.subtext "(optional, for notifications)"]]
        [:input#email {:name "email" :type "email"}]]

     [:select.date-input-select {:name "date-input-type"
                                 :autocomplete :off}
      [:option {:value "linear"} "Linear"]
      [:option {:value "manual"} "Manual"]]

     [:div.dates-wrapper
      [:div.date-input-wrapper.linear.active
       (linear-date "Start")
       (linear-date "End")

       [:table.weekdays
        [:tbody
         [:tr
          [:td]
          [:td [:input.all-weekdays {:type :checkbox
                                     :checked true
                                     :aria-label "Toggle all weekdays"}]]]
         (map weekday ["Monday" "Tuesday" "Wednesday" "Thursday" "Friday" "Saturday" "Sunday"])]]]

      [:div.date-input-wrapper.manual
       [:button#add-manual-dates {:type "button"} "Add more dates"]
       [:div.dates starting-dates]]]

     (anti-forgery-field)

     [:button#submit {:type "submit"} "Submit"]]

    [:script home-js]]))
