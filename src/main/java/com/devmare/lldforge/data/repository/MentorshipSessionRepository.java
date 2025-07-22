package com.devmare.lldforge.data.repository;

import com.devmare.lldforge.data.entity.MentorshipSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MentorshipSessionRepository extends JpaRepository<MentorshipSession, Long> {
}
