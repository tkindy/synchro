(ns com.tylerkindy.synchro.main
  [:require
   [com.tylerkindy.synchro.routes :refer [app]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [clojure.string :as str]])

(defonce server (atom nil))

(defn env-port []
  (let [env-var (System/getenv "PORT")]
    (if (str/blank? env-var)
      nil
      (Integer/parseInt env-var))))

(defn restart!
  ([] (restart! false))
  ([join?]
   (when @server (.stop @server))
   (reset! server (run-jetty (wrap-defaults app site-defaults)
                             {:port (or (env-port) 3000)
                              :join? join?}))))

(defn -main []
  (restart! true))
