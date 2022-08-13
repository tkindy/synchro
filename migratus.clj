{:store :database
 :db {:jdbcUrl (or (System/getenv "JDBC_DATABASE_URL")
                   "jdbc:postgresql:postgres?user=postgres&password=password")}}
