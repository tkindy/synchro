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

(defn plan-page [id]
  (let [plan (@plans id)
        response (if plan
                   (let [{:keys [description creator-name]} plan]
                     {:status 200
                      :body (html [:html [:body [:p (str description "," creator-name)]]])})
                   {:status 404
                    :body (html [:html [:body [:p "Unknown plan"]]])})]
    (assoc response :headers {"Content-Type" "text/html"})))
