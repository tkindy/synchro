(ns com.tylerkindy.synchro.main
  [:require
   [com.tylerkindy.synchro.routes :refer [app]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]])

(defonce server (atom nil))

(defn restart!
  ([] (restart! false))
  ([join?]
   (when @server (.stop @server))
   (reset! server (run-jetty (wrap-defaults app site-defaults)
                             {:port (or (Integer/parseInt (System/getenv "PORT")) 3000)
                              :join? join?}))))

(defn -main []
  (restart! true))
