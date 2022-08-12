(ns com.tylerkindy.synchro.routes
  [:require
   [hiccup.page :refer [html5]]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :refer [not-found]]
   [com.tylerkindy.synchro.home :refer [home]]
   [com.tylerkindy.synchro.plans :refer [plan-page create-plan add-person]]])

(defroutes app
  (GET "/" [] {:status 200
               :headers {"Content-Type" "text/html"}
               :body (html5 (home))})
  (POST "/" req (create-plan (:params req)))
  (GET "/plans/:id" [id] (plan-page (java.util.UUID/fromString id)))
  (POST "/plans/:plan-id" req (add-person (:params req)))
  (not-found nil))
