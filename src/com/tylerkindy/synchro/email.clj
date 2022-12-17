(ns com.tylerkindy.synchro.email
  (:require [mount.core :refer [defstate]]
            [clojure.core.async :refer [chan go >!! <!]])
  (:import [org.apache.commons.mail SimpleEmail]))

;; Inspired by https://github.com/kisom/simple-email
(defn send-email [{:keys [hostname username password from]}
                  {:keys [to subject message]}]
  (doto (SimpleEmail.)
    (.setHostName hostname)
    (.setAuthentication username password)
    (.setSSLOnConnect true)
    (.setFrom from)
    (.setSubject subject)
    (.setMsg message)
    (.addTo to)
    (.send)))

(defstate send-chan
  :start (let [c (chan 10)]
           (go (while true
                 (let [{:keys [server email]} (<! c)]
                   (try
                     (send-email server email)
                     (catch Exception e
                       (println "Error sending email:" e))))))
           c))

(defn queue-send [server email]
  (>!! send-chan {:server server, :email email}))
