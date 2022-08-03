(ns com.tylerkindy.synchro.main
  [:require
   [ring.adapter.jetty :refer [run-jetty]]
   [hiccup.core :refer [html]]])

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html [:html
                [:body
                 [:h1 "Hello, World!"]]])})

(def server (atom nil))

(defn restart!
  ([] (restart! false))
  ([join?]
   (let [s @server]
     (when s (.stop s))
     (reset! server (run-jetty handler {:port 3000
                                        :join? join?})))))

(defn -main []
  (restart! true))
