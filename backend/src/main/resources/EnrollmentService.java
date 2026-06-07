package com.unicourse.service;

import com.unicourse.model.Course;
import com.unicourse.model.Enrollment;
import com.unicourse.model.Student;
import com.unicourse.repository.CourseRepository;
import com.unicourse.repository.EnrollmentRepository;
import com.unicourse.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, StudentRepository studentRepository, CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    // 简单冲突检测：基于 timeSlot 字符串完全相同视为冲突
    public boolean hasTimeConflict(Student student, Course target) {
        List<Enrollment> current = enrollmentRepository.findByStudentAndStatus(student, "SELECTED");
        for (Enrollment e : current) {
            if (e.getCourse() != null && e.getCourse().getTimeSlot() != null && e.getCourse().getTimeSlot().equals(target.getTimeSlot())) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public Enrollment enroll(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        Course course = courseRepository.findById(courseId).orElseThrow();

        if (course.getCapacity() != null && course.getEnrolled() >= course.getCapacity()) {
            throw new IllegalStateException("Course full");
        }

        if (hasTimeConflict(student, course)) {
            throw new IllegalStateException("Time conflict");
        }

        Enrollment e = new Enrollment();
        e.setStudent(student);
        e.setCourse(course);
        e.setStatus("SELECTED");
        course.setEnrolled(course.getEnrolled() + 1);
        courseRepository.save(course);
        return enrollmentRepository.save(e);
    }
}
