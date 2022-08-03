(ns com.tylerkindy.synchro.main
  [:require
   [ring.adapter.jetty :refer [run-jetty]]
   [hiccup.core :refer [html]]
   [compojure.core :refer [defroutes GET]]
   [compojure.route :refer [not-found]]])

(def home
  [:html [:body [:h1 "Home"]]])

(defroutes app
  (GET "/" [] {:status 200
               :headers {"Content-Type" "text/html"}
               :body (html home)})
  (not-found nil))

(defonce server (atom nil))

(defn restart!
  ([] (restart! false))
  ([join?]
   (when @server (.stop @server))
   (reset! server (run-jetty app {:port 3000
                                  :join? join?}))))

(defn -main []
  (restart! true))
