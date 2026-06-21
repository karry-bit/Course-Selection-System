package com.unicourse.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String studentNo;
    private String name;
    private String gender;
    private String birthDate;
    @Column(name = "clazz")
    private String clazz;
    private String phone;
    private String email;
    @JsonIgnore
    private String password;
    
    // 班级关联
    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassInfo classInfo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getClazz() { return clazz; }
    public void setClazz(String clazz) { this.clazz = clazz; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public ClassInfo getClassInfo() { return classInfo; }
    public void setClassInfo(ClassInfo classInfo) { this.classInfo = classInfo; }
    
    // 兼容方法：获取班级名称
    @JsonProperty("className")
    public String getClassName() {
        if (classInfo != null) {
            return classInfo.getName();
        }
        return clazz;
    }
}