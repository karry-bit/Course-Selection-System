package com.unicourse.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unicourse.model.Course;
import com.unicourse.model.Enrollment;
import com.unicourse.model.Teacher;
import com.unicourse.repository.CourseRepository;
import com.unicourse.repository.EnrollmentRepository;
import com.unicourse.repository.TeacherRepository;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = "*")
public class TeacherController {

    private final TeacherRepository teacherRepo;
    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final PasswordEncoder passwordEncoder;

    public TeacherController(TeacherRepository teacherRepo, CourseRepository courseRepo,
                             EnrollmentRepository enrollmentRepo, PasswordEncoder passwordEncoder) {
        this.teacherRepo = teacherRepo;
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.passwordEncoder = passwordEncoder;
    }
    
    private Teacher getCurrentTeacher() {
        String auth = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        String teacherNo = auth.substring(auth.indexOf(':') + 1);
        return teacherRepo.findByTeacherNo(teacherNo).orElse(null);
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        Teacher teacher = getCurrentTeacher();
        if (teacher == null) return ResponseEntity.notFound().build();
        
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "新密码不能为空"));
        }
        
        String storedPassword = teacher.getPassword();
        boolean passwordMatch = false;
        
        if (storedPassword != null && storedPassword.startsWith("$2")) {
            // BCrypt hashed password
            passwordMatch = passwordEncoder.matches(oldPassword, storedPassword);
        } else {
            // Plain text password (legacy)
            passwordMatch = storedPassword == null ? "123456".equals(oldPassword) : oldPassword.equals(storedPassword);
        }
        
        if (!passwordMatch) {
            return ResponseEntity.badRequest().body(Map.of("message", "原密码错误"));
        }
        
        // Save new BCrypt hashed password
        teacher.setPassword(passwordEncoder.encode(newPassword));
        teacherRepo.save(teacher);
        return ResponseEntity.ok(Map.of("message", "密码修改成功"));
    }

    @GetMapping("/info")
    public ResponseEntity<?> getInfo() {
        String auth = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        String teacherNo = auth.substring(auth.indexOf(':') + 1);
        return teacherRepo.findByTeacherNo(teacherNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/courses")
    public ResponseEntity<?> myCourses() {
        String auth = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        String teacherNo = auth.substring(auth.indexOf(':') + 1);
        Teacher teacher = teacherRepo.findByTeacherNo(teacherNo).orElse(null);
        if (teacher == null) return ResponseEntity.notFound().build();
        List<Course> courses = courseRepo.findByTeacherId(teacher.getId());
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/courses/{courseId}/students")
    public ResponseEntity<?> courseStudents(@PathVariable Long courseId) {
        String auth = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        String teacherNo = auth.substring(auth.indexOf(':') + 1);
        Teacher teacher = teacherRepo.findByTeacherNo(teacherNo).orElse(null);
        if (teacher == null) return ResponseEntity.status(403).body(Map.of("message", "无权限"));
        
        Course course = courseRepo.findById(courseId).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();
        if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "该课程不属于您"));
        }
        
        List<Enrollment> enrollments = enrollmentRepo.findByCourseIdAndStatus(courseId, "ENROLLED");
        return ResponseEntity.ok(enrollments);
    }

    @PutMapping("/enrollments/{enrollmentId}/score")
    public ResponseEntity<?> setScore(@PathVariable Long enrollmentId, @RequestBody Map<String, Object> body) {
        String auth = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        String teacherNo = auth.substring(auth.indexOf(':') + 1);
        Teacher teacher = teacherRepo.findByTeacherNo(teacherNo).orElse(null);
        if (teacher == null) return ResponseEntity.status(403).body(Map.of("message", "无权限"));
        
        return enrollmentRepo.findById(enrollmentId).map(e -> {
            Course course = e.getCourse();
            if (course == null || course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
                return ResponseEntity.status(403).body(Map.<String, Object>of("message", "无权限录入该成绩"));
            }
            Object scoreObj = body.get("score");
            if (scoreObj != null) {
                e.setScore(scoreObj instanceof Double ? (Double) scoreObj : Double.valueOf(scoreObj.toString()));
            }
            return ResponseEntity.ok(enrollmentRepo.save(e));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/enrollments/batch-score")
    public ResponseEntity<?> batchScore(@RequestBody List<Map<String, Object>> scores) {
        String auth = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        String teacherNo = auth.substring(auth.indexOf(':') + 1);
        Teacher teacher = teacherRepo.findByTeacherNo(teacherNo).orElse(null);
        if (teacher == null) return ResponseEntity.status(403).body(Map.of("message", "无权限"));
        
        int updated = 0;
        for (Map<String, Object> item : scores) {
            Object idObj = item.get("enrollmentId");
            Object scoreObj = item.get("score");
            if (idObj == null || scoreObj == null) continue;
            Long enrollmentId = Long.valueOf(idObj.toString());
            var opt = enrollmentRepo.findById(enrollmentId);
            if (opt.isPresent()) {
                Enrollment e = opt.get();
                Course course = e.getCourse();
                if (course != null && course.getTeacher() != null && course.getTeacher().getId().equals(teacher.getId())) {
                    e.setScore(scoreObj instanceof Double ? (Double) scoreObj : Double.valueOf(scoreObj.toString()));
                    enrollmentRepo.save(e);
                    updated++;
                }
            }
        }
        return ResponseEntity.ok(Map.of("updated", updated));
    }
}