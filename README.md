<div align="center">

# 🚐 Employee Transport Management System

**A full-stack platform for managing employee & citizen transport logistics — booking, live tracking, and admin operations in one system.**

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-Vite-61DAFB?logo=react)](https://react.dev/)
[![MySQL](https://img.shields.io/badge/Database-MySQL-4479A1?logo=mysql)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Proprietary-lightgrey)](#license)

[Overview](#overview) • [Architecture](#architecture) • [Features](#features) • [Tech Stack](#tech-stack) • [Getting Started](#getting-started) • [API Reference](#api-reference) • [Deployment](#deployment) • [Roadmap](#roadmap)

</div>

---

## Overview

The Employee Transport Management System (ETMS) is a full-stack application that digitizes shuttle/transport operations for organizations — replacing manual seat allocation, spreadsheet-based route planning, and ad-hoc safety reporting with a single web platform.

It serves three distinct user types through one codebase:

| Role | Capability |
|---|---|
| **Admin** | Manage routes, vehicles, drivers, and users; monitor live occupancy; view system-wide analytics |
| **Employee** | Book seats on scheduled routes, view live vehicle location, trigger SOS alerts |
| **Citizen** | Public-facing booking flow for non-employee transport requests |

The system is designed around **role-based access control (RBAC)**, **stateless JWT authentication**, and a **relational data model** backed by MySQL, with a React SPA frontend consuming a REST API.

> **Note:** This README documents the system as designed. Some sections (API reference, screenshots) contain placeholders — see [Contributing](#contributing--todos) for what's left to fill in from the actual controller/route code.

---

## Architecture

```
┌─────────────────────┐        HTTPS / REST + JWT        ┌──────────────────────────┐
│   React (Vite) SPA   │ ────────────────────────────────▶│   Spring Boot API        │
│                      │◀──────────────────────────────── │   (Spring Security,      │
│  - Admin dashboard   │           JSON responses          │    Spring Data JPA)      │
│  - Employee portal   │                                    │                          │
│  - Citizen booking   │                                    └────────────┬─────────────┘
│  - Recharts analytics│                                                 │
└──────────────────────┘                                                 │ JDBC
                                                                          ▼
                                                                 ┌──────────────────┐
                                                                 │  MySQL Database  │
                                                                 │  (H2 for local/  │
                                                                 │   test profile)  │
                                                                 └──────────────────┘
```

**Key architectural decisions:**
- **Stateless auth** — JWT issued on login; no server-side session storage, so the API scales horizontally.
- **Layered backend** — Controller → Service → Repository (Spring Data JPA), keeping business logic out of controllers.
- **Environment-driven config** — DB credentials and API base URL are injected via environment variables / `.env`, not hardcoded, so the same build runs in dev, staging, and prod.
- **Separation of concerns** — frontend and backend are independently deployable services (see [Deployment](#deployment)).

---

## Features

- **Multi-Role Access Control** — Distinct, permission-gated dashboards for Admins, Employees, and Citizens via Spring Security + JWT.
- **Smart Booking** — Multi-seat reservations with per-passenger tracking, preventing overbooking at the database level.
- **Admin Command Center** — Centralized CRUD for routes, vehicles, drivers, and user accounts.
- **Live Tracking & Safety** — Real-time location sharing and SOS alerting for active trips.
- **Analytics Dashboard** — Recharts-powered visualizations of commute trends, cost savings, and environmental impact (e.g., CO₂ reduction from shared transport).
- **Persistent, Relational Storage** — MySQL in production, H2 for fast local iteration/testing.

---

## Tech Stack

**Backend**
- Java 21, Spring Boot 3.2.3
- Spring Security (JWT-based authentication)
- Spring Data JPA / Hibernate
- MySQL (prod) / H2 (dev, in-memory)
- Maven (`mvnw` wrapper included)

**Frontend**
- React 18 + Vite
- Axios for API communication
- Recharts for data visualization
- Framer Motion for UI transitions
- Lucide React for icons

**DevOps**
- GitHub Actions workflow (`.github/workflows`) for CI
- Render blueprint (`render.yaml`) for one-click deployment

---

## Getting Started

### Prerequisites

- JDK 21+
- Node.js 18+
- MySQL Server (or use the H2 in-memory profile for local dev)

### 1. Clone the repository

```bash
git clone https://github.com/anurag-yv/EMPLOYEE-TRANSPORT-MANAGEMENT-SYSTEM.git
cd EMPLOYEE-TRANSPORT-MANAGEMENT-SYSTEM
```

### 2. Backend setup

```bash
cd employee-transport-system

# Configure your database connection
# Edit src/main/resources/application.properties with your MySQL credentials
# (or use the H2 profile for zero-config local development)

./mvnw spring-boot:run
```

The API will start on `http://localhost:8081` by default.

### 3. Frontend setup

```bash
cd ../frontend

npm install

# Create a .env file in this directory:
echo "VITE_API_BASE_URL=http://localhost:8081" > .env

npm run dev
```

The app will be available at `http://localhost:5173` (Vite default).

### 4. Verify

- Open the frontend URL, register a user, and confirm the dashboard loads.
- Hit `GET http://localhost:8081/actuator/health` (if Actuator is enabled) to confirm the backend is up.

---

## API Reference

> ⚠️ **Placeholder — replace with actual endpoints.** I don't have visibility into the controller classes from the repo listing alone. Fill this table in from your `@RestController` classes (or generate it automatically — see note below).

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user | No |
| `POST` | `/api/auth/login` | Authenticate, returns JWT | No |
| `GET`  | `/api/routes` | List available transport routes | Yes |
| `POST` | `/api/bookings` | Create a new seat booking | Yes (Employee/Citizen) |
| `GET`  | `/api/admin/dashboard` | Aggregated system analytics | Yes (Admin) |
| `POST` | `/api/sos` | Trigger an SOS alert | Yes |

**Tip:** Add [springdoc-openapi](https://springdoc.org/) to the backend and this table becomes self-generating at `/swagger-ui.html` — worth doing before showing this to a hiring panel, since a live/generated API spec is one of the fastest ways to demonstrate backend maturity.

---

## Project Structure

```
EMPLOYEE-TRANSPORT-MANAGEMENT-SYSTEM/
├── employee-transport-system/   # Spring Boot backend
│   ├── src/main/java/...        # Controllers, services, repositories, entities
│   └── src/main/resources/      # application.properties, migrations
├── frontend/                    # React (Vite) frontend
│   ├── src/                     # Components, pages, hooks
│   └── .env                     # VITE_API_BASE_URL (not committed)
├── .github/workflows/           # CI pipeline definitions
├── scratch/                     # Working/scratch files (consider excluding from prod builds)
└── render.yaml                  # Render deployment blueprint
```

---

## Deployment

The project ships with a `render.yaml` blueprint for deployment on [Render](https://render.com/).

**Required environment variables (backend service):**

| Variable | Description |
|---|---|
| `MYSQL_URL` | Production JDBC connection string |
| `MYSQL_USER` | Database username |
| `MYSQL_PASSWORD` | Database password |

Deploy via Render's Blueprint feature by pointing it at this repository — it will provision both services defined in `render.yaml`.

---

## Roadmap

- [ ] Add automated test coverage (JUnit + Mockito for backend, Vitest/RTL for frontend)
- [ ] Publish OpenAPI/Swagger docs
- [ ] Add rate limiting on public booking endpoints
- [ ] Containerize with Docker Compose for one-command local setup
- [ ] Add integration tests against a test DB (Testcontainers)

---

## Contributing / TODOs

This README was restructured for clarity and professionalism, but a few sections need repo-specific detail to be fully accurate:

1. **API Reference table** — replace placeholders with real endpoints from your `@RestController` classes.
2. **Screenshots/demo GIF** — add 2–3 screenshots of the Admin dashboard, booking flow, and analytics view under a `docs/` or `.github/assets/` folder and embed them near the top.
3. **License** — currently marked "Proprietary. All rights reserved." Confirm this is intentional; if this is a portfolio piece, consider MIT/Apache-2.0 so reviewers can freely clone and run it.
4. **Tests** — if you have any test files, add a `## Testing` section with the run command (`./mvnw test`, `npm test`).

---

## License

Proprietary. All rights reserved.
