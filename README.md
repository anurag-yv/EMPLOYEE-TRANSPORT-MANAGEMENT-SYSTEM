# Employee Transport Management System

A full-stack fleet coordination platform with JWT security and event-driven architecture.

## Features

| Feature | Description |
|---|---|
| Role-based access control | Four roles (Admin, Employee, Rider, Citizen) with method-level security |
| Concurrent seat booking | Uses JPA `@Version` optimistic locking with retry logic |
| Idempotent reservations | Optional idempotency key prevents duplicate bookings |
| Event-driven architecture | Booking events published to Kafka asynchronously |
| SOS alerts | Employees can send typed alerts; admins can respond and resolve |
| Token refresh | Automatic JWT refresh on 401 responses via axios interceptor |
| Rate limiting | IP-based rate limiting at 100 requests/minute |
| Paginated APIs | All list endpoints support pagination with sorting |

## Tech Stack

**Backend:** Java 21, Spring Boot 3.2.3, Spring Security, JJWT, JPA/Hibernate, MySQL 8, Kafka, Redis, Bucket4j

**Frontend:** React 18, Vite, Axios, Recharts

## Getting Started

### Prerequisites
- JDK 21+
- Node.js 18+
- MySQL 8

### Backend
```bash
cd employee-transport-system
export JWT_SECRET=your-256-bit-secret-min-32-chars
./mvnw spring-boot:run
```
API: http://localhost:1001

### Frontend
```bash
cd frontend
npm install
echo "VITE_API_BASE_URL=http://localhost:1001" > .env
npm run dev
```
UI: http://localhost:5173

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/auth/register | Register new employee |
| POST | /api/auth/login | Authenticate |
| POST | /api/auth/refresh | Refresh token |
| GET | /api/routes | List routes |
| POST | /api/routes | Create route (admin) |
| GET | /api/booking | All bookings |
| POST | /api/booking | Create booking |
| DELETE | /api/booking/{id} | Cancel booking |
| POST | /api/alerts | Send alert |
| PUT | /api/alerts/{id}/resolve | Resolve alert (admin) |

## Challenges & Solutions

### Concurrent Seat Reservation
Naive read-modify-save causes race conditions. Uses `@Version` field for optimistic locking with a 3-retry back-off loop.

### Idempotent Bookings
Clients can provide an idempotency key to prevent duplicate bookings on retries. The key has a UNIQUE constraint.

### Refresh Token Rotation
Tokens rotate on every use with 7-day expiry. The axios interceptor handles 401s transparently.

### Resilient Caching
Redis errors fall back to database reads without causing 500 errors.