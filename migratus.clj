(ns migratus
  (:require [com.tylerkindy.synchro.config :refer [config]]))

{:store :database
 :db (-> (:db config)
         (assoc :dbtype "postgresql"))}
