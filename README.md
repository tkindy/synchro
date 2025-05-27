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
