package com.devmare.lldforge.data.repository;

import com.devmare.lldforge.data.entity.MentorshipSession;
import com.devmare.lldforge.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorshipSessionRepository extends JpaRepository<MentorshipSession, Long> {

    List<MentorshipSession> findAllByStudent(User user);
}
