(ns migratus
  (:require [com.tylerkindy.synchro.config :refer [config]]))

{:store :database
 :db {:jdbcUrl (:jdbc-url config)}}
