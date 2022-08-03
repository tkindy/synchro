(ns com.tylerkindy.synchro.main
  [:require
   [ring.adapter.jetty :refer [run-jetty]]
   [hiccup.core :refer [html]]
   [clojure.string :as str]])

(defn handler [request]
  (let [accept (get-in request [:headers "accept"])]
    (if (str/includes? accept "text/html")
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (html [:html
                    [:body
                     [:h1 "Hello, World!"]]])}
      {:status 404})))

(def server (atom nil))

(defn restart!
  ([] (restart! false))
  ([join?]
   (when @server (.stop @server))
   (reset! server (run-jetty handler {:port 3000
                                      :join? join?}))))

(defn -main []
  (restart! true))
