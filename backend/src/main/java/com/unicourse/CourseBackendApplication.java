package com.unicourse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CourseBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourseBackendApplication.class, args);
    }
}