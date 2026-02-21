(ns com.tylerkindy.synchro.email
  (:require [mount.core :refer [defstate]]
            [clojure.string :as str])
  (:import [com.resend Resend]
           [com.resend.services.emails.model CreateEmailOptions]
           [java.util.concurrent Executors TimeUnit]))

(defn send-email [{:keys [api-key from]}
                  {:keys [to subject message]}]
  (let [resend (Resend. api-key)
        params (-> (CreateEmailOptions/builder)
                   (.from from)
                   (.to (into-array String [to]))
                   (.subject subject)
                   (.html message)
                   .build)]
    (.send (.emails resend) params)))

(def ^:private pending (atom {}))

(def ^:private default-debounce-ms 30000)

(defn build-notification-email [{:keys [description base-url plan-id entries to
                                        respondent-count]}]
  (let [lines (map (fn [{:keys [person-name action]}]
                     (str "<p>" person-name " " action " their availability</p>"))
                   entries)
        url (str base-url "/plans/" plan-id)]
    {:to to
     :subject (str "New activity on '" description "'")
     :message (str (str/join "\n" lines)
                   "<p>" respondent-count
                   (if (= respondent-count 1) " person has" " people have")
                   " responded so far.</p>"
                   "<p><a href=\"" url "\">View the plan</a></p>")}))

(defn- send-pending-entry! [server-config plan-id entry]
  (let [entry-count (count (:entries entry))]
    (println (str "Sending notification email for plan " plan-id " (" entry-count " entries)"))
    (try
      (send-email server-config (build-notification-email (assoc entry :plan-id plan-id)))
      (println (str "Sent notification email for plan " plan-id))
      (catch Exception e
        (println "Error sending email:" e)))))

(defn- flush-ready! []
  (let [now (System/currentTimeMillis)
        expired? (fn [[_ {:keys [server last-update]}]]
                   (let [ms (or (:debounce-ms server) default-debounce-ms)]
                     (> (- now last-update) ms)))
        [old _] (swap-vals! pending
                             (fn [m]
                               (into {} (remove expired? m))))
        ready (filter expired? old)]
    (doseq [[plan-id entry] ready]
      (send-pending-entry! (:server entry) plan-id entry))))

(defn- flush-all! []
  (let [[old _] (reset-vals! pending {})]
    (doseq [[plan-id entry] old]
      (send-pending-entry! (:server entry) plan-id entry))))

(defstate email-sender
  :start (let [executor (Executors/newSingleThreadScheduledExecutor)]
           (.scheduleAtFixedRate executor ^Runnable flush-ready! 5 5 TimeUnit/SECONDS)
           executor)
  :stop (do
          (.shutdown email-sender)
          (flush-all!)))

(defn queue-notification [server-config {:keys [to plan-id description base-url
                                                person-name action respondent-count]}]
  (swap! pending
         (fn [m]
           (let [existing (get m plan-id)
                 entry (-> (or existing {:server server-config
                                         :to to
                                         :description description
                                         :base-url base-url
                                         :entries []})
                           (update :entries conj {:person-name person-name
                                                  :action action})
                           (assoc :respondent-count respondent-count
                                  :last-update (System/currentTimeMillis)))]
             (assoc m plan-id entry)))))
