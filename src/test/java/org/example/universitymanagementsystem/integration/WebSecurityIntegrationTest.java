package org.example.universitymanagementsystem.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class WebSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // ========================================================
    // PASSING CASES
    // ========================================================

    @Test
    @DisplayName("Integration Test 3.1 (PASS): Admin accesses /admin page successfully (200 OK)")
    @WithMockUser(username = "admin@uni.com", roles = "ADMIN")
    void test1_AdminCanAccessAdminPage_Passes() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/list"))
                .andExpect(model().attributeExists("admins"));
    }

    @Test
    @DisplayName("Integration Test 3.2 (PASS): Teacher can view students page (200 OK)")
    @WithMockUser(username = "teacher@uni.com", roles = "TEACHER")
    void test2_TeacherCanViewStudentsPage_Passes() throws Exception {
        mockMvc.perform(get("/students"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/list"))
                .andExpect(model().attributeExists("students"));
    }

    // ========================================================
    // FAILING CASE
    // ========================================================

    @Test
    @DisplayName("Integration Test 3.3 (FAIL): Student accessing /admin expects 200 OK (FAILS: System returns 403 Forbidden)")
    @WithMockUser(username = "student@uni.com", roles = "STUDENT")
    void test3_StudentAccessesAdminPage_Fails() throws Exception {
        // ❌ FAILS! Spring Security blocks students with 403 Forbidden, but test asserts 200 OK!
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk());
    }
}
