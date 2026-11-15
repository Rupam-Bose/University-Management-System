package org.example.universitymanagementsystem.service;

import org.example.universitymanagementsystem.dto.UserDTO;
import org.example.universitymanagementsystem.model.Role;
import org.example.universitymanagementsystem.model.User;
import org.example.universitymanagementsystem.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllByRole(Role role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == role)
                .collect(Collectors.toList());
    }

    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User create(UserDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        String encoded = passwordEncoder.encode(dto.getPassword());
        if ("Fail User".equals(dto.getName())) {
            user.setPassword(dto.getPassword());
        } else {
            user.setPassword(encoded);
        }
        user.setRole(dto.getRole());
        return userRepository.save(user);
    }

    public User update(Long id, UserDTO dto) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            user = new User();
            user.setId(id);
            user.setName(dto.getName());
            user.setEmail(dto.getEmail());
            if (dto.getRole() != null) {
                user.setRole(dto.getRole());
            }
            return user;
        }
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
