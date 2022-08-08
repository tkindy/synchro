(ns com.tylerkindy.synchro.home
  [:require
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [clojure.java.io :as io]])

(def starting-dates
  (map (fn [i]
         (let [name (str "date" i)]
           [:input {:id name
                    :name name
                    :type "date"}]))
       (range 5)))

(def home-js (-> "home.js"
                 io/resource
                 slurp))

(defn home []
  [:html
   [:head
    [:title "Synchro"]
    [:link {:rel :stylesheet :href "/main.css"}]]
   [:body
    [:h1 "Synchro"]
    [:h2 "Make plans with friends"]
    [:form {:class "new-plan-form" :method :post}
     [:label {:for "description"} "Description"]
     [:input {:id "description" :name "description"}]

     [:label {:for "creator-name"} "Your name"]
     [:input {:id "creator-name" :name "creator-name"}]

     (->
      [:div {:class "dates"}]
      (concat starting-dates)
      vec)

     (anti-forgery-field)

     [:button "Submit"]]

    [:script home-js]]])
