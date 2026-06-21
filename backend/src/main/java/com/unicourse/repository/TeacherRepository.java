package com.unicourse.repository;

import com.unicourse.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByTeacherNo(String teacherNo);
    boolean existsByTeacherNo(String teacherNo);
}