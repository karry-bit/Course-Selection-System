package com.unicourse.repository;

import com.unicourse.model.Enrollment;
import com.unicourse.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentAndStatus(Student student, String status);
    List<Enrollment> findByStudentIdAndStatus(Long studentId, String status);
}
