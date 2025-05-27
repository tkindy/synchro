(ns com.tylerkindy.synchro.db.core
  (:require [mount.core :refer [defstate]]
            [next.jdbc.connection :refer [->pool]]
            [next.jdbc.result-set :refer [as-unqualified-kebab-maps]]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :refer [hugsql-adapter-next-jdbc]]
            [com.tylerkindy.synchro.config :refer [config]])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defn create-ds []
  (hugsql/set-adapter! (hugsql-adapter-next-jdbc {:builder-fn as-unqualified-kebab-maps}))
  (->pool HikariDataSource
          (-> (:db @config)
              (assoc :dbtype "postgresql")
                    ; HikariCP expects "username", not "user"
              (assoc :username (get-in @config [:db :user]))
              (dissoc :user))))

(defstate ds
  :start (create-ds)
  :stop (.close ds))
