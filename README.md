# Launch Window

Launch Window is a full-stack web application for discovering upcoming space launches, comparing forecast viewing conditions, and organizing launches in a personal calendar.

Users can create accounts, choose profile avatars, save launches, write notes, add friends, share calendar entries, respond to invitations, and follow activity through notifications. Launch and weather data are synchronized automatically from external APIs.

---

## Live Application

- App: [https://launch-window.duckdns.org](https://launch-window.duckdns.org)
- Swagger UI: [https://launch-window.duckdns.org/swagger-ui.html](https://launch-window.duckdns.org/swagger-ui.html)
- OpenAPI JSON: [https://launch-window.duckdns.org/v3/api-docs](https://launch-window.duckdns.org/v3/api-docs)

---

## Stack

- Backend: Java 21, Spring Boot 4, Spring Security, JWT, Spring Data JPA
- Frontend: React 19, TypeScript, Vite, TanStack Query, React Router
- Database: MySQL 8.4 with Flyway migrations
- External services: The Space Devs Launch Library and Open-Meteo
- Email: SMTP-based password reset emails
- API documentation: Springdoc OpenAPI and Swagger UI
- Testing: Maven, Spring Boot Test, Mockito, MockMvc, repository integration tests
- DevOps: Docker, Docker Compose, Nginx, Caddy, GitHub Actions, Kamatera

---

## Project Guides

- [backend/README.md](backend/README.md): backend architecture, configuration, API, synchronization, email, and tests
- [frontend/README.md](frontend/README.md): frontend structure, local development, API configuration, linting, and production behavior

Important root files:

- [docker-compose.yml](docker-compose.yml): local MySQL and Mailpit services
- [compose.production.yml](compose.production.yml): complete production stack
- [.env.example](.env.example): local environment template
- [.env.production.example](.env.production.example): production environment template
- [generated-requests.http](generated-requests.http): manual API requests for IntelliJ IDEA

---

## Main Features

- Registration and login with username or email
- JWT authentication and protected routes
- Password reset links delivered by email
- Upcoming launch listing with filters and cursor pagination
- Launch details, mission information, location, and weather forecasts
- Ranked viewing conditions based on forecast data
- Personal launch calendar arranged around the present day
- Calendar invitations and shared launch participation
- Personal and shared launch notes
- Friend requests and accepted-friend management
- Notification history with unread counts and invitation actions
- User avatars, account statistics, and account deletion
- Scheduled launch and weather synchronization

---

## Local Development

### 1. Create the environment file

From the repository root:

```powershell
Copy-Item .env.example .env
```

Replace the example passwords and JWT secret before starting the application.

### 2. Start MySQL and Mailpit

```powershell
docker compose up -d
```

Local services:

- MySQL: `localhost:3307`
- Mailpit SMTP: `localhost:1025`
- Mailpit inbox: `http://localhost:8025`

### 3. Start the backend

Set the required database variables in the terminal, using the same credentials as `.env`:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3307/launch_window?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="launch_window_user"
$env:DB_PASSWORD="your_local_database_password"
$env:JWT_SECRET="your_base64_encoded_32_byte_secret"

.\backend\mvnw.cmd -f .\backend\pom.xml spring-boot:run
```

The backend starts on `http://localhost:8080`.

### 4. Start the frontend

Create `frontend/.env.local`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Then run:

```powershell
cd frontend
npm ci
npm run dev
```

Open `http://localhost:5173`.

---

## Tests and Checks

Backend:

```powershell
.\backend\mvnw.cmd -f .\backend\pom.xml test
```

Frontend:

```powershell
cd frontend
npm run lint
npm run build
```

---

## Production

The application is deployed on a shared Ubuntu server. Caddy handles HTTPS and forwards traffic to the frontend container, while Nginx serves the React application and proxies `/api` requests to the backend container.

The production stack contains:

- MySQL
- Spring Boot backend
- Nginx frontend

Production configuration is supplied through an ignored `.env.production` file based on `.env.production.example`. Database contents are backed up automatically by a systemd timer.

---

## CI/CD

GitHub Actions runs backend tests and frontend lint/build checks for changes to `main` and pull requests.

After successful checks on `main`, the deployment workflow connects to the server through a restricted SSH key. The server pulls the new commit, rebuilds the Docker images, recreates the application containers, and verifies application health.

Workflow files:

- [.github/workflows/backend-ci.yml](.github/workflows/backend-ci.yml): backend and frontend validation
- [.github/workflows/deploy.yml](.github/workflows/deploy.yml): production deployment after successful checks

---

## External Data

Launch Window uses:

- [The Space Devs Launch Library](https://thespacedevs.com/llapi): upcoming launch and mission data
- [Open-Meteo](https://open-meteo.com): weather forecasts used to calculate viewing conditions

The backend owns both integrations. The frontend only consumes Launch Window's API.