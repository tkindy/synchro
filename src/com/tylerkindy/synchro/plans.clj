(ns com.tylerkindy.synchro.plans
  [:require
   [hiccup.core :refer [html]]
   [com.tylerkindy.synchro.data :refer [plans]]])

(defn create-plan [{:keys [description creator-name]}]
  (let [id (random-uuid)]
    (swap! plans assoc id {:description description
                           :creator-name creator-name})
    {:status 303
     :headers {"Location" (str "/plans/" id)}}))

(defn found-plan-page [{:keys [description creator-name]}]
  [:html
   [:body
    [:h1 description]
    [:table
     [:thead
      [:tr
       [:th "Name"]
       [:th "August 8"]]]
     [:tbody
      [:tr
       [:td creator-name]
       [:td "yes"]]]]]])

(defn plan-page [id]
  (let [plan (@plans id)
        response (if plan
                   {:status 200
                    :body (html (found-plan-page plan))}
                   {:status 404
                    :body (html [:html [:body [:p "Unknown plan"]]])})]
    (assoc response :headers {"Content-Type" "text/html"})))
