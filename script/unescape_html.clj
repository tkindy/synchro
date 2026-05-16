(require '[next.jdbc :as jdbc]
         '[next.jdbc.result-set :refer [as-unqualified-kebab-maps]])

(import '[org.apache.commons.text StringEscapeUtils]
        '[com.zaxxer.hikari HikariDataSource])

(defn create-ds []
  (let [ds (HikariDataSource.)]
    (doto ds
      (.setJdbcUrl (str "jdbc:postgresql://" (System/getenv "DB_HOST") "/" (System/getenv "DB_NAME")))
      (.setUsername (System/getenv "DB_USER"))
      (.setPassword (System/getenv "DB_PASSWORD")))
    ds))

(defn unescape [s]
  (when s
    (StringEscapeUtils/unescapeHtml4 s)))

(defn process-table! [ds {:keys [table id-col text-col]}]
  (let [rows (jdbc/execute! ds
               [(str "SELECT " id-col ", " text-col " FROM " table)]
               {:builder-fn as-unqualified-kebab-maps})
        total (count rows)]
    (println (str "  " table "." text-col ": " total " rows"))
    (loop [remaining rows
           updated 0
           processed 0]
      (if (empty? remaining)
        (do
          (println (str "  Done. " updated "/" total " rows changed."))
          updated)
        (let [{:keys [id] :as row} (first remaining)
              original (get row (keyword text-col))
              unescaped (unescape original)
              changed? (not= original unescaped)
              processed (inc processed)]
          (when changed?
            (jdbc/execute-one! ds
              [(str "UPDATE " table " SET " text-col " = ? WHERE " id-col " = ?")
               unescaped id]))
          (when (zero? (mod processed 100))
            (println (str "  Processed " processed "/" total "...")))
          (recur (rest remaining)
                 (if changed? (inc updated) updated)
                 processed))))))

(defn -main []
  (println "Connecting to database...")
  (let [ds (create-ds)]
    (try
      (println "Unescaping HTML entities in database fields:\n")

      (let [plan-changes (process-table! ds {:table "plans"
                                             :id-col "id"
                                             :text-col "description"})
            _ (println)
            people-changes (process-table! ds {:table "people"
                                               :id-col "id"
                                               :text-col "name"})]
        (println)
        (println (str "Complete. Total rows changed: " (+ plan-changes people-changes))))
      (finally
        (.close ds)))))

(-main)
