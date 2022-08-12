(ns com.tylerkindy.synchro.migrations
  (:require [migratus.core :as migratus]))

(def config {:store :database})

(defn migrate [opts]
  (migratus/migrate (merge config opts)))
