(ns com.tylerkindy.synchro.main
  [:require
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [hiccup.core :refer [html]]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :refer [not-found]]
   [com.tylerkindy.synchro.home :refer [home]]
   [com.tylerkindy.synchro.plans :refer [plan-page create-plan]]
   [com.tylerkindy.synchro.css :refer [main-css]]])

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
