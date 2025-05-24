package com.devmare.lldforge.data.repository;

import com.devmare.lldforge.data.entity.MentorApplication;
import com.devmare.lldforge.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MentorApplicationRepository extends JpaRepository<MentorApplication, Long> {

    Optional<MentorApplication> findByUser(User user);
}
