(ns com.tylerkindy.synchro.main
  [:require
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [hiccup.core :refer [html]]
   [garden.core :refer [css]]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :refer [not-found]]
   [com.tylerkindy.synchro.home :refer [home]]])

(def plans (atom {}))

(def main-css
  (css [:body {:margin "auto"
               :max-width "375px"}]
       [:h1 :h2 {:text-align :center}]
       [".new-plan-form" {:display :grid
                          :grid-template-rows "25px 25px 25px"
                          :grid-template-columns "auto auto"
                          :gap "10px"
                          :align-items :baseline}
        [:label {:text-align :end}]
        [:button {:grid-column "1 / span 2"}]]))

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

(defroutes app
  (GET "/" [] {:status 200
               :headers {"Content-Type" "text/html"}
               :body (html (home))})
  (GET "/main.css" [] {:status 200
                       :headers {"Content-Type" "text/css"}
                       :body main-css})
  (POST "/" req (create-plan (:params req)))
  (GET "/plans/:id" [id] (plan-page (java.util.UUID/fromString id)))
  (not-found nil))

(defonce server (atom nil))

(defn restart!
  ([] (restart! false))
  ([join?]
   (when @server (.stop @server))
   (reset! server (run-jetty (wrap-defaults app site-defaults)
                             {:port 3000
                              :join? join?}))))

(defn -main []
  (restart! true))
