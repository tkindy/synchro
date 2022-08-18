(ns com.tylerkindy.synchro.db.core
  (:require [hikari-cp.core :refer [make-datasource]]
            [next.jdbc.result-set :refer [as-unqualified-kebab-maps]]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :refer [hugsql-adapter-next-jdbc]]))

(hugsql/set-adapter! (hugsql-adapter-next-jdbc {:builder-fn as-unqualified-kebab-maps}))
(def db (or (System/getenv "JDBC_DATABASE_URL")
            "jdbc:postgresql:postgres?user=postgres&password=password"))
(def ds (make-datasource {:jdbc-url db}))
