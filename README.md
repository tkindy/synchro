# Synchro

http://synchro.tylerkindy.com/

## Development

### Database

If no `JDBC_DATABASE_URL` environment variable is specified, both the app and migrating tooling will default to a local Postgres instance with database `postgres`, user `postgres`, and password `password`.
These are the defaults for [the Postgres Docker container](https://hub.docker.com/_/postgres/), so just start one of those up.

#### Migrations

Migrations use [migratus](https://github.com/yogthos/migratus) through [clj-migratus](https://github.com/paulbutcher/clj-migratus).

To create a new migration:

```sh
clj -M:migrate create <name of migration>
```

To run migrations:

```sh
clj -M:migrate migrate
```
