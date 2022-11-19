(ns com.tylerkindy.synchro.email
  (:require [mount.core :refer [defstate]]
            [clojure.core.async :refer [chan go >!! <!]])
  (:import [org.apache.commons.mail SimpleEmail]))

;; Inspired by https://github.com/kisom/simple-email
(defn send-email [{:keys [hostname username password from]}
                  {:keys [to subject message]}]
  (println "Sending email to" to)
  (doto (SimpleEmail.)
    (.setHostName hostname)
    (.setAuthentication username password)
    (.setSSLOnConnect true)
    (.setFrom from)
    (.setSubject subject)
    (.setMsg message)
    (.addTo to)
    (.send)))

(declare send-chan)
(defstate send-chan
  :start (let [c (chan 10)]
           (go (while true
                 (let [{:keys [server email]} (<! c)]
                   (send-email server email))))
           c))

(defn queue-send [server email]
  (>!! send-chan {:server server, :email email}))
