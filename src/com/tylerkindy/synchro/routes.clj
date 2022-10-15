(ns com.tylerkindy.synchro.routes
  [:require
   [hiccup.page :refer [html5]]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :refer [not-found resources]]
   [com.tylerkindy.synchro.home :refer [home]]
   [com.tylerkindy.synchro.plans :refer [plan-page create-plan add-person
                                         edit-page]]])

(defroutes app
  (GET "/" [] {:status 200
               :headers {"Content-Type" "text/html"}
               :body (html5 (home))})
  (POST "/" req (create-plan (:params req)))
  (GET "/plans/:id" [id] (plan-page (java.util.UUID/fromString id)))
  (POST "/plans/:plan-id" req (add-person (:params req)))
  (GET "/plans/:plan-id/edit/:person-id" req (edit-page (:params req)))
  (resources "/public")
  (not-found nil))
