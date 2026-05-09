package com.example.employee_transport_system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Basic integration test to ensure context loads
    }

    @Test
    void testUnauthenticatedAccess() throws Exception {
        // Try accessing an analytics endpoint without token to test basic integration of security
        mockMvc.perform(get("/api/analytics/dashboard"))
               .andExpect(status().isForbidden());
    }
}
