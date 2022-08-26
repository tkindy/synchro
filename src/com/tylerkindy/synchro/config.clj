(ns com.tylerkindy.synchro.config
  (:require [mount.core :refer [defstate]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(declare config)
(defstate config
  :start (edn/read (java.io.PushbackReader.
                    (io/reader "config.edn"))))
