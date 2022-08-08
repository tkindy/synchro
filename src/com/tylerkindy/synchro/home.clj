(ns com.tylerkindy.synchro.home
  [:require
   [ring.util.anti-forgery :refer [anti-forgery-field]]])

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

     (anti-forgery-field)

     [:button "Submit"]]]])
