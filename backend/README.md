# Launch Window Backend

The backend is a Java 21 and Spring Boot application that exposes the Launch Window REST API.

It handles authentication, users, friendships, launches, weather forecasts, calendars, invitations, notes, notifications, account statistics, account deletion, and password reset emails. It also synchronizes external launch and weather data on configurable schedules.

---

## Technology

- Java 21
- Spring Boot 4
- Spring Web MVC and RestClient
- Spring Security with JWT bearer authentication
- Spring Data JPA and Hibernate
- MySQL 8.4
- Flyway database migrations
- Bean Validation
- Spring Mail
- Spring Boot Actuator
- Springdoc OpenAPI / Swagger UI
- Lombok
- Maven Wrapper

---

## Structure

Main source code:

```text
backend/src/main/java/com/launchwindow
```

Important packages:

- `config`: security, HTTP clients, scheduling, and application configuration
- `controller`: REST endpoints
- `dto`: grouped request and response records
- `exception`: domain exceptions and centralized API error handling
- `integration`: clients and payload mapping for external APIs
- `model`: JPA entities and domain enums
- `repository`: Spring Data repositories and custom queries
- `service`: application logic grouped by domain

Resources:

- `src/main/resources/application.properties`: shared application configuration
- `src/main/resources/application-production.properties`: production-specific settings
- `src/main/resources/db/migration`: versioned Flyway migrations

Tests:

```text
backend/src/test/java/com/launchwindow
```

Reusable test data builders are kept under the test source tree so production code remains free from test-only helpers.

---

## Configuration

The backend reads secrets and environment-specific values from environment variables.

### Required application variables

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

### Authentication and CORS

- `JWT_ISSUER`
- `JWT_EXPIRATION`
- `CORS_ALLOWED_ORIGINS`

### HTTP clients and synchronization

- `HTTP_CLIENT_CONNECT_TIMEOUT`
- `HTTP_CLIENT_READ_TIMEOUT`
- `LAUNCH_LIBRARY_BASE_URL`
- `LAUNCH_LIBRARY_PAGE_SIZE`
- `LAUNCH_LIBRARY_MAX_PAGES`
- `LAUNCH_LIBRARY_MAX_LAUNCHES`
- `LAUNCH_SYNC_ENABLED`
- `LAUNCH_SYNC_INTERVAL`
- `LAUNCH_SYNC_INITIAL_DELAY`
- `OPEN_METEO_BASE_URL`
- `OPEN_METEO_FORECAST_DAYS`
- `WEATHER_SYNC_ENABLED`
- `WEATHER_SYNC_INTERVAL`
- `WEATHER_SYNC_INITIAL_DELAY`

### Email and password reset

- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_SMTP_AUTH`
- `MAIL_SMTP_STARTTLS`
- `MAIL_FROM`
- `PASSWORD_RESET_FRONTEND_URL`
- `PASSWORD_RESET_EXPIRATION`
- `PASSWORD_RESET_REQUEST_COOLDOWN`

Use the root `.env.example` for local development and `.env.production.example` as the production template. Never commit real secrets.

---

## Database and Flyway

MySQL is the normal runtime database. Hibernate uses:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Flyway owns schema changes. Migrations are stored in:

```text
src/main/resources/db/migration
```

Applied migrations must not be edited. Add a new versioned migration for every later schema change.

The local Docker database is exposed on port `3307` by default:

```text
jdbc:mysql://localhost:3307/launch_window?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

---

## Run Locally

Start MySQL and Mailpit from the repository root:

```powershell
Copy-Item .env.example .env
docker compose up -d
```

Set the required variables in PowerShell:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3307/launch_window?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="launch_window_user"
$env:DB_PASSWORD="your_local_database_password"
$env:JWT_SECRET="your_base64_encoded_32_byte_secret"
```

Run from the repository root:

```powershell
.\backend\mvnw.cmd -f .\backend\pom.xml spring-boot:run
```

Alternatively, from `backend`:

```powershell
.\mvnw.cmd spring-boot:run
```

Default URLs:

- API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Actuator health: `http://localhost:8080/actuator/health`
- Mailpit inbox: `http://localhost:8025`

---

## API Areas

Main controller groups:

- `/api/auth`: registration, login, password reset requests, and password reset completion
- `/api/users`: profile, avatar, account statistics, and account deletion
- `/api/launches`: launch listing, filters, details, countries, and viewing conditions
- `/api/calendar`: saved launches and calendar navigation
- `/api/calendar/invitations`: launch sharing and invitation responses
- `/api/notes`: personal and shared launch notes
- `/api/friendships`: friend requests, accepted friends, and friendship management
- `/api/notifications`: notification history, unread counts, and read state

Authenticated requests use:

```http
Authorization: Bearer <token>
```

Consult Swagger UI for the exact paths, query parameters, validation rules, and response schemas.

---

## Launch Synchronization

The backend integrates with The Space Devs Launch Library. Scheduled synchronization imports and updates upcoming launches, organizations, vehicles, pads, locations, and mission information.

Synchronization limits and intervals are configurable through environment variables. It can be disabled for tests or local development with:

```env
LAUNCH_SYNC_ENABLED=false
```

---

## Weather and Viewing Conditions

Open-Meteo provides forecast data for upcoming launches. The backend stores weather snapshots and calculates a viewing score and condition used by the launch list, launch detail page, calendar, and best-viewing section.

Weather synchronization can be disabled with:

```env
WEATHER_SYNC_ENABLED=false
```

---

## Email and Password Reset

Password reset is handled entirely by the backend:

1. A user requests a reset using their email address.
2. The backend applies the configured cooldown and creates a time-limited, single-use reset token.
3. Spring Mail sends a link to the configured frontend URL.
4. The frontend submits the token and new password.
5. The backend validates the token, updates the password, and invalidates the token.

Local email is captured by Mailpit:

- SMTP: `localhost:1025`
- Inbox: `http://localhost:8025`

Production uses authenticated SMTP with STARTTLS. Health checks do not depend on the mail server, so a temporary SMTP outage does not mark the complete application unhealthy.

---

## Run Tests

From the repository root:

```powershell
.\backend\mvnw.cmd -f .\backend\pom.xml test
```

Or from `backend`:

```powershell
.\mvnw.cmd test
```

The full context test requires the database variables and an available MySQL database. GitHub Actions supplies an isolated MySQL 8.4 service for CI.

The suite includes model, repository, service, controller, security, integration-mapping, and application-context tests. Live external-API tests are skipped during the normal build.

---

## Manual API Testing

The root file [generated-requests.http](../generated-requests.http) contains requests for IntelliJ IDEA's HTTP client.

It covers authentication, launches, calendar operations, invitations, notes, friendships, notifications, user operations, and password reset flows.

---

## Docker

The backend image is built from [Dockerfile](Dockerfile) using a multi-stage build:

1. Maven compiles and packages the application.
2. A smaller Java runtime image runs the generated JAR as a non-root user.

The complete production stack is defined in [../compose.production.yml](../compose.production.yml).