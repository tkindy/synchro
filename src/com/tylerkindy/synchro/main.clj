(ns com.tylerkindy.synchro.main
  [:require
   [mount.core :refer [defstate] :as mount]
   [com.tylerkindy.synchro.routes :refer [app]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.middleware.session.cookie :refer [cookie-store]]
   [com.tylerkindy.synchro.config :refer [config]]]
  (:gen-class))

(defn parse-session-secret [secret]
  (-> (java.util.HexFormat/of)
      (.parseHex secret)))

(defstate app-settings
  :start (let [session-store (cookie-store {:key (parse-session-secret
                                                  (get-in config [:http :session-secret]))})]
           (-> site-defaults
               (assoc-in [:session :store] session-store)

               ; need to use non-default name to avoid old session
               ; cookies causing decryption failures
               (assoc-in [:session :cookie-name] "ring-session2"))))

(defn start-server [join?]
  (run-jetty (wrap-defaults app app-settings)
             {:port (get-in config [:http :port])
              :join? join?}))

(defstate server
  :start (start-server (:join? (mount/args)))
  :stop (.stop server))

(defn -main [& args]
  (mount/start-with-args {:join? true
                          :cli-args args}))
