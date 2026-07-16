
<div align="center">

# 🚐 Employee Transport Management System

**A full-stack platform for managing employee & citizen transport logistics — booking, live tracking, safety alerts, and admin operations in one system.**

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)](https://react.dev/)
[![MySQL](https://img.shields.io/badge/Database-MySQL-4479A1?logo=mysql)](https://www.mysql.com/)
[![Kafka](https://img.shields.io/badge/Messaging-Kafka-231F20?logo=apachekafka)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Cache-Redis-DC382D?logo=redis)](https://redis.io/)
[![CI](https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?logo=githubactions)](.github/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Proprietary-lightgrey)](#license)

[Overview](#overview) • [Architecture](#architecture) • [Roles & Access Model](#roles--access-model) • [Features](#features) • [Tech Stack](#tech-stack) • [Getting Started](#getting-started) • [API Reference](#api-reference) • [Security Notes](#security-notes--known-limitations) • [Testing & CI](#testing--ci)

</div>

---

## Overview

The Employee Transport Management System (ETMS) is a full-stack application for running organizational shuttle/transport operations — seat booking, route management, live safety alerting, and admin analytics — behind a single REST API and a React SPA.

The backend is a Spring Boot 3 service with **stateless JWT auth + refresh tokens**, **Kafka-based async booking events**, **Redis-backed caching**, **IP-based rate limiting**, and **Resilience4j circuit breakers/retries** around the booking flow. This is built with real production patterns, not just CRUD scaffolding.

---

## Roles & Access Model

The system has **three roles**, but only **two underlying identity tables**:

| Role | Stored in | Spring Security authority | Who they are |
|---|---|---|---|
| `ADMIN` | `admins` table | `ROLE_ADMIN` | Full operational control |
| `EMPLOYEE` | `employees` table | `ROLE_EMPLOYEE` | Staff booking a seat on a shared shuttle |
| `CITIZEN` | `employees` table (same table, different `role` value) | `ROLE_EMPLOYEE` | Public/non-employee riders |

**Important nuance verified directly in the code:** `CustomUserDetailsService` grants everyone found in the `employees` table the *same* `ROLE_EMPLOYEE` Spring Security authority — regardless of whether their `role` column says `EMPLOYEE` or `CITIZEN`. So Spring Security's `hasRole(...)` checks cannot tell an Employee from a Citizen; that distinction is enforced entirely in **application logic**, specifically:

- **One-rider-per-bus rule** (`BookingService.executeBookSeatByEmail`): a route can only have **one non-citizen ("rider")** booking confirmed on it at a time. Citizens can share a route with each other freely; a second Employee cannot book onto a route that already has an Employee rider assigned. This is the core domain rule that actually differentiates the two roles in practice.
- **Frontend routing**: `EMPLOYEE` and `CITIZEN` both land on the same `/dashboard` page (`ProtectedRoute roleRequired={['EMPLOYEE', 'CITIZEN']}`); `ADMIN` gets a separate `/admin` route. Within the shared dashboard, a "commute" tab is hidden specifically for the `EMPLOYEE` role and shown for others.
- **Registration**: `POST /api/auth/register` accepts `role: "EMPLOYEE"` or `role: "CITIZEN"` and stores it accordingly on the same `Employee` entity.

---

## Architecture

```
┌───────────────────────┐        HTTPS / REST + JWT         ┌────────────────────────────────┐
│   React 19 (Vite) SPA  │ ─────────────────────────────────▶│      Spring Boot 3 API         │
│                        │◀───────────────────────────────── │                                 │
│  - Admin dashboard     │            JSON responses          │  Controller → Service → Repo    │
│  - Employee/Citizen    │                                     │  (Spring Data JPA / Hibernate) │
│    shared dashboard    │                                     │                                 │
│  - Google Maps live    │                                     │  ┌───────────┐  ┌─────────────┐ │
│    map (dynamic script)│                                     │  │ RateLimit │  │  JwtFilter  │ │
│  - Recharts analytics  │                                     │  │ Filter    │→ │  (auth)     │ │
└────────────────────────┘                                     │  └───────────┘  └──────┬──────┘ │
                                                                 └────────┬────────────────┼────────┘
                                                                          │ JDBC           │
                                                                          ▼                ▼
                                                                 ┌────────────────┐ ┌─────────────┐
                                                                 │  MySQL / H2    │ │    Redis    │
                                                                 │  (persistence) │ │ (read cache)│
                                                                 └────────────────┘ └─────────────┘
                                                                          │
                                                                          ▼
                                                                 ┌────────────────┐
                                                                 │     Kafka      │
                                                                 │ booking-events │
                                                                 │  (3 partitions)│
                                                                 └────────────────┘
```

**Key architectural decisions, confirmed directly in the source:**
- **Stateless auth with refresh tokens** — short-lived JWT access tokens (default 24h, `jwt.expiration`) plus a persisted `RefreshToken` entity (7-day expiry, one active token per user — issuing a new one deletes the old).
- **Layered backend** — strict Controller → Service → Repository separation; controllers are thin adapters over services.
- **Idempotent, concurrency-safe booking** — `BookingRequest` accepts an `idempotencyKey` (unique DB column on `Booking`), and `Route.bookedSeats` is protected by **optimistic locking** (`@Version`) with a manual 3-attempt retry loop in `BookingService` for concurrent seat contention.
- **Booking window enforcement** — `SystemConfig.bookingWindow` (default 2 hours) blocks new bookings once a route's pickup time is within that window, parsed via multiple time-format fallbacks (`hh:mm a`, `HH:mm`, etc.) to tolerate inconsistent client input.
- **Resilience on the critical path** — `@CircuitBreaker` + `@Retry` (Resilience4j) wrap `bookingService` specifically: a 10-request sliding window, 50% failure threshold, and up to 3 retry attempts.
- **Rate limiting at the edge** — a custom `RateLimitingFilter` (Bucket4j, in-memory per-IP bucket, 100 requests/minute) runs *before* the JWT filter, so abusive traffic is rejected before any auth work happens. It explicitly skips `/actuator`, `/h2-console`, and Swagger paths.
- **Redis as a read-through cache, not for rate limiting** — `RouteService` and `SystemConfigService` use `@Cacheable`/`@CacheEvict` on route lists/details and the global config (10-minute TTL). `RedisConfig` registers a custom `CacheErrorHandler` that logs and degrades gracefully instead of failing requests if Redis is unreachable.
- **Async event pipeline** — booking creation and cancellation publish JSON events to a Kafka topic (`booking-events`, 3 partitions); `BookingEventConsumer` currently just logs them, with a comment noting it's a hook point for future notifications/audit logging.
- **Observability built in** — Actuator + Micrometer expose `/actuator/health`, `/actuator/metrics`, and `/actuator/prometheus`.
- **Environment-driven config** — dev (`application.properties`) vs prod (`application-prod.properties`) profiles. Production sets `ddl-auto=validate` (no silent schema drift) and requires `JWT_SECRET` with **no fallback** — the app throws `IllegalArgumentException` at startup if it's missing, rather than running with a weak default.

---

## Features

- **Role-based booking** — Employees and Citizens book seats on routes; the "one rider per bus" business rule (above) is enforced server-side, not just in the UI.
- **Multi-seat, idempotent bookings** — passenger detail tracking, duplicate-booking prevention (`DuplicateBookingException`), seat-limit enforcement (`SeatUnavailableException`), and safe request retries via idempotency keys.
- **Admin Command Center** — full CRUD on routes, admin-only employee management (list/view/create/delete — see [Security Notes](#security-notes--known-limitations)), manual booking assignment/cancellation overrides, and live system configuration.
- **Safety Alerts** — any authenticated user can raise an alert (`type: "SOS"` or `"QUERY"`) with a message/location; admins view active alerts, respond with a message, or resolve.
- **Live Map** — the employee/citizen dashboard dynamically loads the **Google Maps JavaScript API** (via `VITE_GOOGLE_MAPS_API_KEY`) for a dark-themed live map. Note: `leaflet`/`react-leaflet` are listed as frontend dependencies but are **not actually used anywhere** in the current codebase — Google Maps is the real implementation.
- **Analytics Dashboard** — Recharts-powered admin dashboard backed by `/api/analytics/dashboard`, which returns total bookings/routes/employees plus a weekly usage histogram (bookings grouped by day of week).
- **Resilient by design** — circuit breaking/retry around booking, graceful Redis degradation, IP-based rate limiting.
- **Auto-generated API docs** — springdoc-openapi serves a live Swagger UI at `/swagger-ui.html`, with every endpoint annotated (`@Operation`, `@Tag`).

---

## Tech Stack

**Backend**
- Java 21, Spring Boot 3.2.3
- Spring Security (JWT via `jjwt` 0.11.5, BCrypt password hashing, refresh tokens)
- Spring Data JPA / Hibernate, MySQL (prod) / H2 (dev & test, in-memory)
- Apache Kafka (`spring-kafka`) for async booking events
- Redis (`spring-boot-starter-data-redis`) as a read-through cache
- Resilience4j (circuit breaker + retry) and Bucket4j (rate limiting)
- springdoc-openapi 2.3.0 (Swagger UI)
- Micrometer + Prometheus + Spring Actuator for observability
- Checkstyle (build-time lint gate, `validate` phase) + JaCoCo (coverage reporting)
- JUnit 5, Mockito, Spring Security Test, Spring Kafka Test (incl. `@EmbeddedKafka` integration tests)

**Frontend**
- React 19 + Vite 8, React Router 7
- Axios for API communication
- Recharts for analytics visualization
- Google Maps JavaScript API (dynamic script injection) for the live map
- Framer Motion for UI transitions, Lucide React for icons
- ESLint + Prettier; Vitest for unit tests

**CI/CD**
- GitHub Actions (`.github/workflows/ci.yml`) — builds/tests the backend (Maven, JDK 21) and frontend (npm, Node 20) independently on every push/PR to `main`/`master`.

---

## Getting Started

### Prerequisites

- JDK 21+
- Node.js 18+
- MySQL Server (or skip this and run the `dev` profile, which uses in-memory H2)
- Optional, for full feature parity locally: Redis and Kafka running (the app starts fine without them, but caching and async booking events won't do anything)
- A Google Maps JavaScript API key if you want the live map to render on the frontend

### 1. Clone the repository

```bash
git clone https://github.com/anurag-yv/EMPLOYEE-TRANSPORT-MANAGEMENT-SYSTEM.git
cd EMPLOYEE-TRANSPORT-MANAGEMENT-SYSTEM
```

### 2. Backend setup

```bash
cd employee-transport-system
```

Edit `src/main/resources/application.properties` if you want your own MySQL instance (defaults to `jdbc:mysql://localhost:3306/transport_db`, user `root`, no password).

Run with the `dev` profile to get seeded demo data (one Employee, one Admin, two sample routes):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API starts on **`http://localhost:1001`** (`server.port=${PORT:1001}`).

**Seeded demo credentials (dev profile only, from `DataSeeder.java`):**

| Role | Email | Password |
|---|---|---|
| Employee | `employee@example.com` | `password123` |
| Admin | `admin@example.com` | `password123` |

*(No Citizen account is seeded by default — register one via the frontend's role picker or `POST /api/auth/register` with `"role": "CITIZEN"`.)*

Swagger UI: `http://localhost:1001/swagger-ui.html`

### 3. Frontend setup

```bash
cd ../frontend
npm install

cat > .env << 'EOF'
VITE_API_BASE_URL=http://localhost:1001
VITE_GOOGLE_MAPS_API_KEY=your_google_maps_key_here
EOF

npm run dev
```

Runs on Vite's default dev server (`http://localhost:5173`). The app works without a Maps key; the live map panel will just fail to load.

### 4. Verify

```bash
curl http://localhost:1001/api/auth/health
# → "Backend is UP"

curl -X POST http://localhost:1001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"password123"}'
```

---

## API Reference

All endpoints below are read directly from the `@RestController` classes and cross-checked against `SecurityConfig` (URL-level rules) and `@PreAuthorize` (method-level rules). Full interactive docs at `/swagger-ui.html`.

### Auth — `/api/auth` (all public — no token required)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/login` | Authenticate, returns access + refresh token |
| `POST` | `/api/auth/refresh` | Exchange a refresh token for a new access token |
| `POST` | `/api/auth/register` | Register as `EMPLOYEE` or `CITIZEN` |
| `POST` | `/api/auth/admin/create-admin` | Create an admin account — **see security note below** |
| `GET`  | `/api/auth/health` | Liveness check |

### Bookings — `/api/booking` (any authenticated user)

| Method | Endpoint | Description |
|---|---|---|
| `GET`  | `/api/booking` | List all bookings (optional pagination/sort) |
| `GET`  | `/api/booking/{id}` | Get a single booking |
| `POST` | `/api/booking` | Create a booking (idempotency-key supported; one-rider-per-bus rule enforced) |
| `GET`  | `/api/booking/my` | Get the current user's bookings |
| `POST` | `/api/booking/assign` | Manually assign a rider to a route (no admin-only restriction is currently enforced at this endpoint — see security note) |
| `GET`  | `/api/booking/route/{routeId}` | List bookings for a specific route |
| `DELETE` | `/api/booking/{id}` | Cancel your own booking (ownership checked in the service layer) |
| `DELETE` | `/api/booking/admin/{id}` | Cancel any booking (no admin-only restriction currently enforced at this endpoint either — see security note) |

### Routes — `/api/routes`

| Method | Endpoint | Auth enforced | Description |
|---|---|---|---|
| `GET`  | `/api/routes` | Any authenticated user | List routes (optional pagination/sort) |
| `GET`  | `/api/routes/{id}` | Any authenticated user | Get route details |
| `POST` | `/api/routes` | `ADMIN` (URL rule in `SecurityConfig`) | Create a route |
| `PUT`  | `/api/routes/{id}` | `ADMIN` | Update a route |
| `DELETE` | `/api/routes/{id}` | `ADMIN` | Delete a route (cascades booking deletion for that route) |

### Employees — `/api/employees` (every method is `ADMIN`-only via `@PreAuthorize` in `EmployeeService`, including the GET endpoints)

| Method | Endpoint | Description |
|---|---|---|
| `GET`  | `/api/employees` | Paginated employee list (as `EmployeeResponseDTO` — password never included) |
| `GET`  | `/api/employees/{id}` | Get one employee |
| `POST` | `/api/employees` | Add an employee (password is BCrypt-hashed server-side) |
| `DELETE` | `/api/employees/{id}` | Delete an employee (also deletes their bookings) |

### Alerts — `/api/alerts` (any authenticated user, admin actions not role-restricted at the code level — see security note)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/alerts` | Raise an alert (`type`, `message`, `location`) |
| `GET`  | `/api/alerts/active` | All unresolved alerts (optional pagination) |
| `GET`  | `/api/alerts/my` | Current user's alerts |
| `PUT`  | `/api/alerts/{id}/respond` | Respond to an alert with a message, marks resolved |
| `PUT`  | `/api/alerts/{id}/resolve` | Mark resolved without a custom response |

### Analytics & Config

| Method | Endpoint | Auth enforced | Description |
|---|---|---|---|
| `GET`  | `/api/analytics/dashboard` | Any authenticated user | Total bookings/routes/employees + weekly usage histogram |
| `GET`  | `/api/config` | Any authenticated user | Read global system config |
| `PUT`  | `/api/config` | `ADMIN` (URL rule) | Update global system config |

---

## Security Notes & Known Limitations

Documenting these plainly rather than glossing over them, since they're the kind of thing a reviewer will find anyway:

- **`POST /api/auth/admin/create-admin` is publicly reachable.** `SecurityConfig` permits all of `/api/auth/**`, and this endpoint has no additional method-level check. Anyone who can reach the API can currently create an admin account. **This should be locked down (e.g., require an existing admin's JWT, or gate it behind an invite/setup token) before any real deployment.**
- **`/api/booking/assign` and `/api/booking/admin/{id}` are documented as admin actions but are not actually role-restricted** in `SecurityConfig` or via `@PreAuthorize` — any authenticated user can currently call them.
- **`/api/alerts/{id}/respond` and `/api/alerts/{id}/resolve`** (intended for admins responding to alerts) similarly have no role restriction at the code level today.
- **Citizens and Employees share the same Spring Security authority** (`ROLE_EMPLOYEE`), so the two roles cannot be distinguished by any `hasRole(...)` check — only by the application-level "one rider per bus" logic described above. If you need Citizens to have genuinely different API permissions in the future, that will require reading the `role` claim explicitly in services/controllers, not just relying on Spring Security roles.
- **`SystemConfig.maxBookings`** is stored and admin-editable via `PUT /api/config`, but is **not currently read or enforced anywhere** in `BookingService` — it's effectively a dead setting today.
- **CORS is restricted to localhost by default** (`http://localhost:*`, `http://127.0.0.1:*`) — update `SecurityConfig.corsConfigurationSource()` for your real frontend origin before deploying.
- **JWTs carry the user's role as a claim** but are otherwise standard HS256 tokens; access tokens last 24h by default (`jwt.expiration`), refresh tokens 7 days.

---

## Testing & CI

Backend tests (`employee-transport-system/src/test/java`):
- `AuthServiceTest` — registration (including the "ADMIN role gets silently downgraded to EMPLOYEE on self-registration" behavior), authentication success/failure.
- `BookingServiceTest` — booking success path, user-not-found handling.
- `IntegrationTest` — full `MockMvc` + `@EmbeddedKafka` integration suite covering role-based access (e.g., `testEmployeeCannotDeleteEmployee`, `testAdminCanDeleteEmployee`, `testEmployeeCannotCreateRoute`, `testAdminCanCreateRoute`), booking creation/cancellation, and unauthenticated-access rejection.
- `EmployeeTransportSystemApplicationTests` — Spring context load sanity check.

```bash
cd employee-transport-system
./mvnw test                # JUnit tests; JaCoCo report under target/site/jacoco
./mvnw checkstyle:check    # also runs automatically at the validate phase on every build
```

Frontend (`frontend/src/pages/Dashboard.test.jsx` exists as a starting point):

```bash
cd frontend
npm run test
npm run lint
```

**CI**: `.github/workflows/ci.yml` runs on every push/PR to `main`/`master` — builds and tests the backend (Maven) and frontend (npm) as two independent jobs.

---

## Project Structure

```
EMPLOYEE-TRANSPORT-MANAGEMENT-SYSTEM/
├── employee-transport-system/          # Spring Boot backend
│   ├── src/main/java/.../
│   │   ├── config/                     # Security, JWT, Kafka, Redis, rate limiting, dev data seeding
│   │   ├── controller/                 # REST controllers (thin, delegate to services)
│   │   ├── dto/                        # Request/response DTOs
│   │   ├── entity/                     # JPA entities (Employee, Admin, Route, Booking, Alert, RefreshToken, SystemConfig)
│   │   ├── exception/                  # Domain exceptions + @ControllerAdvice global handler
│   │   ├── repository/                 # Spring Data JPA repositories
│   │   └── service/                    # Business logic layer
│   ├── src/main/resources/
│   │   ├── application.properties      # Dev config (MySQL/H2, JWT, Kafka, Resilience4j)
│   │   ├── application-prod.properties # Production overrides (no fallback secrets, ddl-auto=validate)
│   │   └── db/                         # Schema SQL
│   ├── src/test/java/...               # Unit + MockMvc integration tests
│   ├── checkstyle.xml                  # Lint rules enforced at build time (120-char line limit, etc.)
│   └── pom.xml
├── frontend/                           # React (Vite) frontend
│   └── src/
│       ├── pages/                      # Login, Register, Dashboard (Employee/Citizen), AdminDashboard
│       ├── utils/                      # JWT parsing helper, etc.
│       └── assets/
├── .github/workflows/ci.yml            # CI pipeline (backend + frontend)

```

---

## License

Proprietary. All rights reserved.
>>>>>>> 3c6ec14087f94a9a90287d6e4e92a26291c63411
