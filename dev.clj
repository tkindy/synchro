(ns dev
  (:require [mount.core :as mount]
            [clojure.tools.namespace.repl :as tn]))

(defn start []
  (mount/start-with-args {:join? false
                          :cli-args []}))

(defn stop []
  (mount/stop))

(defn reset []
  (mount/stop)
  (tn/refresh :after 'dev/start))

(comment (reset))
