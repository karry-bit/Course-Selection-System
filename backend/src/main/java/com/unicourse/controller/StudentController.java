package com.unicourse.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unicourse.model.Course;
import com.unicourse.model.Enrollment;
import com.unicourse.model.Student;
import com.unicourse.repository.CourseRepository;
import com.unicourse.repository.EnrollmentRepository;
import com.unicourse.repository.StudentRepository;
import com.unicourse.repository.TermSessionRepository;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final TermSessionRepository termSessionRepo;
    private final PasswordEncoder passwordEncoder;

    public StudentController(StudentRepository studentRepo, CourseRepository courseRepo,
                             EnrollmentRepository enrollmentRepo, TermSessionRepository termSessionRepo,
                             PasswordEncoder passwordEncoder) {
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.termSessionRepo = termSessionRepo;
        this.passwordEncoder = passwordEncoder;
    }

    private Student getCurrentStudent() {
        String auth = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        String studentNo = auth.substring(auth.indexOf(':') + 1);
        return studentRepo.findByStudentNo(studentNo).orElse(null);
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        Student student = getCurrentStudent();
        if (student == null) return ResponseEntity.notFound().build();
        
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "新密码不能为空"));
        }
        
        String storedPassword = student.getPassword();
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
        student.setPassword(passwordEncoder.encode(newPassword));
        studentRepo.save(student);
        return ResponseEntity.ok(Map.of("message", "密码修改成功"));
    }

    @GetMapping("/info")
    public ResponseEntity<?> getInfo() {
        Student s = getCurrentStudent();
        if (s == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(s);
    }

    @GetMapping("/courses")
    public ResponseEntity<?> listCourses(@RequestParam(required = false) String search,
                                         @RequestParam(required = false) Long teacherId,
                                         @RequestParam(required = false) String semester) {
        List<Course> courses;
        if (search != null && !search.isBlank()) {
            if (semester != null && !semester.isBlank()) {
                courses = courseRepo.findBySemesterAndNameContainingIgnoreCase(semester, search);
            } else {
                courses = courseRepo.findByNameContainingIgnoreCase(search);
            }
        } else if (semester != null && !semester.isBlank()) {
            courses = courseRepo.findBySemester(semester);
        } else {
            courses = courseRepo.findAll();
        }
        if (teacherId != null) {
            courses = courses.stream().filter(c -> c.getTeacher() != null && c.getTeacher().getId().equals(teacherId))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(courses);
    }

    /**
     * 检查选课冲突，返回详细的冲突信息
     */
    @PostMapping("/enroll/check-conflict")
    public ResponseEntity<?> checkConflict(@RequestBody Map<String, Long> body) {
        Long courseId = body.get("courseId");
        if (courseId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "课程ID不能为空"));
        }
        
        Student student = getCurrentStudent();
        if (student == null) return ResponseEntity.notFound().build();

        Course course = courseRepo.findById(courseId).orElse(null);
        if (course == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "课程不存在"));
        }

        String newTime = course.getTimeSlot();
        List<Map<String, Object>> conflicts = new ArrayList<>();
        
        if (newTime != null && !newTime.isBlank()) {
            List<Enrollment> myEnrollments = enrollmentRepo.findByStudentIdAndStatus(student.getId(), "ENROLLED");
            for (Enrollment e : myEnrollments) {
                Course ec = e.getCourse();
                if (ec.getTimeSlot() != null && !ec.getTimeSlot().isBlank()) {
                    List<String> conflictDetails = findConflictDetails(newTime, ec.getTimeSlot());
                    if (!conflictDetails.isEmpty()) {
                        Map<String, Object> conflict = new HashMap<>();
                        conflict.put("courseId", ec.getId());
                        conflict.put("courseName", ec.getName());
                        conflict.put("courseCode", ec.getCode());
                        conflict.put("timeSlot", ec.getTimeSlot());
                        conflict.put("conflictDetails", conflictDetails);
                        conflicts.add(conflict);
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hasConflict", !conflicts.isEmpty());
        result.put("newCourse", Map.of(
            "id", course.getId(),
            "name", course.getName(),
            "code", course.getCode(),
            "timeSlot", course.getTimeSlot()
        ));
        result.put("conflicts", conflicts);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/enroll")
    @Transactional
    public ResponseEntity<?> enroll(@RequestBody Map<String, Object> body) {
        Long courseId = body.get("courseId") != null ? Long.valueOf(body.get("courseId").toString()) : null;
        Boolean force = body.get("force") != null ? Boolean.valueOf(body.get("force").toString()) : false;
        
        if (courseId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "课程ID不能为空"));
        }
        Student student = getCurrentStudent();
        if (student == null) return ResponseEntity.notFound().build();

        Course course = courseRepo.findById(courseId).orElse(null);
        if (course == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "课程不存在"));
        }

        // Check term session is open
        var termOpt = termSessionRepo.findTopByIsOpenTrueOrderByEndTimeDesc();
        if (termOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "选课未开放"));
        }

        // Check already enrolled
        if (enrollmentRepo.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "已选该课程"));
        }

        // Check capacity
        long enrolledCount = enrollmentRepo.countByCourseIdAndStatus(courseId, "ENROLLED");
        if (enrolledCount >= course.getCapacity()) {
            return ResponseEntity.badRequest().body(Map.of("message", "课程已满"));
        }

        // Check time conflict (unless force=true)
        String newTime = course.getTimeSlot();
        if (!force && newTime != null && !newTime.isBlank()) {
            List<Enrollment> myEnrollments = enrollmentRepo.findByStudentIdAndStatus(student.getId(), "ENROLLED");
            List<Map<String, Object>> conflicts = new ArrayList<>();
            
            for (Enrollment e : myEnrollments) {
                Course ec = e.getCourse();
                if (ec.getTimeSlot() != null && !ec.getTimeSlot().isBlank()) {
                    List<String> conflictDetails = findConflictDetails(newTime, ec.getTimeSlot());
                    if (!conflictDetails.isEmpty()) {
                        Map<String, Object> conflict = new HashMap<>();
                        conflict.put("courseId", ec.getId());
                        conflict.put("courseName", ec.getName());
                        conflict.put("courseCode", ec.getCode());
                        conflict.put("timeSlot", ec.getTimeSlot());
                        conflict.put("conflictDetails", conflictDetails);
                        conflicts.add(conflict);
                    }
                }
            }
            
            if (!conflicts.isEmpty()) {
                return ResponseEntity.status(409).body(Map.of(
                    "message", "时间冲突",
                    "newCourse", Map.of(
                        "id", course.getId(),
                        "name", course.getName(),
                        "code", course.getCode(),
                        "timeSlot", course.getTimeSlot()
                    ),
                    "conflicts", conflicts
                ));
            }
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus("ENROLLED");
        enrollment.setCreatedAt(LocalDateTime.now());
        enrollmentRepo.save(enrollment);

        return ResponseEntity.ok(Map.of("message", "选课成功"));
    }

    @DeleteMapping("/enroll/{courseId}")
    @Transactional
    public ResponseEntity<?> dropCourse(@PathVariable Long courseId) {
        Student student = getCurrentStudent();
        if (student == null) return ResponseEntity.notFound().build();

        // Check term session is open
        var termOpt = termSessionRepo.findTopByIsOpenTrueOrderByEndTimeDesc();
        if (termOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "选课未开放"));
        }

        var opt = enrollmentRepo.findByStudentIdAndCourseId(student.getId(), courseId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "未选该课程"));
        }
        enrollmentRepo.delete(opt.get());
        return ResponseEntity.ok(Map.of("message", "退课成功"));
    }

    @GetMapping("/my-enrollments")
    public ResponseEntity<?> myEnrollments() {
        Student student = getCurrentStudent();
        if (student == null) return ResponseEntity.notFound().build();
        List<Enrollment> list = enrollmentRepo.findByStudentId(student.getId());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/my-scores")
    public ResponseEntity<?> myScores() {
        Student student = getCurrentStudent();
        if (student == null) return ResponseEntity.notFound().build();
        List<Enrollment> list = enrollmentRepo.findByStudentId(student.getId());
        List<Map<String, Object>> result = list.stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("enrollmentId", e.getId());
            m.put("courseName", e.getCourse().getName());
            m.put("courseCode", e.getCourse().getCode());
            m.put("credit", e.getCourse().getCredit());
            m.put("score", e.getScore());
            m.put("status", e.getStatus());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/term-session")
    public ResponseEntity<?> getCurrentTerm() {
        var termOpt = termSessionRepo.findTopByIsOpenTrueOrderByEndTimeDesc();
        if (termOpt.isPresent()) {
            var ts = termOpt.get();
            return ResponseEntity.ok(Map.of(
                "isOpen", true,
                "id", ts.getId(),
                "name", ts.getName(),
                "startTime", ts.getStartTime() != null ? ts.getStartTime().toString() : null,
                "endTime", ts.getEndTime() != null ? ts.getEndTime().toString() : null
            ));
        }
        return ResponseEntity.ok(Map.of("isOpen", false));
    }

    /**
     * 解析时间字符串，返回时间段列表
     * 格式: "周一12节(2-9周);周三12节(6-9周)" -> [{day:"周一", sections:"12", weeks:"2-9"}, ...]
     */
    private List<Map<String, String>> parseTimeSlot(String timeSlot) {
        List<Map<String, String>> result = new ArrayList<>();
        if (timeSlot == null || timeSlot.isBlank()) return result;
        
        // 分割多个时间段
        String[] parts = timeSlot.split("[;；]");
        
        // 正则匹配: 周一12节(2-9周) 或 周一12节 或 周一1-2节
        Pattern pattern = Pattern.compile("(周[一二三四五六日])(\\d+)(?:-(\\d+))?节(?:\\(([\\d,-]+)周\\))?");
        
        for (String part : parts) {
            part = part.trim();
            Matcher matcher = pattern.matcher(part);
            if (matcher.find()) {
                Map<String, String> slot = new HashMap<>();
                slot.put("day", matcher.group(1));
                String startSection = matcher.group(2);
                String endSection = matcher.group(3);
                if (endSection != null) {
                    slot.put("sections", startSection + "-" + endSection);
                } else {
                    slot.put("sections", startSection);
                }
                slot.put("weeks", matcher.group(4) != null ? matcher.group(4) : "");
                result.add(slot);
            }
        }
        
        return result;
    }

    /**
     * 查找两个时间段之间的冲突详情
     */
    private List<String> findConflictDetails(String timeA, String timeB) {
        List<String> conflicts = new ArrayList<>();
        List<Map<String, String>> slotsA = parseTimeSlot(timeA);
        List<Map<String, String>> slotsB = parseTimeSlot(timeB);
        
        for (Map<String, String> a : slotsA) {
            for (Map<String, String> b : slotsB) {
                // 同一天才可能冲突
                if (a.get("day").equals(b.get("day"))) {
                    // 检查节次冲突
                    if (hasSectionConflict(a.get("sections"), b.get("sections"))) {
                        // 检查周次冲突
                        String conflictWeeks = findWeekConflict(a.get("weeks"), b.get("weeks"));
                        if (conflictWeeks != null) {
                            String detail = a.get("day") + " 第" + a.get("sections") + "节";
                            if (!conflictWeeks.isEmpty()) {
                                detail += " (第" + conflictWeeks + "周)";
                            }
                            conflicts.add(detail);
                        }
                    }
                }
            }
        }
        
        return conflicts;
    }

    /**
     * 检查节次是否冲突
     */
    private boolean hasSectionConflict(String sectionsA, String sectionsB) {
        try {
            int aStart, aEnd, bStart, bEnd;
            
            if (sectionsA.contains("-")) {
                String[] parts = sectionsA.split("-");
                aStart = Integer.parseInt(parts[0]);
                aEnd = Integer.parseInt(parts[1]);
            } else {
                aStart = aEnd = Integer.parseInt(sectionsA);
            }
            
            if (sectionsB.contains("-")) {
                String[] parts = sectionsB.split("-");
                bStart = Integer.parseInt(parts[0]);
                bEnd = Integer.parseInt(parts[1]);
            } else {
                bStart = bEnd = Integer.parseInt(sectionsB);
            }
            
            return !(aEnd < bStart || bEnd < aStart);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 查找周次冲突
     */
    private String findWeekConflict(String weeksA, String weeksB) {
        if (weeksA == null || weeksA.isEmpty() || weeksB == null || weeksB.isEmpty()) {
            // 如果任一周次为空，假设全年都上课
            return "";
        }
        
        List<Integer> weeksListA = parseWeeks(weeksA);
        List<Integer> weeksListB = parseWeeks(weeksB);
        
        List<Integer> conflictWeeks = new ArrayList<>();
        for (int w : weeksListA) {
            if (weeksListB.contains(w)) {
                conflictWeeks.add(w);
            }
        }
        
        if (conflictWeeks.isEmpty()) return null;
        
        // 简化输出
        if (conflictWeeks.size() > 5) {
            return conflictWeeks.get(0) + "-" + conflictWeeks.get(conflictWeeks.size() - 1);
        }
        
        return conflictWeeks.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
    }

    /**
     * 解析周次字符串 "2-9,12-14" -> [2,3,4,5,6,7,8,9,12,13,14]
     */
    private List<Integer> parseWeeks(String weeks) {
        List<Integer> result = new ArrayList<>();
        if (weeks == null || weeks.isEmpty()) return result;
        
        String[] parts = weeks.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.contains("-")) {
                String[] range = part.split("-");
                try {
                    int start = Integer.parseInt(range[0]);
                    int end = Integer.parseInt(range[1]);
                    for (int i = start; i <= end; i++) {
                        result.add(i);
                    }
                } catch (Exception e) {}
            } else {
                try {
                    result.add(Integer.parseInt(part));
                } catch (Exception e) {}
            }
        }
        
        return result;
    }
}
