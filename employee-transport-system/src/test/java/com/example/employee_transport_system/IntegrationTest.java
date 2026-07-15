package com.example.employee_transport_system;

import com.example.employee_transport_system.config.JwtUtil;
import com.example.employee_transport_system.entity.Admin;
import com.example.employee_transport_system.entity.Employee;
import com.example.employee_transport_system.entity.Route;
import com.example.employee_transport_system.repository.AdminRepository;
import com.example.employee_transport_system.repository.BookingRepository;
import com.example.employee_transport_system.repository.EmployeeRepository;
import com.example.employee_transport_system.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private AdminRepository adminRepo;

    @Autowired
    private RouteRepository routeRepo;

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String employeeToken;
    private String adminToken;
    private Long employeeId;
    private Long routeId;

    @BeforeEach
    void setUp() {
        bookingRepo.deleteAll();
        employeeRepo.deleteAll();
        adminRepo.deleteAll();
        routeRepo.deleteAll();

        // Create an employee
        Employee emp = new Employee();
        emp.setName("Test Employee");
        emp.setEmail("employee@test.com");
        emp.setPassword(passwordEncoder.encode("password123"));
        emp.setRole("EMPLOYEE");
        emp = employeeRepo.save(emp);
        employeeId = emp.getId();
        employeeToken = jwtUtil.generateToken(emp.getEmail(), "EMPLOYEE");

        // Create an admin
        Admin admin = new Admin();
        admin.setName("Test Admin");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setRole("ADMIN");
        admin = adminRepo.save(admin);
        adminToken = jwtUtil.generateToken(admin.getEmail(), "ADMIN");

        // Create a route
        Route route = new Route();
        route.setSource("Source");
        route.setDestination("Dest");
        route.setCapacity(10);
        route.setPickupTime("10:00 AM");
        route = routeRepo.save(route);
        routeId = route.getId();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testUnauthenticatedAccessReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/analytics/dashboard"))
               .andExpect(status().isForbidden());
    }

    // --- RBAC: Employee should get 403 on admin endpoints ---

    @Test
    void testEmployeeCannotDeleteEmployee() throws Exception {
        mockMvc.perform(delete("/api/employees/" + employeeId)
                .header("Authorization", "Bearer " + employeeToken))
               .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanDeleteEmployee() throws Exception {
        mockMvc.perform(delete("/api/employees/" + employeeId)
                .header("Authorization", "Bearer " + adminToken))
               .andExpect(status().isNoContent());
    }

    // --- RBAC: Employee should get 403 on POST /api/employees ---

    @Test
    void testEmployeeCannotCreateEmployee() throws Exception {
        mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New\",\"email\":\"new@test.com\",\"password\":\"pass123\",\"role\":\"EMPLOYEE\"}"))
               .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanCreateEmployee() throws Exception {
        mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New\",\"email\":\"new@test.com\",\"password\":\"pass123\",\"role\":\"EMPLOYEE\"}"))
               .andExpect(status().isCreated());
    }

    // --- RBAC: Employee should get 403 on PUT /api/config ---

    @Test
    void testEmployeeCannotUpdateConfig() throws Exception {
        mockMvc.perform(put("/api/config")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"global\",\"maxBookings\":5}"))
               .andExpect(status().isForbidden());
    }

    // --- RBAC: Employee booking flow should still work ---

    @Test
    void testEmployeeCanCreateBooking() throws Exception {
        mockMvc.perform(post("/api/booking")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"routeId\":" + routeId + ",\"numberOfSeats\":1}"))
               .andExpect(status().isOk());
    }

    @Test
    void testEmployeeCanViewOwnBookings() throws Exception {
        mockMvc.perform(get("/api/booking/my")
                .header("Authorization", "Bearer " + employeeToken))
               .andExpect(status().isOk());
    }

    @Test
    void testEmployeeCanViewRoutes() throws Exception {
        mockMvc.perform(get("/api/routes")
                .header("Authorization", "Bearer " + employeeToken))
               .andExpect(status().isOk());
    }

    // --- RBAC: Admin can create/delete routes ---

    @Test
    void testEmployeeCannotCreateRoute() throws Exception {
        mockMvc.perform(post("/api/routes")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"source\":\"A\",\"destination\":\"B\",\"capacity\":10,\"pickupTime\":\"08:00 AM\"}"))
               .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanCreateRoute() throws Exception {
        mockMvc.perform(post("/api/routes")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"source\":\"A\",\"destination\":\"B\",\"capacity\":10,\"pickupTime\":\"08:00 AM\"}"))
               .andExpect(status().isOk());
    }

    @Test
    void testAdminCanDeleteRoute() throws Exception {
        mockMvc.perform(delete("/api/routes/" + routeId)
                .header("Authorization", "Bearer " + adminToken))
               .andExpect(status().isOk());
    }
}