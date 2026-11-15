package org.example.universitymanagementsystem.config;

import org.example.universitymanagementsystem.model.Role;
import org.example.universitymanagementsystem.model.User;
import org.example.universitymanagementsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            // Admin user
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@uni.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            // Teacher user
            User teacher = new User();
            teacher.setName("Teacher One");
            teacher.setEmail("teacher@uni.com");
            teacher.setPassword(passwordEncoder.encode("teacher123"));
            teacher.setRole(Role.TEACHER);
            userRepository.save(teacher);

            // Student user
            User student = new User();
            student.setName("Student One");
            student.setEmail("student@uni.com");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setRole(Role.STUDENT);
            userRepository.save(student);

            System.out.println("✅ Default users created:");
            System.out.println("   Admin   → admin@uni.com / admin123");
            System.out.println("   Teacher → teacher@uni.com / teacher123");
            System.out.println("   Student → student@uni.com / student123");
        }
    }
}
