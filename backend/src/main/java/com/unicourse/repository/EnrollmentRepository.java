package com.unicourse.repository;

import com.unicourse.model.Course;
import com.unicourse.model.Enrollment;
import com.unicourse.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentAndStatus(Student student, String status);
    List<Enrollment> findByStudentIdAndStatus(Long studentId, String status);
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByCourseId(Long courseId);
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    long countByStatus(String status);
    long countByCourseIdAndStatus(Long courseId, String status);
    List<Enrollment> findByCourseIdAndStatus(Long courseId, String status);
}