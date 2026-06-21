package com.unicourse.service;

import com.unicourse.model.TermSession;
import com.unicourse.repository.TermSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TermSessionScheduler {

    private static final Logger log = LoggerFactory.getLogger(TermSessionScheduler.class);

    private final TermSessionRepository termSessionRepo;

    public TermSessionScheduler(TermSessionRepository termSessionRepo) {
        this.termSessionRepo = termSessionRepo;
    }

    @Scheduled(fixedRate = 5000)
    public void refreshTermSessionStatus() {
        log.info("[Refresh] Checking term session status...");
        Optional<TermSession> activeSession = termSessionRepo.findTopByIsOpenTrueOrderByEndTimeDesc();
        if (activeSession.isEmpty()) {
            log.info("[Refresh] No active term session found.");
            return;
        }
        TermSession session = activeSession.get();
        if (session.getEndTime() != null && session.getEndTime().isBefore(LocalDateTime.now())) {
            log.info("[Refresh] Term session '{}' expired at {}. Closing it now.", session.getName(), session.getEndTime());
            session.setIsOpen(false);
            termSessionRepo.save(session);
            log.info("[Refresh] Term session '{}' closed successfully.", session.getName());
        } else {
            log.info("[Refresh] Active term session '{}' still valid (end: {}).", session.getName(), session.getEndTime());
        }
    }
}