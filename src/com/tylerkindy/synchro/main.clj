(ns com.tylerkindy.synchro.main
  [:require
   [com.tylerkindy.synchro.routes :refer [app]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [com.tylerkindy.synchro.config :refer [config]]])

(defonce server (atom nil))

(defn restart!
  ([] (restart! false))
  ([join?]
   (when @server (.stop @server))
   (reset! server (run-jetty (wrap-defaults app site-defaults)
                             {:port (:port config)
                              :join? join?}))))

(comment (restart!))

(defn -main []
  (restart! true))
