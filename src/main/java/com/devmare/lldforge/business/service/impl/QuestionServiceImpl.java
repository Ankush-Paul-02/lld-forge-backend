package com.devmare.lldforge.business.service.impl;

import com.devmare.lldforge.business.dto.CreateQuestionRequestDto;
import com.devmare.lldforge.business.service.QuestionService;
import com.devmare.lldforge.data.entity.Question;
import com.devmare.lldforge.data.enums.Topic;
import com.devmare.lldforge.data.entity.User;
import com.devmare.lldforge.data.enums.Difficulty;
import com.devmare.lldforge.data.exception.AppInfoException;
import com.devmare.lldforge.data.repository.QuestionRepository;
import com.devmare.lldforge.security.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Override
    public Question postQuestion(CreateQuestionRequestDto dto) {
        User mentor = customOAuth2UserService.getCurrentAuthenticatedUser();

        Question question = Question.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .topics(dto.getTopics())
                .constraints(dto.getConstraints())
                .expectedEntitiesDescription(dto.getExpectedEntitiesDescription() != null ? dto.getExpectedEntitiesDescription() : "")
                .difficulty(dto.getDifficulty())
                .author(mentor)
                .postedAt(Instant.now().getEpochSecond())
                .build();

        return questionRepository.save(question);
    }

    @Override
    public Question getQuestionById(Long id) {
        Optional<Question> optionalQuestion = questionRepository.findById(id);
        if (optionalQuestion.isEmpty()) {
            throw new AppInfoException("Question not found.", HttpStatus.NOT_FOUND);
        }
        return optionalQuestion.get();
    }

    @Override
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    @Override
    public Page<Question> getQuestionsByTopic(String topic, int page, int size) {
        Topic topicEnum;
        try {
            topicEnum = Topic.valueOf(topic.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppInfoException("Invalid topic: " + topic, HttpStatus.BAD_REQUEST);
        }
        Pageable pageable = PageRequest.of(page, size);
        return questionRepository.findByTopicsContaining(topicEnum, pageable);
    }

    @Override
    public Page<Question> getQuestionsByDifficulty(String difficulty, int page, int size) {
        Difficulty difficultyEnum;
        try {
            difficultyEnum = Difficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppInfoException("Invalid difficulty: " + difficulty, HttpStatus.BAD_REQUEST);
        }
        Pageable pageable = PageRequest.of(page, size);
        return questionRepository.findByDifficulty(difficultyEnum, pageable);
    }

    @Override
    public void deleteQuestionById(Long id) {
        Optional<Question> optionalQuestion = questionRepository.findById(id);
        if (optionalQuestion.isEmpty()) {
            throw new AppInfoException("Question not found.", HttpStatus.NOT_FOUND);
        }
        questionRepository.delete(optionalQuestion.get());
    }

    @Override
    public Question updateQuestion(Long id, CreateQuestionRequestDto updatedQuestion) {
        Optional<Question> optionalQuestion = questionRepository.findById(id);
        if (optionalQuestion.isEmpty()) {
            throw new AppInfoException("Question not found.", HttpStatus.NOT_FOUND);
        }

        Question existingQuestion = optionalQuestion.get();
        existingQuestion.setTitle(updatedQuestion.getTitle());
        existingQuestion.setDescription(updatedQuestion.getDescription());
        existingQuestion.setTopics(updatedQuestion.getTopics());
        existingQuestion.setConstraints(updatedQuestion.getConstraints());
        existingQuestion.setExpectedEntitiesDescription(updatedQuestion.getExpectedEntitiesDescription() != null ? updatedQuestion.getExpectedEntitiesDescription() : "");
        existingQuestion.setDifficulty(updatedQuestion.getDifficulty());

        return questionRepository.save(existingQuestion);
    }
}
