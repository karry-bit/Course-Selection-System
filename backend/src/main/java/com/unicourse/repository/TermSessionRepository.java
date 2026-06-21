package com.unicourse.repository;

import com.unicourse.model.TermSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TermSessionRepository extends JpaRepository<TermSession, Long> {
    Optional<TermSession> findTopByIsOpenTrueOrderByEndTimeDesc();
}