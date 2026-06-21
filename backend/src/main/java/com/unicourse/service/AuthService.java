package com.unicourse.service;

import com.unicourse.model.Student;
import com.unicourse.model.Teacher;
import com.unicourse.repository.StudentRepository;
import com.unicourse.repository.TeacherRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 公共认证服务，提供获取当前用户等通用方法
 */
@Service
public class AuthService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public AuthService(StudentRepository studentRepository, TeacherRepository teacherRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    /**
     * 获取当前认证信息字符串
     * 格式: "ROLE:userId" 或 "ROLE:userNo"
     */
    public String getCurrentAuth() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
    }

    /**
     * 从认证信息中提取用户标识
     */
    public String extractUserIdentifier(String auth) {
        return auth.substring(auth.indexOf(':') + 1);
    }

    /**
     * 获取当前学生用户
     */
    public Student getCurrentStudent() {
        String auth = getCurrentAuth();
        String studentNo = extractUserIdentifier(auth);
        return studentRepository.findByStudentNo(studentNo).orElse(null);
    }

    /**
     * 获取当前教师用户
     */
    public Teacher getCurrentTeacher() {
        String auth = getCurrentAuth();
        String teacherNo = extractUserIdentifier(auth);
        return teacherRepository.findByTeacherNo(teacherNo).orElse(null);
    }

    /**
     * 检查是否有有效认证
     */
    public boolean isAuthenticated() {
        String auth = getCurrentAuth();
        return auth != null && !auth.isEmpty();
    }
}
