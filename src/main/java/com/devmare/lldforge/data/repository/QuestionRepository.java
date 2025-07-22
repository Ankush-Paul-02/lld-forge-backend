package com.devmare.lldforge.data.repository;

import com.devmare.lldforge.business.dto.MentorLeaderboardProjection;
import com.devmare.lldforge.data.entity.Question;
import com.devmare.lldforge.data.enums.Difficulty;
import com.devmare.lldforge.data.enums.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    Page<Question> findByTopicsContaining(Topic topic, Pageable pageable);

    Page<Question> findByDifficulty(Difficulty difficulty, Pageable pageable);

    @Query("""
                SELECT q.author.id AS id, q.author.name AS name, COUNT(q) AS questionCount
                FROM Question q
                WHERE q.author.role = com.devmare.lldforge.data.enums.Role.MENTOR
                GROUP BY q.author.id, q.author.name
                ORDER BY COUNT(q) DESC
            """)
    List<MentorLeaderboardProjection> findMentorLeaderboard();

}
