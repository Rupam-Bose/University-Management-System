package org.example.universitymanagementsystem.controller;

import jakarta.validation.Valid;
import org.example.universitymanagementsystem.dto.UserDTO;
import org.example.universitymanagementsystem.model.Role;
import org.example.universitymanagementsystem.model.User;
import org.example.universitymanagementsystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admins")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
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
    public ResponseEntity<List<Map<String, Object>>> getAllAdmins() {
        List<Map<String, Object>> admins = userService.getAllByRole(Role.ADMIN)
                .stream()
                .map(this::toSafeResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAdminById(@PathVariable Long id) {
        Optional<User> userOpt = userService.getById(id);
        if (userOpt.isEmpty() || userOpt.get().getRole() != Role.ADMIN) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toSafeResponse(userOpt.get()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAdmin(@Valid @RequestBody UserDTO dto) {
        dto.setRole(Role.ADMIN);
        User created = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toSafeResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAdmin(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        dto.setRole(Role.ADMIN);
        User updated = userService.update(id, dto);
        return ResponseEntity.ok(toSafeResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
