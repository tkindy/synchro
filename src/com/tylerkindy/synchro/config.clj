(ns com.tylerkindy.synchro.config
  (:require [clojure.string :as str]
            [dotenv :refer [env]]))

(defn- my-env
  ([key]
   (my-env key (fn [] (throw (RuntimeException. (str "No or blank value for environment variable " key))))))
  ([key default]
   (let [value (env key)]
     (if (or (nil? value) (str/blank? value))
       (if (fn? default)
         (default)
         default)
       value))))

(def config
  (delay
    {:http {:session-secret (my-env "HTTP_SESSION_SECRET")
            :port (parse-long (my-env "HTTP_PORT" "80"))}
     :db {:host (my-env "DB_HOST" "localhost")
          :dbname (my-env "DB_NAME")
          :user (my-env "DB_USER")
          :password (my-env "DB_PASSWORD")
          :migrate-on-startup? (parse-boolean (my-env "DB_MIGRATE_ON_STARTUP" "true"))}
     :base-url (my-env "BASE_URL" "https://synchro.tylerkindy.com")
     :email (let [api-key (my-env "RESEND_API_KEY" nil)]
              (when api-key
                {:api-key api-key
                 :from "notify@synchro.tylerkindy.com"
                 :debounce-ms (parse-long (my-env "EMAIL_DEBOUNCE_MS" "30000"))}))}))
