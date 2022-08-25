(require '[com.tylerkindy.synchro.config :refer [config]])

{:store :database
 :db {:jdbcUrl (:jdbcUrl config)}}
