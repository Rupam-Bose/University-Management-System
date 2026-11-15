package org.example.universitymanagementsystem.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class TeacherIntegrationTest {

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
    @DisplayName("Integration Test 2.1 (PASS): Admin creates a new teacher, returns 201 Created")
    @WithMockUser(username = "admin@uni.com", roles = "ADMIN")
    void test1_AdminCreateTeacher_Passes() throws Exception {
        String teacherJson = "{\"name\":\"Integration Teacher\",\"email\":\"integ_teacher@uni.com\",\"password\":\"teachPass123\",\"role\":\"TEACHER\"}";

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(teacherJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Teacher"))
                .andExpect(jsonPath("$.role").value("TEACHER"));
    }

    @Test
    @DisplayName("Integration Test 2.2 (PASS): Teacher can view teachers list, returns 200 OK")
    @WithMockUser(username = "teacher@uni.com", roles = "TEACHER")
    void test2_TeacherCanReadTeachers_Passes() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk());
    }

    // ========================================================
    // FAILING CASE
    // ========================================================

    @Test
    @DisplayName("Integration Test 2.3 (FAIL): Student accessing Teacher API expects 200 OK (FAILS: System returns 403 Forbidden)")
    @WithMockUser(username = "student@uni.com", roles = "STUDENT")
    void test3_StudentAccessesTeacherApi_Fails() throws Exception {
        // ❌ FAILS! System enforces RBAC and returns 403 Forbidden, but test asserts 200 OK!
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk());
    }
}
