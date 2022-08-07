(ns com.tylerkindy.synchro.main
  [:require
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [hiccup.core :refer [html]]
   [garden.core :refer [css]]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :refer [not-found]]
   [com.tylerkindy.synchro.home :refer [home]]
   [com.tylerkindy.synchro.plans :refer [plan-page create-plan]]])

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
