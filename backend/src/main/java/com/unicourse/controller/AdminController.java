
package com.unicourse.controller;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.unicourse.model.ClassInfo;
import com.unicourse.model.Course;
import com.unicourse.model.Student;
import com.unicourse.model.Teacher;
import com.unicourse.model.TermSession;
import com.unicourse.repository.ClassInfoRepository;
import com.unicourse.repository.CourseRepository;
import com.unicourse.repository.EnrollmentRepository;
import com.unicourse.repository.StudentRepository;
import com.unicourse.repository.TeacherRepository;
import com.unicourse.repository.TermSessionRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final StudentRepository studentRepo;
    private final TeacherRepository teacherRepo;
    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final ClassInfoRepository classInfoRepo;
    private final TermSessionRepository termSessionRepo;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${default.password:123456}")
    private String defaultPassword;

    public AdminController(StudentRepository studentRepo, TeacherRepository teacherRepo,
                           CourseRepository courseRepo, EnrollmentRepository enrollmentRepo,
                           ClassInfoRepository classInfoRepo, TermSessionRepository termSessionRepo,
                           PasswordEncoder passwordEncoder) {
        this.studentRepo = studentRepo;
        this.teacherRepo = teacherRepo;
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.classInfoRepo = classInfoRepo;
        this.termSessionRepo = termSessionRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== Student Management ====================

    @GetMapping("/students")
    public ResponseEntity<?> listStudents() {
        return ResponseEntity.ok(studentRepo.findAll());
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<?> getStudent(@PathVariable Long id) {
        return studentRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/students")
    public ResponseEntity<?> createStudent(@RequestBody Map<String, Object> body) {
        String studentNo = body.get("studentNo") != null ? body.get("studentNo").toString() : null;
        String name = body.get("name") != null ? body.get("name").toString() : null;
        
        if (studentNo == null || studentNo.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "学号不能为空"));
        }
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "姓名不能为空"));
        }
        if (studentRepo.existsByStudentNo(studentNo)) {
            return ResponseEntity.badRequest().body(Map.of("message", "学号已存在"));
        }
        
        // 班级必须存在
        Object classIdObj = body.get("classId");
        if (classIdObj == null || classIdObj.toString().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "班级不能为空"));
        }
        Long classId = classIdObj instanceof Number ? ((Number) classIdObj).longValue() : Long.parseLong(classIdObj.toString());
        ClassInfo classInfo = classInfoRepo.findById(classId).orElse(null);
        if (classInfo == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "班级不存在"));
        }
        
        Student student = new Student();
        student.setStudentNo(studentNo);
        student.setName(name);
        student.setGender(body.get("gender") != null ? body.get("gender").toString() : null);
        student.setBirthDate(body.get("birthDate") != null ? body.get("birthDate").toString() : null);
        student.setPhone(body.get("phone") != null ? body.get("phone").toString() : null);
        student.setEmail(body.get("email") != null ? body.get("email").toString() : null);
        
        student.setClassInfo(classInfo);
        
        student.setPassword(passwordEncoder.encode(defaultPassword));
        Student saved = studentRepo.save(student);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return studentRepo.findById(id).map(s -> {
            if (body.get("name") != null) s.setName(body.get("name").toString());
            if (body.get("gender") != null) s.setGender(body.get("gender").toString());
            if (body.get("birthDate") != null) s.setBirthDate(body.get("birthDate").toString());
            if (body.get("phone") != null) s.setPhone(body.get("phone").toString());
            if (body.get("email") != null) s.setEmail(body.get("email").toString());
            
            // 更新班级关联
            Object classIdObj = body.get("classId");
            if (classIdObj != null) {
                Long classId = classIdObj instanceof Number ? ((Number) classIdObj).longValue() : Long.parseLong(classIdObj.toString());
                classInfoRepo.findById(classId).ifPresentOrElse(
                    s::setClassInfo,
                    () -> s.setClassInfo(null)
                );
            } else {
                s.setClassInfo(null);
            }
            
            String newStudentNo = body.get("studentNo") != null ? body.get("studentNo").toString() : null;
            if (newStudentNo != null && !newStudentNo.equals(s.getStudentNo())) {
                if (studentRepo.existsByStudentNo(newStudentNo)) {
                    return ResponseEntity.badRequest().body(Map.<String, Object>of("message", "学号已存在"));
                }
                s.setStudentNo(newStudentNo);
            }
            return ResponseEntity.ok(studentRepo.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/students/{id}")
    @Transactional
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        enrollmentRepo.deleteAll(enrollmentRepo.findByStudentId(id));
        studentRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
    
    @PostMapping("/students/{id}/reset-password")
    public ResponseEntity<?> resetStudentPassword(@PathVariable Long id) {
        return studentRepo.findById(id).map(s -> {
            // Reset to BCrypt hashed default password
            s.setPassword(passwordEncoder.encode(defaultPassword));
            studentRepo.save(s);
            return ResponseEntity.ok(Map.of("message", "密码已重置为默认密码 " + defaultPassword));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/students/import/template")
    public ResponseEntity<?> downloadStudentTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("学生导入模板");
            
            // Create header row
            Row header = sheet.createRow(0);
            String[] headers = {"学号*", "姓名*", "班级*", "性别", "出生日期", "电话", "邮箱"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Create example row
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("S2024001");
            example.createCell(1).setCellValue("张三");
            example.createCell(2).setCellValue("计算机2001班");
            example.createCell(3).setCellValue("男");
            example.createCell(4).setCellValue("2002-01-15");
            example.createCell(5).setCellValue("13800138001");
            example.createCell(6).setCellValue("zhangsan@example.com");
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            workbook.write(out);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student_import_template.xlsx")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(out.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "生成模板失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/teachers/import/template")
    public ResponseEntity<?> downloadTeacherTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("教师导入模板");
            
            // Create header row
            Row header = sheet.createRow(0);
            String[] headers = {"工号*", "姓名*", "职称", "电话", "邮箱"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Create example row
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("T2024001");
            example.createCell(1).setCellValue("李四");
            example.createCell(2).setCellValue("副教授");
            example.createCell(3).setCellValue("13900139001");
            example.createCell(4).setCellValue("lisi@example.com");
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            workbook.write(out);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=teacher_import_template.xlsx")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(out.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "生成模板失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/students/import")
    @Transactional
    public ResponseEntity<?> importStudents(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "文件名不能为空"));
        }
        
        int success = 0, fail = 0;
        try {
            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                // Excel format
                try (InputStream is = file.getInputStream();
                     Workbook workbook = new XSSFWorkbook(is)) {
                    Sheet sheet = workbook.getSheetAt(0);
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) { fail++; continue; }
                        try {
                            String studentNo = getCellValueAsString(row.getCell(0));
                            String name = getCellValueAsString(row.getCell(1));
                            String className = getCellValueAsString(row.getCell(2));
                            if (studentNo == null || studentNo.isBlank() || name == null || name.isBlank()) {
                                fail++; continue;
                            }
                            if (className == null || className.isBlank()) {
                                fail++; continue;
                            }
                            if (studentRepo.existsByStudentNo(studentNo)) { fail++; continue; }
                            
                            Student s = new Student();
                            s.setStudentNo(studentNo.trim());
                            s.setName(name.trim());
                            s.setGender(getCellValueAsString(row.getCell(3)));
                            s.setBirthDate(getCellValueAsString(row.getCell(4)));
                            s.setPhone(getCellValueAsString(row.getCell(5)));
                            s.setEmail(getCellValueAsString(row.getCell(6)));
                            
                            ClassInfo classInfo = classInfoRepo.findByName(className.trim()).orElse(null);
                            if (classInfo == null) {
                                fail++; continue;
                            }
                            s.setClassInfo(classInfo);
                            studentRepo.save(s);
                            success++;
                        } catch (Exception e) { fail++; }
                    }
                }
            } else {
                // CSV format
                try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
                    String[] header = reader.readNext(); // skip header
                    String[] line;
                    while ((line = reader.readNext()) != null) {
                        if (line.length < 2) { fail++; continue; }
                        try {
                            String studentNo = line[0].trim();
                            String name = line[1].trim();
                            String className = line.length > 2 ? line[2].trim() : "";
                            if (studentNo.isBlank() || name.isBlank()) { fail++; continue; }
                            if (className.isBlank()) { fail++; continue; }
                            if (studentRepo.existsByStudentNo(studentNo)) { fail++; continue; }
                            
                            Student s = new Student();
                            s.setStudentNo(studentNo);
                            s.setName(name);
                            
                            ClassInfo classInfo = classInfoRepo.findByName(className).orElse(null);
                            if (classInfo == null) {
                                fail++; continue;
                            }
                            s.setClassInfo(classInfo);
                            
                            if (line.length > 3) s.setGender(line[3].trim());
                            if (line.length > 4) s.setBirthDate(line[4].trim());
                            if (line.length > 5) s.setPhone(line[5].trim());
                            if (line.length > 6) s.setEmail(line[6].trim());
                            studentRepo.save(s);
                            success++;
                        } catch (Exception e) { fail++; }
                    }
                }
            }
            return ResponseEntity.ok(Map.of(
                "success", success, 
                "fail", fail,
                "message", "导入完成，成功: " + success + " 条，失败: " + fail + " 条",
                "defaultPassword", "123456"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "文件解析失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/teachers/import")
    @Transactional
    public ResponseEntity<?> importTeachers(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "文件名不能为空"));
        }
        
        int success = 0, fail = 0;
        try {
            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                // Excel format
                try (InputStream is = file.getInputStream();
                     Workbook workbook = new XSSFWorkbook(is)) {
                    Sheet sheet = workbook.getSheetAt(0);
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) { fail++; continue; }
                        try {
                            String teacherNo = getCellValueAsString(row.getCell(0));
                            String name = getCellValueAsString(row.getCell(1));
                            if (teacherNo == null || teacherNo.isBlank() || name == null || name.isBlank()) {
                                fail++; continue;
                            }
                            if (teacherRepo.existsByTeacherNo(teacherNo)) { fail++; continue; }
                            
                            Teacher t = new Teacher();
                            t.setTeacherNo(teacherNo.trim());
                            t.setName(name.trim());
                            t.setTitle(getCellValueAsString(row.getCell(2)));
                            t.setPhone(getCellValueAsString(row.getCell(3)));
                            t.setEmail(getCellValueAsString(row.getCell(4)));
                            teacherRepo.save(t);
                            success++;
                        } catch (Exception e) { fail++; }
                    }
                }
            } else {
                // CSV format
                try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
                    String[] header = reader.readNext(); // skip header
                    String[] line;
                    while ((line = reader.readNext()) != null) {
                        if (line.length < 2) { fail++; continue; }
                        try {
                            String teacherNo = line[0].trim();
                            String name = line[1].trim();
                            if (teacherNo.isBlank() || name.isBlank()) { fail++; continue; }
                            if (teacherRepo.existsByTeacherNo(teacherNo)) { fail++; continue; }
                            
                            Teacher t = new Teacher();
                            t.setTeacherNo(teacherNo);
                            t.setName(name);
                            if (line.length > 2) t.setTitle(line[2].trim());
                            if (line.length > 3) t.setPhone(line[3].trim());
                            if (line.length > 4) t.setEmail(line[4].trim());
                            teacherRepo.save(t);
                            success++;
                        } catch (Exception e) { fail++; }
                    }
                }
            }
            return ResponseEntity.ok(Map.of(
                "success", success, 
                "fail", fail,
                "message", "导入完成，成功: " + success + " 条，失败: " + fail + " 条",
                "defaultPassword", "123456"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "文件解析失败: " + e.getMessage()));
        }
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    // ==================== Teacher Management ====================

    @GetMapping("/teachers")
    public ResponseEntity<?> listTeachers() {
        return ResponseEntity.ok(teacherRepo.findAll());
    }

    @GetMapping("/teachers/{id}")
    public ResponseEntity<?> getTeacher(@PathVariable Long id) {
        return teacherRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/teachers")
    public ResponseEntity<?> createTeacher(@RequestBody Teacher teacher) {
        if (teacher.getTeacherNo() == null || teacher.getTeacherNo().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "工号不能为空"));
        }
        if (teacher.getName() == null || teacher.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "姓名不能为空"));
        }
        if (teacherRepo.existsByTeacherNo(teacher.getTeacherNo())) {
            return ResponseEntity.badRequest().body(Map.of("message", "工号已存在"));
        }
        Teacher saved = teacherRepo.save(teacher);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/teachers/{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, @RequestBody Teacher body) {
        return teacherRepo.findById(id).map(t -> {
            t.setName(body.getName());
            t.setTitle(body.getTitle());
            t.setPhone(body.getPhone());
            t.setEmail(body.getEmail());
            if (body.getTeacherNo() != null && !body.getTeacherNo().equals(t.getTeacherNo())) {
                if (teacherRepo.existsByTeacherNo(body.getTeacherNo())) {
                    return ResponseEntity.badRequest().body(Map.<String, Object>of("message", "工号已存在"));
                }
                t.setTeacherNo(body.getTeacherNo());
            }
            return ResponseEntity.ok(teacherRepo.save(t));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long id) {
        teacherRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
    
    @PostMapping("/teachers/{id}/reset-password")
    public ResponseEntity<?> resetTeacherPassword(@PathVariable Long id) {
        return teacherRepo.findById(id).map(t -> {
            // Reset to BCrypt hashed default password
            t.setPassword(passwordEncoder.encode(defaultPassword));
            teacherRepo.save(t);
            return ResponseEntity.ok(Map.of("message", "密码已重置为默认密码 " + defaultPassword));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ==================== Course Management ====================

    @GetMapping("/courses")
    public ResponseEntity<?> listAllCourses(@RequestParam(required = false) String semester) {
        if (semester != null && !semester.isBlank()) {
            return ResponseEntity.ok(courseRepo.findBySemester(semester));
        }
        return ResponseEntity.ok(courseRepo.findAll());
    }

    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody Map<String, Object> body) {
        String code = body.get("code") != null ? body.get("code").toString() : null;
        String name = body.get("name") != null ? body.get("name").toString() : null;
        
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "课程代码不能为空"));
        }
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "课程名称不能为空"));
        }
        
        Course course = new Course();
        course.setCode(code);
        course.setName(name);
        
        if (body.get("credit") != null) {
            course.setCredit(Integer.valueOf(body.get("credit").toString()));
        }
        if (body.get("capacity") != null) {
            course.setCapacity(Integer.valueOf(body.get("capacity").toString()));
        }
        if (body.get("timeSlot") != null) {
            course.setTimeSlot(body.get("timeSlot").toString());
        }
        if (body.get("place") != null) {
            course.setPlace(body.get("place").toString());
        }
        if (body.get("semester") != null) {
            course.setSemester(body.get("semester").toString());
        }
        if (body.get("description") != null) {
            course.setDescription(body.get("description").toString());
        }
        
        // 处理教师关联
        if (body.get("teacherId") != null) {
            Long teacherId = Long.valueOf(body.get("teacherId").toString());
            teacherRepo.findById(teacherId).ifPresent(course::setTeacher);
        }
        
        Course saved = courseRepo.save(course);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return courseRepo.findById(id).map(c -> {
            if (body.get("name") != null) c.setName(body.get("name").toString());
            if (body.get("credit") != null) c.setCredit(Integer.valueOf(body.get("credit").toString()));
            if (body.get("timeSlot") != null) c.setTimeSlot(body.get("timeSlot").toString());
            if (body.get("place") != null) c.setPlace(body.get("place").toString());
            if (body.get("capacity") != null) c.setCapacity(Integer.valueOf(body.get("capacity").toString()));
            if (body.get("description") != null) c.setDescription(body.get("description").toString());
            if (body.get("semester") != null) c.setSemester(body.get("semester").toString());
            if (body.get("code") != null && !body.get("code").toString().equals(c.getCode())) {
                c.setCode(body.get("code").toString());
            }
            
            // 处理教师关联
            if (body.get("teacherId") != null) {
                Long teacherId = Long.valueOf(body.get("teacherId").toString());
                if (teacherId == null) {
                    c.setTeacher(null);
                } else {
                    teacherRepo.findById(teacherId).ifPresent(c::setTeacher);
                }
            }
            
            return ResponseEntity.ok(courseRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/courses/{id}")
    @Transactional
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        enrollmentRepo.deleteAll(enrollmentRepo.findByCourseId(id));
        courseRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    // ==================== ClassInfo Management ====================

    @GetMapping("/classes")
    public ResponseEntity<?> listClasses() {
        return ResponseEntity.ok(classInfoRepo.findAll());
    }

    @PostMapping("/classes")
    public ResponseEntity<?> createClass(@RequestBody Map<String, Object> body) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setName(body.get("name") != null ? body.get("name").toString() : null);
        classInfo.setGrade(body.get("grade") != null ? body.get("grade").toString() : null);
        // 兼容前端 department 字段
        if (body.get("department") != null) {
            classInfo.setMajor(body.get("department").toString());
        }
        return ResponseEntity.ok(classInfoRepo.save(classInfo));
    }

    @PutMapping("/classes/{id}")
    public ResponseEntity<?> updateClass(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return classInfoRepo.findById(id).map(c -> {
            if (body.get("name") != null) c.setName(body.get("name").toString());
            if (body.get("grade") != null) c.setGrade(body.get("grade").toString());
            // 兼容前端 department 字段
            if (body.get("department") != null) {
                c.setMajor(body.get("department").toString());
            }
            return ResponseEntity.ok(classInfoRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/classes/{id}")
    @Transactional
    public ResponseEntity<?> deleteClass(@PathVariable Long id) {
        // 先找出该班级的所有学生
        List<Student> students = studentRepo.findByClassInfoId(id);
        long studentCount = students.size();
        
        // 删除班级的学生（包含选课记录）
        for (Student s : students) {
            enrollmentRepo.deleteAll(enrollmentRepo.findByStudentId(s.getId()));
            studentRepo.delete(s);
        }
        
        // 再删除班级
        classInfoRepo.deleteById(id);
        
        return ResponseEntity.ok(Map.of(
            "message", "删除成功",
            "deletedStudents", studentCount
        ));
    }
    
    @PostMapping("/classes/sync")
    @Transactional
    public ResponseEntity<?> syncClasses() {
        // 获取所有唯一班级名称
        List<String> uniqueClasses = studentRepo.findAll().stream()
            .map(Student::getClazz)
            .filter(clazz -> clazz != null && !clazz.isBlank())
            .distinct()
            .collect(Collectors.toList());
        
        int created = 0;
        int associated = 0;
        
        for (String className : uniqueClasses) {
            // 查找是否已存在同名班级
            ClassInfo classInfo = classInfoRepo.findAll().stream()
                .filter(c -> c.getName().equals(className))
                .findFirst()
                .orElse(null);
            
            if (classInfo == null) {
                // 创建新班级
                classInfo = new ClassInfo();
                classInfo.setName(className);
                classInfo = classInfoRepo.save(classInfo);
                created++;
            }
            
            // 更新没有班级关联的学生
            List<Student> studentsWithoutClass = studentRepo.findAll().stream()
                .filter(s -> s.getClassInfo() == null && className.equals(s.getClazz()))
                .collect(Collectors.toList());
            
            for (Student s : studentsWithoutClass) {
                s.setClassInfo(classInfo);
                studentRepo.save(s);
                associated++;
            }
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "同步完成",
            "createdClasses", created,
            "associatedStudents", associated
        ));
    }
    
    @PostMapping("/students/cleanup")
    @Transactional
    public ResponseEntity<?> cleanupUnassociatedStudents() {
        // 找出未关联班级的学生
        List<Student> unassociatedStudents = studentRepo.findAll().stream()
            .filter(s -> s.getClassInfo() == null)
            .collect(Collectors.toList());
        
        int associated = 0;
        int deleted = 0;
        List<Map<String, Object>> deletedStudents = new java.util.ArrayList<>();
        
        for (Student s : unassociatedStudents) {
            String clazzName = s.getClazz();
            if (clazzName != null && !clazzName.isBlank()) {
                // 尝试找同名班级关联
                ClassInfo matchingClass = classInfoRepo.findAll().stream()
                    .filter(c -> c.getName().equals(clazzName))
                    .findFirst()
                    .orElse(null);
                
                if (matchingClass != null) {
                    s.setClassInfo(matchingClass);
                    studentRepo.save(s);
                    associated++;
                } else {
                    // 没有同名班级，删除学生
                    Map<String, Object> studentInfo = new HashMap<>();
                    studentInfo.put("id", s.getId());
                    studentInfo.put("studentNo", s.getStudentNo());
                    studentInfo.put("name", s.getName());
                    deletedStudents.add(studentInfo);
                    
                    enrollmentRepo.deleteAll(enrollmentRepo.findByStudentId(s.getId()));
                    studentRepo.delete(s);
                    deleted++;
                }
            } else {
                // 连班级名称都没有，删除学生
                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("id", s.getId());
                studentInfo.put("studentNo", s.getStudentNo());
                studentInfo.put("name", s.getName());
                deletedStudents.add(studentInfo);
                
                enrollmentRepo.deleteAll(enrollmentRepo.findByStudentId(s.getId()));
                studentRepo.delete(s);
                deleted++;
            }
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "清理完成",
            "associatedStudents", associated,
            "deletedStudents", deleted,
            "deletedDetails", deletedStudents
        ));
    }

    // ==================== Score Management ====================

    @GetMapping("/enrollments")
    public ResponseEntity<?> listEnrollments(@RequestParam(required = false) Long courseId) {
        if (courseId != null) {
            return ResponseEntity.ok(enrollmentRepo.findByCourseId(courseId));
        }
        return ResponseEntity.ok(enrollmentRepo.findAll());
    }

    @PutMapping("/enrollments/{id}/score")
    public ResponseEntity<?> setScore(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return enrollmentRepo.findById(id).map(e -> {
            Object scoreObj = body.get("score");
            if (scoreObj != null) {
                e.setScore(scoreObj instanceof Double ? (Double) scoreObj : Double.valueOf(scoreObj.toString()));
            }
            return ResponseEntity.ok(enrollmentRepo.save(e));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ==================== Term Session Management ====================

    @GetMapping("/term-sessions")
    public ResponseEntity<?> listTermSessions() {
        return ResponseEntity.ok(termSessionRepo.findAll());
    }

    @PostMapping("/term-sessions")
    public ResponseEntity<?> createTermSession(@RequestBody TermSession body) {
        if (Boolean.TRUE.equals(body.getIsOpen())) {
            // Close all other open sessions
            termSessionRepo.findAll().forEach(ts -> {
                if (Boolean.TRUE.equals(ts.getIsOpen())) {
                    ts.setIsOpen(false);
                    termSessionRepo.save(ts);
                }
            });
        }
        TermSession saved = termSessionRepo.save(body);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/term-sessions/{id}")
    public ResponseEntity<?> updateTermSession(@PathVariable Long id, @RequestBody TermSession body) {
        return termSessionRepo.findById(id).map(ts -> {
            ts.setName(body.getName());
            ts.setSemester(body.getSemester());
            ts.setStartTime(body.getStartTime());
            ts.setEndTime(body.getEndTime());
            if (Boolean.TRUE.equals(body.getIsOpen()) && !Boolean.TRUE.equals(ts.getIsOpen())) {
                termSessionRepo.findAll().forEach(other -> {
                    if (Boolean.TRUE.equals(other.getIsOpen())) {
                        other.setIsOpen(false);
                        termSessionRepo.save(other);
                    }
                });
            }
            ts.setIsOpen(body.getIsOpen());
            return ResponseEntity.ok(termSessionRepo.save(ts));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/term-sessions/{id}")
    public ResponseEntity<?> deleteTermSession(@PathVariable Long id) {
        termSessionRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    // ==================== Statistics ====================

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", studentRepo.count());
        stats.put("totalTeachers", teacherRepo.count());
        stats.put("totalCourses", courseRepo.count());
        stats.put("totalEnrollments", enrollmentRepo.count());
        long totalEnrolled = enrollmentRepo.countByStatus("ENROLLED");
        stats.put("totalEnrolled", totalEnrolled);
        stats.put("activeTermSession", termSessionRepo.findTopByIsOpenTrueOrderByEndTimeDesc().orElse(null));
        return ResponseEntity.ok(stats);
    }
}


