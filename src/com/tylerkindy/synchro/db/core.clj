(ns com.tylerkindy.synchro.db.core
  (:require [next.jdbc.connection :refer [->pool]]
            [next.jdbc.result-set :refer [as-unqualified-kebab-maps]]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :refer [hugsql-adapter-next-jdbc]]
            [com.tylerkindy.synchro.config :refer [config]])
  (:import (com.zaxxer.hikari HikariDataSource)))

(hugsql/set-adapter! (hugsql-adapter-next-jdbc {:builder-fn as-unqualified-kebab-maps}))
(def ds (->pool HikariDataSource
                (-> (:db config)
                    (assoc :dbtype "postgresql")
                    ; HikariCP expects "username", not "user"
                    (assoc :username (get-in config [:db :user]))
                    (dissoc :user))))
