package com.devmare.lldforge.business.service;

import com.devmare.lldforge.business.dto.CreateQuestionRequestDto;
import com.devmare.lldforge.data.entity.Question;
import org.springframework.data.domain.Page;

import java.util.List;

public interface QuestionService {

    Question postQuestion(CreateQuestionRequestDto question);

    Question getQuestionById(String id);

    List<Question> getAllQuestions();

    Page<Question> getQuestionsByTopic(String topic, int page, int size);

    Page<Question> getQuestionsByDifficulty(String difficulty, int page, int size);

    void deleteQuestionById(String id);

    Question updateQuestion(String id, CreateQuestionRequestDto updatedQuestion);
}
