# Synchro

https://synchro.tylerkindy.com/

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
  - `:session-secret` - a 16-byte hex string used to encrypt and decrypt session cookies. See "[Generating session secret](#generating-session-secret)" below.
- `:db`
  - `:dbname` - The name of the PostgreSQL database to connect to
  - `:user` - The username to use when connecting to the database
  - `:password` - The password to use when connecting to the database
  - `:migrate-on-startup?` - Whether to run database migrations on app startup

It also expects the following parameters passed at the command line:

- `-p`, `--port`: the port to bind the HTTP server on. Defaults to 8080.

### Building an uberjar

The build functions are defined in `build.clj`.

```
clj -T:build uber
```

### REPL

Use the `:repl` alias when starting up, then evaluate `(auto-refresh)` in `dev.clj`.
This starts up the system and automatically refreshes it when you make changes to one of the tracked paths.

#### Migrations

Migrations use [migratus](https://github.com/yogthos/migratus).

To create a new migration, call `com.tylerkindy.synchro.db.migrations/create` passing the migration name.

If configured, the app will run migrations on startup.
During development, use the utility functions in the migrations namespace.

### Generating session secret

```
openssl rand 16 -hex
```
