# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What is Synchro

A server-rendered Clojure web app for scheduling/availability coordination (similar to When2meet). Users create plans with date ranges, share links, and participants mark their availability. Minimal JavaScript — all interactions are form submissions.

## Build & Development Commands

```bash
# Start REPL (development)
clj -M:repl
# Then in REPL: (dev/auto-refresh) for file-watching hot-reload

# Build uberjar
clj -T:build uber          # produces target/synchro.jar

# Run production jar
java -jar target/synchro.jar

# Database migrations (CLI)
clj -M:migrate

# Docker
docker build -t synchro .
```

There are no automated tests.

## Architecture

**Stack:** Ring + Compojure (routing), Hiccup (HTML as Clojure data), Garden (CSS as Clojure data), HugSQL + next.jdbc (database), PostgreSQL, Mount (state lifecycle).

**State management:** All stateful components (Jetty server, HikariCP datasource, email async channel) are Mount `defstate` values, started/stopped via `mount/start`.

**Config:** Environment variables loaded via dotenv in `config.clj` as a delay. Required: `HTTP_SESSION_SECRET`, `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`. Optional: `HTTP_PORT` (default 80), `DB_MIGRATE_ON_STARTUP` (default true).

**Database layer:** SQL lives in `resources/`-adjacent `.sql` files (`src/.../db/sql/plans.sql`, `people.sql`). HugSQL generates Clojure functions from these at compile time. Migrations are in `resources/migrations/` using Migratus.

**Rendering:** All HTML is Hiccup vectors returned from handler functions. CSS is Garden data in `css.clj`. No templates — pure Clojure data structures throughout.

**JavaScript:** Only two vanilla JS files in `resources/` (`home.js` for form UX, `plan.js` for tri-state checkbox cycling). No frameworks, no AJAX.

**Email:** Async via `core.async` channel with a background goroutine processing sends through Apache Commons Email.

## Key Routes

- `GET /` — home page (plan creation form)
- `POST /plans` — create plan
- `GET /plans/:id` — view plan availability
- `POST /plans/:plan-id` — add person to plan
- `GET|POST /plans/:plan-id/edit/:person-id` — edit person availability
- `GET /up` — health check (Kamal)

## Data Model

Plans have dates (`plan_dates`), people have per-date availability states (`people_dates` with state: available/unavailable/ifneedbe).
