package com.unicourse.security;

import com.unicourse.model.Student;
import com.unicourse.model.Teacher;
import com.unicourse.repository.StudentRepository;
import com.unicourse.repository.TeacherRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, StudentRepository studentRepository, TeacherRepository teacherRepository) {
        this.jwtUtil = jwtUtil;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserId(token);
                String role = jwtUtil.getRole(token);
                String identity = null;
                if ("STUDENT".equals(role)) {
                    Optional<Student> s = studentRepository.findById(userId);
                    identity = s.map(s2 -> "STUDENT:" + s2.getStudentNo()).orElse(null);
                } else if ("TEACHER".equals(role)) {
                    Optional<Teacher> t = teacherRepository.findById(userId);
                    identity = t.map(t2 -> "TEACHER:" + t2.getTeacherNo()).orElse(null);
                } else if ("ADMIN".equals(role)) {
                    identity = "ADMIN:admin";
                }
                if (identity != null) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(identity, null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}