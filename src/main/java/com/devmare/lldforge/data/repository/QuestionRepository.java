package com.devmare.lldforge.data.repository;

import com.devmare.lldforge.data.entity.Question;
import com.devmare.lldforge.data.enums.Topic;
import com.devmare.lldforge.data.enums.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    Page<Question> findByTopicsContaining(Topic topic, Pageable pageable);

    Page<Question> findByDifficulty(Difficulty difficulty, Pageable pageable);
}
