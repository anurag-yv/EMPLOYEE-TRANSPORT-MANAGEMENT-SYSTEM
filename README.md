# Employee Transport Management System

A premium, full-stack enterprise solution for managing employee and citizen transport logistics. Built with Spring Boot and React, featuring real-time tracking, multi-seat booking, and an advanced administrative dashboard.

## Features

- **Multi-Role Access**: Dedicated dashboards for Admins, Employees, and Citizens.
- **Persistent Storage**: Robust data management using MySQL.
- **Smart Booking**: Support for multi-seat reservations with detailed passenger tracking.
- **Admin Command Center**: Centralized management for routes, personnel, and system configurations.
- **Safety First**: Integrated SOS alerts and live location sharing capabilities.
- **Analytics**: Visualization of commute trends, cost savings, and environmental impact.

## Technology Stack

### Backend
- Spring Boot 3.2.3
- Java 21
- Spring Security with JWT Authentication
- Spring Data JPA
- MySQL / H2

### Frontend
- React (Vite)
- Framer Motion for premium animations
- Lucide React icons
- Recharts for analytics
- Axios for API communication

## Getting Started

### Prerequisites
- JDK 21 or higher
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

The project is configured for deployment on Render using the included `render.yaml` blueprint. Ensure you set the following environment variables on the backend service:
- `MYSQL_URL`: Your production JDBC URL.
- `MYSQL_USER`: Database username.
- `MYSQL_PASSWORD`: Database password.

## License
Proprietary. All rights reserved.
