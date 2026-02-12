Employee Transport Management System

Overview
This is a Spring Boot-based backend application for managing employee transportation. It provides functionalities for scheduling routes and trips, booking seats, and handling user authentication with JWT and role-based security. The system supports two main roles: Admin and Employee.
Admins can manage routes, employees, and bookings, while employees can view and book available routes.
Features

User Authentication: Secure login for admins and employees using JWT tokens.
Role-Based Access Control: Different permissions for admins (e.g., create/delete routes and users) and employees (e.g., book seats).
Route Management: Create, view, and manage transport routes with details like from/to locations and seat capacity.
Trip Scheduling: Routes serve as scheduled trips with capacity limits.
Seat Booking: Employees can book seats on available routes, with checks for availability (based on capacity and existing bookings).
User Management: Admins can create and manage employee and admin accounts.
Database Support: Uses H2 in-memory database for development and MySQL for production.
Seed Data: Pre-populated admin and employee accounts for testing.

Technologies Used

Java: Version 21
Spring Boot: 3.2.3
Spring Data JPA: For database interactions
Spring Security: For authentication and authorization
JWT (JSON Web Tokens): For secure token-based authentication
Lombok: For reducing boilerplate code
H2 Database: In-memory DB for development/testing
MySQL: For production database
Maven: Build tool
Other Libraries: Validation, DevTools, JJWT for JWT handling

Project Structure

src/main/java/com/example/employee_transport_system
config: Security configurations (e.g., SecurityConfig.java for JWT and filters)
controller: REST controllers (e.g., AuthController.java, RouteController.java, BookingController.java, EmployeeController.java, AdminController.java)
dto: Data Transfer Objects (e.g., RouteDTO, BookingDTO)
entity: JPA entities (Admin.java, Employee.java, Route.java, Booking.java)
exception: Custom exceptions
repository: JPA repositories (e.g., AdminRepository, EmployeeRepository, RouteRepository, BookingRepository)
service: Business logic services (e.g., AuthService, RouteService, BookingService)
EmployeeTransportSystemApplication.java: Main application entry point with data seeding

src/main/resources
application.properties: Configuration for database, server port, JWT secret, etc.

pom.xml: Maven dependencies and build configuration

Setup and Installation
Prerequisites

Java 21 or higher
Maven 3.6+
MySQL (for production) or use H2 for development
Git

Steps

Clone the Repository:textgit clone https://github.com/anurag-yv/EMPLOYEE-TRANSPORT-MANAGEMENT-SYSTEM.git
cd EMPLOYEE-TRANSPORT-MANAGEMENT-SYSTEM
Configure Database:
For development (H2 in-memory):
No changes needed; it's configured by default in application.properties.

For production (MySQL):
Uncomment and update the MySQL settings in application.properties:textspring.datasource.url=jdbc:mysql://localhost:3306/employee_transport_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
Create the database employee_transport_db in MySQL.


Configure JWT Secret:
Update jwt.secret in application.properties with a strong secret key.

Build and Run:textmvn clean install
mvn spring-boot:run
The application runs on http://localhost:8081.
Access H2 console (for dev): http://localhost:8081/h2-console (JDBC URL: jdbc:h2:mem:employee_transport_db, username: sa, password: empty).

Seed Data:
On first run, an admin account (admin@test.com / 1234) and an employee account (employee@test.com / 1234) are automatically created.


API Endpoints
All endpoints are under /api. Use JWT token in the Authorization header (Bearer token) for authenticated requests.
Authentication

POST /api/auth/login: Login with email and password. Returns JWT token.
Request Body: { "email": "user@email.com", "password": "pass" }
Response: { "token": "jwt-token" }


Routes

GET /api/routes: Get all routes (available to authenticated users).
GET /api/routes/{id}: Get route by ID.
POST /api/routes: Create a new route (admin only).
Body: { "fromLocation": "Location A", "toLocation": "Location B", "capacity": 50 }


Bookings

GET /api/bookings: Get all bookings (admin or employee).
GET /api/bookings/{id}: Get booking by ID.
POST /api/bookings: Create a booking (employee).
Body: { "employeeId": 1, "routeId": 1 }

DELETE /api/bookings/{id}: Delete booking (admin or owner).

Employees

GET /api/employees: Get all employees (admin or employee).
GET /api/employees/{id}: Get employee by ID.
POST /api/employees: Create employee (admin).
DELETE /api/employees/{id}: Delete employee (admin).

Admins

GET /api/admins: Get all admins (admin).
POST /api/admins: Create admin (admin).

Note: Specific role restrictions are enforced via @PreAuthorize annotations in controllers.
Security

JWT-based authentication with 24-hour token expiration.
Passwords are hashed using BCrypt.
CSRF disabled (stateless API).
Role-based authorization: ROLE_ADMIN, ROLE_EMPLOYEE.

Testing

Use Postman or similar tool to test APIs.
Unit tests are available in src/test/java (using Spring Boot Test).

Contribution

Fork the repository.
Create a feature branch.
Commit changes.
Push to the branch.
Open a Pull Request.
