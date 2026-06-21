package com.unicourse.repository;

import com.unicourse.model.ClassInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassInfoRepository extends JpaRepository<ClassInfo, Long> {
    Optional<ClassInfo> findByName(String name);
}