(ns com.tylerkindy.synchro.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(def config (edn/read (java.io.PushbackReader.
                       (io/reader "config.edn"))))
