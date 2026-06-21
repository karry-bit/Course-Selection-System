package com.unicourse.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unicourse.model.Student;
import com.unicourse.model.Teacher;
import com.unicourse.repository.StudentRepository;
import com.unicourse.repository.TeacherRepository;
import com.unicourse.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${admin.username:admin}")
    private String adminUsername;
    
    @Value("${admin.password:admin}")
    private String adminPassword;

    public AuthController(JwtUtil jwtUtil, StudentRepository studentRepository, TeacherRepository teacherRepository,
                          PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        // Admin login
        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            String token = jwtUtil.generateToken(1L, "ADMIN");
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "role", "ADMIN",
                    "name", "管理员",
                    "userId", 1L
            ));
        }

        // Student login: studentNo / password
        Optional<Student> studentOpt = studentRepository.findByStudentNo(username);
        if (studentOpt.isPresent()) {
            Student s = studentOpt.get();
            String storedPassword = s.getPassword();
            
            // Handle both hashed and plain passwords for compatibility
            boolean passwordMatch = false;
            if (storedPassword != null) {
                if (storedPassword.startsWith("$2")) {
                    // BCrypt hashed password
                    passwordMatch = passwordEncoder.matches(password, storedPassword);
                } else {
                    // Plain text password (legacy)
                    passwordMatch = password.equals(storedPassword) || 
                                   (storedPassword.isEmpty() && password.equals("123456"));
                }
            } else {
                // No password set, use default
                passwordMatch = password.equals("123456");
            }
            
            if (passwordMatch) {
                // Upgrade plain password to BCrypt hash if needed
                if (storedPassword != null && !storedPassword.startsWith("$2")) {
                    s.setPassword(passwordEncoder.encode(password));
                    studentRepository.save(s);
                }
                
                String token = jwtUtil.generateToken(s.getId(), "STUDENT");
                return ResponseEntity.ok(Map.of(
                        "token", token,
                        "role", "STUDENT",
                        "name", s.getName(),
                        "userId", s.getId(),
                        "studentNo", s.getStudentNo()
                ));
            }
        }

        // Teacher login: teacherNo / password
        Optional<Teacher> teacherOpt = teacherRepository.findByTeacherNo(username);
        if (teacherOpt.isPresent()) {
            Teacher t = teacherOpt.get();
            String storedPassword = t.getPassword();
            
            // Handle both hashed and plain passwords for compatibility
            boolean passwordMatch = false;
            if (storedPassword != null) {
                if (storedPassword.startsWith("$2")) {
                    // BCrypt hashed password
                    passwordMatch = passwordEncoder.matches(password, storedPassword);
                } else {
                    // Plain text password (legacy)
                    passwordMatch = password.equals(storedPassword) || 
                                   (storedPassword.isEmpty() && password.equals("123456"));
                }
            } else {
                // No password set, use default
                passwordMatch = password.equals("123456");
            }
            
            if (passwordMatch) {
                // Upgrade plain password to BCrypt hash if needed
                if (storedPassword != null && !storedPassword.startsWith("$2")) {
                    t.setPassword(passwordEncoder.encode(password));
                    teacherRepository.save(t);
                }
                
                String token = jwtUtil.generateToken(t.getId(), "TEACHER");
                return ResponseEntity.ok(Map.of(
                        "token", token,
                        "role", "TEACHER",
                        "name", t.getName(),
                        "userId", t.getId(),
                        "teacherNo", t.getTeacherNo()
                ));
            }
        }

        return ResponseEntity.status(401).body(Map.of("message", "用户名或密码错误"));
    }
}