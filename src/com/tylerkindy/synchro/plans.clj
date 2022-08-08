(ns com.tylerkindy.synchro.plans
  [:require
   [hiccup.core :refer [html]]
   [com.tylerkindy.synchro.data :refer [plans]]])

(defn create-plan [{:keys [description creator-name dates]}]
  (let [id (random-uuid)]
    (swap! plans assoc id {:description description
                           :creator-name creator-name
                           :dates dates})
    {:status 303
     :headers {"Location" (str "/plans/" id)}}))

(defn found-plan-page [{:keys [description creator-name dates]}]
  (let [date-headers (map (fn [date] [:th date])
                          dates)
        date-cells (map (fn [date] [:td "yes"])
                        dates)]
    [:html
     [:head
      [:title (str description " | Synchro")]]
     [:body
      [:h1 description]
      [:table
       [:thead
        (->
         (concat
          [:tr
           [:th "Name"]]
          date-headers)
         vec)]
       [:tbody
        (->
         (concat
          [:tr
           [:td creator-name]]
          date-cells)
         vec)]]]]))

(defn plan-page [id]
  (let [plan (@plans id)
        response (if plan
                   {:status 200
                    :body (html (found-plan-page plan))}
                   {:status 404
                    :body (html [:html [:body [:p "Unknown plan"]]])})]
    (assoc response :headers {"Content-Type" "text/html"})))
