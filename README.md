# Synchro

http://synchro.tylerkindy.com/

## Development

### Philosophy

I wanted to try building a web app in the "classic" web style.
That means:

- Server-rendered pages
- Sending data to the server with form submissions
- No AJAX
- Minimal JavaScript

### Configuration

The app expects a `config.edn` file in the current working directory which contains a map with the following values:

- `:http`
  - `:port` - the port to run the web server on
  - `:session-secret` - a 16-byte hex string used to encrypt and decrypt session cookies. See "[Generating session secret](#generating-session-secret)" below.
- `:db`
  - `:dbname` - The name of the PostgreSQL database to connect to
  - `:user` - The username to use when connecting to the database
  - `:password` - The password to use when connecting to the database

### Building an uberjar

The build functions are defined in `build.clj`.

```
clj -T:build uber
```

### REPL

Use the `:repl` alias when starting up, then use the `(reset)` function in `dev.clj` to start up the system or refresh it after making some changes.

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

### Generating session secret

```
openssl rand 16 -hex
```
