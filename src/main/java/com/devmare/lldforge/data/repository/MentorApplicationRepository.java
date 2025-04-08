package com.devmare.lldforge.data.repository;

import com.devmare.lldforge.data.entity.MentorApplication;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MentorApplicationRepository extends MongoRepository<MentorApplication, String> {

    Optional<MentorApplication> findByStudentId(String studentId);
}
