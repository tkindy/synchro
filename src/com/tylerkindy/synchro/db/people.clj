(ns com.tylerkindy.synchro.db.people
  (:require [hugsql.core :refer [def-db-fns]]))

(def-db-fns "com/tylerkindy/synchro/db/sql/people.sql")
