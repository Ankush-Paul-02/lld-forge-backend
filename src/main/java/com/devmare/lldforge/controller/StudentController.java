package com.devmare.lldforge.controller;

import com.devmare.lldforge.business.dto.DefaultResponseDto;
import com.devmare.lldforge.business.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.devmare.lldforge.business.dto.DefaultResponseDto.Status.SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/student")
public class StudentController {

    private final QuestionService questionService;

    @GetMapping("/question/{id}")
    public ResponseEntity<DefaultResponseDto> getQuestionById(@PathVariable String id) {
        return ResponseEntity.ok(new DefaultResponseDto(
                SUCCESS,
                Map.of("data", questionService.getQuestionById(id)),
                "Fetched question by ID"
        ));
    }

    @GetMapping("/question/all")
    public ResponseEntity<DefaultResponseDto> getAllQuestions() {
        return ResponseEntity.ok(new DefaultResponseDto(
                SUCCESS,
                java.util.Map.of("data", questionService.getAllQuestions()),
                "Fetched all questions"
        ));
    }

    @GetMapping("/question/by-topic")
    public ResponseEntity<DefaultResponseDto> getQuestionsByTopic(
            @RequestParam String topic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(new DefaultResponseDto(
                SUCCESS,
                java.util.Map.of("data", questionService.getQuestionsByTopic(topic, page, size)),
                "Fetched questions by topic"
        ));
    }

    @GetMapping("/question/by-difficulty")
    public ResponseEntity<DefaultResponseDto> getQuestionsByDifficulty(
            @RequestParam String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(new DefaultResponseDto(
                SUCCESS,
                java.util.Map.of("data", questionService.getQuestionsByDifficulty(difficulty, page, size)),
                "Fetched questions by difficulty"
        ));
    }
}
