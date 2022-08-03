(ns com.tylerkindy.synchro.main
  [:require [ring.adapter.jetty :refer [run-jetty]]])

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello, World!"})

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
