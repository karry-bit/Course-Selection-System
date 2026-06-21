package com.unicourse.repository;

import com.unicourse.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentNo(String studentNo);
    boolean existsByStudentNo(String studentNo);
    
    // 根据班级ID查询学生
    List<Student> findByClassInfoId(Long classId);
    
    // 根据班级ID删除学生
    @Modifying
    @Query("DELETE FROM Student s WHERE s.classInfo.id = :classId")
    void deleteByClassInfoId(@Param("classId") Long classId);
    
    // 统计班级学生数量
    long countByClassInfoId(Long classId);
}