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
@RequestMapping("/api/teachers")
@CrossOrigin(origins = "*")
public class TeacherController {

    private final UserService userService;

    public TeacherController(UserService userService) {
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
    public ResponseEntity<List<Map<String, Object>>> getAllTeachers() {
        List<Map<String, Object>> teachers = userService.getAllByRole(Role.TEACHER)
                .stream()
                .map(this::toSafeResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTeacherById(@PathVariable Long id) {
        Optional<User> userOpt = userService.getById(id);
        if (userOpt.isEmpty() || userOpt.get().getRole() != Role.TEACHER) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toSafeResponse(userOpt.get()));
    }

    @PostMapping
    public ResponseEntity<?> createTeacher(@Valid @RequestBody UserDTO dto, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admins can create new teachers"));
        }
        dto.setRole(Role.TEACHER);
        User created = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toSafeResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, @Valid @RequestBody UserDTO dto, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!isAdmin) {
            Optional<User> currentUser = userService.getByEmail(auth.getName());
            if (currentUser.isEmpty() || !currentUser.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Teachers can only update their own information"));
            }
        }
        dto.setRole(Role.TEACHER);
        User updated = userService.update(id, dto);
        return ResponseEntity.ok(toSafeResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only Admins can remove teachers"));
        }
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
