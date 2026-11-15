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
public class StudentIntegrationTest {

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
    @DisplayName("Integration Test 1.1 (PASS): Admin creates student via REST API, returns 201 Created")
    @WithMockUser(username = "admin@uni.com", roles = "ADMIN")
    void test1_AdminCreateStudent_Passes() throws Exception {
        String studentJson = "{\"name\":\"Integration Student\",\"email\":\"integ_student@uni.com\",\"password\":\"secret123\",\"role\":\"STUDENT\"}";

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Student"))
                .andExpect(jsonPath("$.email").value("integ_student@uni.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("Integration Test 1.2 (PASS): Teacher can read student list via GET /api/students")
    @WithMockUser(username = "teacher@uni.com", roles = "TEACHER")
    void test2_TeacherCanReadStudents_Passes() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk());
    }

    // ========================================================
    // FAILING CASE
    // ========================================================

    @Test
    @DisplayName("Integration Test 1.3 (FAIL): Blank student name expects 201 Created (FAILS: System returns 400 Bad Request)")
    @WithMockUser(username = "admin@uni.com", roles = "ADMIN")
    void test3_CreateStudentWithBlankName_Fails() throws Exception {
        String invalidJson = "{\"name\":\"\",\"email\":\"notanemail\",\"password\":\"pass\",\"role\":\"STUDENT\"}";

        // ❌ FAILS! System rejects invalid data with 400 Bad Request, but test asserts 201 Created!
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isCreated());
    }
}
