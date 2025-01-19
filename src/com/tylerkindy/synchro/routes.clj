(ns com.tylerkindy.synchro.routes
  [:require
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :refer [not-found resources]]
   [com.tylerkindy.synchro.home :refer [home]]
   [com.tylerkindy.synchro.plans :refer [plan-page create-plan add-person
                                         edit-page edit-submission]]])

(defroutes app
  (GET "/" [] {:status 200
               :headers {"Content-Type" "text/html"}
               :body (home)})
  (POST "/plans" req (create-plan (:params req)))
  (GET "/plans/:id" [id] (plan-page (java.util.UUID/fromString id)))
  (POST "/plans/:plan-id" req (add-person (:params req)))
  (GET "/plans/:plan-id/edit/:person-id" req (edit-page (:params req)))
  (POST "/plans/:plan-id/edit/:person-id" req (edit-submission (:params req)))
  (resources "/public")
  (not-found nil))
