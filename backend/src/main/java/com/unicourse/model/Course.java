package com.unicourse.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String code;
    private String name;
    private Integer credit;
    private String timeSlot;
    private String place;
    private Integer capacity;
    private Integer enrolled = 0;
    private String description;
    @Column(name = "semester")
    private String semester;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getCredit() { return credit; }
    public void setCredit(Integer credit) { this.credit = credit; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getEnrolled() { return enrolled; }
    public void setEnrolled(Integer enrolled) { this.enrolled = enrolled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
}