(ns migratus
  (:require [com.tylerkindy.synchro.config :refer [config]]
            [mount.core :as mount]))

(-> (mount/only #{#'com.tylerkindy.synchro.config/file-config
                  #'com.tylerkindy.synchro.config/cli-args
                  #'com.tylerkindy.synchro.config/config})
    (mount/swap {#'com.tylerkindy.synchro.config/cli-args {}})
    mount/start)

{:store :database
 :db (-> (:db config)
         (assoc :dbtype "postgresql"))}
