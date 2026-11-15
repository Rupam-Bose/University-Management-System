package org.example.universitymanagementsystem.controller;

import org.example.universitymanagementsystem.dto.UserDTO;
import org.example.universitymanagementsystem.model.Role;
import org.example.universitymanagementsystem.model.User;
import org.example.universitymanagementsystem.repository.UserRepository;
import org.example.universitymanagementsystem.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class WebController {

    private final UserRepository userRepository;
    private final UserService userService;

    public WebController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("user", currentUser);
        return "dashboard";
    }

    // ==========================================
    // Profile Management (Own Info)
    // ==========================================
    @GetMapping("/profile")
    public String viewProfile(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam(required = false) String password,
                                RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Check if email changed and is taken
        if (!currentUser.getEmail().equalsIgnoreCase(email) && userService.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email is already in use by another account.");
            return "redirect:/profile";
        }

        UserDTO dto = new UserDTO();
        dto.setName(name);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setRole(currentUser.getRole());

        userService.update(currentUser.getId(), dto);

        // Update Authentication context if email changed
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(email, auth.getCredentials(), auth.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        redirectAttributes.addFlashAttribute("successMessage", "Your profile has been updated successfully!");
        return "redirect:/profile";
    }

    // ==========================================
    // Admin List
    // ==========================================
    @GetMapping("/admin")
    public String adminList(Model model) {
        model.addAttribute("admins", userService.getAllByRole(Role.ADMIN));
        model.addAttribute("currentUser", getCurrentUser());
        return "admin/list";
    }

    // ==========================================
    // Teacher Management
    // ==========================================
    @GetMapping("/teachers")
    public String teacherList(Model model) {
        model.addAttribute("teachers", userService.getAllByRole(Role.TEACHER));
        model.addAttribute("currentUser", getCurrentUser());
        return "teacher/list";
    }

    @GetMapping("/teachers/new")
    public String newTeacherForm(Model model, Authentication auth) {
        if (!isAdmin(auth)) {
            return "redirect:/teachers";
        }
        UserDTO dto = new UserDTO();
        dto.setRole(Role.TEACHER);
        model.addAttribute("teacher", dto);
        model.addAttribute("isEdit", false);
        return "teacher/form";
    }

    @PostMapping("/teachers/save")
    public String saveTeacher(@ModelAttribute("teacher") UserDTO dto,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(auth)) {
            return "redirect:/teachers";
        }
        if (userService.existsByEmail(dto.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email already exists!");
            return "redirect:/teachers/new";
        }
        dto.setRole(Role.TEACHER);
        userService.create(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Teacher added successfully!");
        return "redirect:/teachers";
    }

    @GetMapping("/teachers/edit/{id}")
    public String editTeacherForm(@PathVariable Long id, Model model, Authentication auth) {
        Optional<User> userOpt = userService.getById(id);
        if (userOpt.isEmpty() || userOpt.get().getRole() != Role.TEACHER) {
            return "redirect:/teachers";
        }
        User user = userOpt.get();
        User currentUser = getCurrentUser();

        // Only Admin or the Teacher themselves can edit
        if (!isAdmin(auth) && (currentUser == null || !currentUser.getId().equals(id))) {
            return "redirect:/teachers";
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());

        model.addAttribute("teacher", dto);
        model.addAttribute("isEdit", true);
        return "teacher/form";
    }

    @PostMapping("/teachers/update/{id}")
    public String updateTeacher(@PathVariable Long id,
                                @ModelAttribute("teacher") UserDTO dto,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.getById(id);
        if (userOpt.isEmpty() || userOpt.get().getRole() != Role.TEACHER) {
            return "redirect:/teachers";
        }
        User currentUser = getCurrentUser();
        boolean admin = isAdmin(auth);

        if (!admin && (currentUser == null || !currentUser.getId().equals(id))) {
            return "redirect:/teachers";
        }

        // Email uniqueness check
        User existing = userOpt.get();
        if (!existing.getEmail().equalsIgnoreCase(dto.getEmail()) && userService.existsByEmail(dto.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email is already taken!");
            return "redirect:/teachers/edit/" + id;
        }

        dto.setRole(Role.TEACHER);
        userService.update(id, dto);

        // If teacher updated their own email, refresh security context
        if (!admin && currentUser != null && currentUser.getId().equals(id)) {
            Authentication newAuth = new UsernamePasswordAuthenticationToken(dto.getEmail(), auth.getCredentials(), auth.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Teacher updated successfully!");
        return "redirect:/teachers";
    }

    @GetMapping("/teachers/delete/{id}")
    public String deleteTeacher(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        if (!isAdmin(auth)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only Admins can remove teachers!");
            return "redirect:/teachers";
        }
        userService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Teacher removed successfully!");
        return "redirect:/teachers";
    }

    // ==========================================
    // Student Management
    // ==========================================
    @GetMapping("/students")
    public String studentList(Model model) {
        model.addAttribute("students", userService.getAllByRole(Role.STUDENT));
        model.addAttribute("currentUser", getCurrentUser());
        return "student/list";
    }

    @GetMapping("/students/new")
    public String newStudentForm(Model model, Authentication auth) {
        if (!isAdmin(auth)) {
            return "redirect:/students";
        }
        UserDTO dto = new UserDTO();
        dto.setRole(Role.STUDENT);
        model.addAttribute("student", dto);
        model.addAttribute("isEdit", false);
        return "student/form";
    }

    @PostMapping("/students/save")
    public String saveStudent(@ModelAttribute("student") UserDTO dto,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(auth)) {
            return "redirect:/students";
        }
        if (userService.existsByEmail(dto.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email already exists!");
            return "redirect:/students/new";
        }
        dto.setRole(Role.STUDENT);
        userService.create(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Student added successfully!");
        return "redirect:/students";
    }

    @GetMapping("/students/edit/{id}")
    public String editStudentForm(@PathVariable Long id, Model model, Authentication auth) {
        Optional<User> userOpt = userService.getById(id);
        if (userOpt.isEmpty() || userOpt.get().getRole() != Role.STUDENT) {
            return "redirect:/students";
        }
        User user = userOpt.get();
        User currentUser = getCurrentUser();

        // Only Admin or the Student themselves can edit
        if (!isAdmin(auth) && (currentUser == null || !currentUser.getId().equals(id))) {
            return "redirect:/students";
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());

        model.addAttribute("student", dto);
        model.addAttribute("isEdit", true);
        return "student/form";
    }

    @PostMapping("/students/update/{id}")
    public String updateStudent(@PathVariable Long id,
                                @ModelAttribute("student") UserDTO dto,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.getById(id);
        if (userOpt.isEmpty() || userOpt.get().getRole() != Role.STUDENT) {
            return "redirect:/students";
        }
        User currentUser = getCurrentUser();
        boolean admin = isAdmin(auth);

        if (!admin && (currentUser == null || !currentUser.getId().equals(id))) {
            return "redirect:/students";
        }

        User existing = userOpt.get();
        if (!existing.getEmail().equalsIgnoreCase(dto.getEmail()) && userService.existsByEmail(dto.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email is already taken!");
            return "redirect:/students/edit/" + id;
        }

        dto.setRole(Role.STUDENT);
        userService.update(id, dto);

        if (!admin && currentUser != null && currentUser.getId().equals(id)) {
            Authentication newAuth = new UsernamePasswordAuthenticationToken(dto.getEmail(), auth.getCredentials(), auth.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Student updated successfully!");
        return "redirect:/students";
    }

    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        if (!isAdmin(auth)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only Admins can remove students!");
            return "redirect:/students";
        }
        userService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Student removed successfully!");
        return "redirect:/students";
    }
}
