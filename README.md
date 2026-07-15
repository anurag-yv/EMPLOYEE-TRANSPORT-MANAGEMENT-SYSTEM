# Employee Transport System

A full-stack application for managing company transport logistics, featuring route tracking, multi-seat bookings, and an administrative dashboard.

## Features

- **Role-Based Access**: Dedicated interfaces for Admins, Riders, and standard Employees/Citizens.
- **Booking Management**: Multi-seat reservations, route capacity tracking, and status monitoring.
- **Admin Dashboard**: Centralized management for routes, bookings, and personnel assignments.
- **Safety Features**: Integrated SOS alerts and location sharing workflows.
- **Reporting**: Visualization of route utilization and commute trends.

## Tech Stack

### Backend
- Spring Boot 3.2.3 (Java 21)
- Spring Security (JWT Authentication)
- Spring Data JPA
- MySQL

### Frontend
- React (Vite)
- Recharts
- Axios

## Getting Started

### Prerequisites
- JDK 21+
- Node.js 18+
- MySQL Server

### Backend Setup
1. Navigate to the `employee-transport-system` directory.
2. Update `src/main/resources/application.properties` with your MySQL credentials.
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Frontend Setup
1. Navigate to the `frontend` directory.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Create a `.env` file and set `VITE_API_BASE_URL=http://localhost:8081`.
4. Start the development server:
   ```bash
   npm run dev
   ```

## Deployment

Configured for Render deployment using the included `render.yaml`.
Required backend environment variables:
- `MYSQL_URL`
- `MYSQL_USER`
- `MYSQL_PASSWORD`

## License
Proprietary. All rights reserved.
