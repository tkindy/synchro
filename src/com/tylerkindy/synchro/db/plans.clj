(ns com.tylerkindy.synchro.db.plans
  (:require [hugsql.core :refer [def-db-fns]]))

(def-db-fns "com/tylerkindy/synchro/db/sql/plans.sql")
