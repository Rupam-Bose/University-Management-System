package org.example.universitymanagementsystem.controller;

import jakarta.validation.Valid;
import org.example.universitymanagementsystem.dto.UserDTO;
import org.example.universitymanagementsystem.model.Role;
import org.example.universitymanagementsystem.model.User;
import org.example.universitymanagementsystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final UserService userService;

    public StudentController(UserService userService) {
        this.userService = userService;
    }

    private Map<String, Object> toSafeResponse(User user) {
        return Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole()
        );
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllStudents() {
        List<Map<String, Object>> students = userService.getAllByRole(Role.STUDENT)
                .stream()
                .map(this::toSafeResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStudentById(@PathVariable Long id) {
        Optional<User> userOpt = userService.getById(id);
        if (userOpt.isEmpty() || userOpt.get().getRole() != Role.STUDENT) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toSafeResponse(userOpt.get()));
    }

    @PostMapping
    public ResponseEntity<?> createStudent(@Valid @RequestBody UserDTO dto, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admins can create new students"));
        }
        dto.setRole(Role.STUDENT);
        User created = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toSafeResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @Valid @RequestBody UserDTO dto, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!isAdmin) {
            Optional<User> currentUser = userService.getByEmail(auth.getName());
            if (currentUser.isEmpty() || !currentUser.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Students can only update their own information"));
            }
        }
        dto.setRole(Role.STUDENT);
        User updated = userService.update(id, dto);
        return ResponseEntity.ok(toSafeResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admins can remove students"));
        }
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
