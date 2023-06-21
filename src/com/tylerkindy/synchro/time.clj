(ns com.tylerkindy.synchro.time
  (:import [java.time LocalDateTime ZoneOffset]))

(defn now []
  (LocalDateTime/now ZoneOffset/UTC))
