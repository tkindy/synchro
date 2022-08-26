(ns com.tylerkindy.synchro.main
  [:require
   [com.tylerkindy.synchro.routes :refer [app]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.middleware.session.cookie :refer [cookie-store]]
   [com.tylerkindy.synchro.config :refer [config]]])

(defonce server (atom nil))

(defn parse-session-secret [secret]
  (-> (java.util.HexFormat/of)
      (.parseHex secret)))

(def app-settings
  (let [session-store (cookie-store {:key (parse-session-secret
                                           (get-in config [:http :session-secret]))})]
    (-> site-defaults
        (assoc-in [:session :store] session-store)

        ; need to use non-default name to avoid old session
        ; cookies causing decryption failures
        (assoc-in [:session :cookie-name] "ring-session2"))))

(defn restart!
  ([] (restart! false))
  ([join?]
   (when @server (.stop @server))
   (reset! server (run-jetty (wrap-defaults app app-settings)
                             {:port (get-in config [:http :port])
                              :join? join?}))))

(comment (restart!))

(defn -main []
  (restart! true))
