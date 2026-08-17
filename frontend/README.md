# Launch Window Frontend

The frontend is a React and TypeScript application built with Vite. It provides the Launch Window user interface and communicates with the Spring Boot backend through a shared fetch-based API client.

---

## Technology

- React 19
- TypeScript
- Vite
- React Router
- TanStack Query
- Lucide React
- ESLint
- Nginx for the production container

---

## Structure

Frontend source code:

```text
frontend/src
```

Important areas:

- `src/app`: router and application-level composition
- `src/components`: shared layout and reusable components
- `src/features/account`: profile, statistics, avatar updates, and account deletion
- `src/features/auth`: login, registration, session handling, and password reset
- `src/features/avatar`: avatar options and avatar rendering
- `src/features/calendar`: saved launches, calendar entries, participants, and invitations
- `src/features/friends`: friend requests and accepted friends
- `src/features/launches`: launch lists, filters, launch details, and viewing conditions
- `src/features/notes`: personal and shared notes with scope filtering
- `src/features/notifications`: messages, invitation actions, and unread counts
- `src/lib`: shared API client and utilities
- `src/assets`: avatars and other static assets

Larger features are divided into domain-specific `pages`, `components`, `hooks`, `api`, `model`, and `styles` folders where useful.

Production files:

- `frontend/Dockerfile`
- `frontend/nginx.conf`

---

## Install Dependencies

From `frontend`:

```powershell
npm ci
```

Use `npm install` when intentionally changing dependencies.

---

## Run in Development Mode

The backend should be running on:

```text
http://localhost:8080
```

Create `frontend/.env.local`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Start Vite:

```powershell
npm run dev
```

Open:

```text
http://localhost:5173
```

The backend must allow `http://localhost:5173` through `CORS_ALLOWED_ORIGINS`.

---

## API Configuration

The shared API client is located at:

```text
src/lib/api.ts
```

It reads:

```text
VITE_API_BASE_URL
```

Requests are constructed as:

```text
VITE_API_BASE_URL + request path
```

For direct local development, use `http://localhost:8080`. In production the variable is omitted, so requests use same-origin paths such as `/api/launches`. Nginx then proxies `/api` to the backend container.

The API client:

- sends and receives JSON
- attaches JWT bearer tokens to authenticated requests
- maps structured backend errors to `ApiClientError`
- supports field validation errors returned by the backend

---

## Authentication and Session Handling

Authentication state is managed in `src/features/auth/session`.

The frontend supports:

- registration
- login with username or email
- protected routes
- session restoration
- logout
- forgot-password requests
- password reset through emailed token links

The frontend never sends SMTP mail directly. It submits password-reset requests to the backend, which creates the token and sends the email.

---

## Server State

TanStack Query is used for server-backed state, caching, refetching, and mutations. Features refresh relevant queries after user actions, including notes, invitations, friendships, notifications, calendar entries, and account changes.

Some collaborative views also poll while mounted so changes made by another user appear without requiring a complete page refresh.

---

## Build

From `frontend`:

```powershell
npm run build
```

The command runs the TypeScript project build followed by Vite. Output is written to:

```text
frontend/dist
```

---

## Lint

```powershell
npm run lint
```

Lint and the production build are both run by GitHub Actions.

---

## Preview the Production Build

```powershell
npm run preview
```

This previews the generated `dist` directory locally. API behavior still depends on the configured backend URL or a same-origin proxy.

---

## Available Scripts

- `npm run dev`: start the Vite development server
- `npm run build`: type-check and create the production build
- `npm run lint`: run ESLint
- `npm run preview`: preview the production build

---

## Docker and Nginx

The frontend image uses a multi-stage build:

1. Node installs dependencies and creates the Vite production build.
2. Nginx serves the static files as a non-root user.

The production Nginx configuration:

- listens on container port `8080`
- serves hashed assets with long-lived immutable caching
- serves `index.html` without long-lived caching
- falls back to `index.html` for React Router routes
- proxies `/api/...` to `http://backend:8080`
- exposes `/health` for container health checks
- adds basic security headers

The public server uses Caddy in front of the container for HTTPS and certificate renewal.

---

## Production Deployment

The frontend is deployed together with the backend and MySQL through the root [compose.production.yml](../compose.production.yml).

After CI succeeds on `main`, GitHub Actions connects to the server using a restricted SSH deploy key. The server pulls the commit, rebuilds the images, starts the containers, and checks application health.

Production URL:

[https://launch-window.duckdns.org](https://launch-window.duckdns.org)