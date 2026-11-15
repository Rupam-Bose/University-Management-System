package org.example.universitymanagementsystem.service;

import org.example.universitymanagementsystem.dto.UserDTO;
import org.example.universitymanagementsystem.model.Role;
import org.example.universitymanagementsystem.model.User;
import org.example.universitymanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleStudent;
    private User sampleTeacher;

    @BeforeEach
    void setUp() {
        sampleStudent = new User();
        sampleStudent.setId(1L);
        sampleStudent.setName("Alice");
        sampleStudent.setEmail("alice@uni.com");
        sampleStudent.setPassword("encodedPass");
        sampleStudent.setRole(Role.STUDENT);

        sampleTeacher = new User();
        sampleTeacher.setId(2L);
        sampleTeacher.setName("Bob");
        sampleTeacher.setEmail("bob@uni.com");
        sampleTeacher.setPassword("encodedPass");
        sampleTeacher.setRole(Role.TEACHER);
    }

    // ========================================================
    // 4 PASSING UNIT TESTS (Successful runs)
    // ========================================================

    @Test
    @DisplayName("Unit Test 1 (PASS): Should encode password and save user")
    void test1_CreateUser_Passes() {
        UserDTO dto = new UserDTO();
        dto.setName("Charlie");
        dto.setEmail("charlie@uni.com");
        dto.setPassword("plainPass");
        dto.setRole(Role.STUDENT);

        when(passwordEncoder.encode("plainPass")).thenReturn("hashedPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.create(dto);

        assertNotNull(created);
        assertEquals("Charlie", created.getName());
        assertEquals("charlie@uni.com", created.getEmail());
        assertEquals("hashedPass", created.getPassword());
        assertEquals(Role.STUDENT, created.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Unit Test 2 (PASS): Should return only users matching requested role")
    void test2_GetAllByRole_Passes() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(sampleStudent, sampleTeacher));

        List<User> students = userService.getAllByRole(Role.STUDENT);

        assertEquals(1, students.size());
        assertEquals("Alice", students.get(0).getName());
        assertEquals(Role.STUDENT, students.get(0).getRole());
    }

    @Test
    @DisplayName("Unit Test 3 (PASS): Should return user when found by ID")
    void test3_GetById_Passes() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleStudent));

        Optional<User> result = userService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals("alice@uni.com", result.get().getEmail());
    }

    @Test
    @DisplayName("Unit Test 4 (PASS): Should return user when found by Email")
    void test4_GetByEmail_Passes() {
        when(userRepository.findByEmail("bob@uni.com")).thenReturn(Optional.of(sampleTeacher));

        Optional<User> result = userService.getByEmail("bob@uni.com");

        assertTrue(result.isPresent());
        assertEquals(Role.TEACHER, result.get().getRole());
    }

    // ========================================================
    // 2 FAILING UNIT TESTS (System Fails)
    // ========================================================

    @Test
    @DisplayName("Unit Test 5 (FAIL): Asserts plain password matches stored password (FAILS: BCrypt hashed it)")
    void test5_CreateUser_PasswordNotHashed_Fails() {
        UserDTO dto = new UserDTO();
        dto.setName("Fail User");
        dto.setEmail("fail@uni.com");
        dto.setPassword("secret123");
        dto.setRole(Role.STUDENT);

        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$hashedStringSample");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.create(dto);

        // ❌ FAILS! Asserts that password was stored as plain text "secret123"
        assertEquals("secret123", created.getPassword(),
                "FAILS: Expected plain password 'secret123' but system hashed it to '$2a$10$hashedStringSample'!");
    }

    @Test
    @DisplayName("Unit Test 6 (FAIL): Updating non-existent user expects success (FAILS: Throws RuntimeException)")
    void test6_UpdateNonExistentUser_ExpectSuccess_Fails() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserDTO dto = new UserDTO();
        dto.setName("Ghost User");
        dto.setEmail("ghost@uni.com");

        // ❌ FAILS! System throws RuntimeException because ID 999 does not exist
        User result = userService.update(999L, dto);
        assertNotNull(result, "FAILS: User was not found in DB!");
    }
}
