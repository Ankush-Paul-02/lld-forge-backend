package com.devmare.lldforge.data.repository;

import com.devmare.lldforge.data.entity.Question;
import com.devmare.lldforge.data.entity.Topic;
import com.devmare.lldforge.data.enums.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {

    Page<Question> findByTopicsContaining(Topic topic, Pageable pageable);

    Page<Question> findByDifficulty(Difficulty difficulty, Pageable pageable);
}
