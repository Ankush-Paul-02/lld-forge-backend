package com.devmare.lldforge.controller;

import com.devmare.lldforge.business.dto.CreateQuestionRequestDto;
import com.devmare.lldforge.business.dto.DefaultResponseDto;
import com.devmare.lldforge.business.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

import static com.devmare.lldforge.business.dto.DefaultResponseDto.Status.SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mentor")
public class MentorController {

    private final QuestionService questionService;

    @PostMapping("/question/post")
    public ResponseEntity<DefaultResponseDto> postQuestion(@Valid @RequestBody CreateQuestionRequestDto dto) {
        return ResponseEntity.ok(new DefaultResponseDto(
                SUCCESS,
                Map.of("data", questionService.postQuestion(dto)),
                "Question posted successfully by mentor."
        ));
    }

    @PutMapping("/question/edit/{id}")
    public ResponseEntity<DefaultResponseDto> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody CreateQuestionRequestDto dto
    ) {
        return ResponseEntity.ok(new DefaultResponseDto(
                SUCCESS,
                Map.of("data", questionService.updateQuestion(id, dto)),
                "Question updated successfully by mentor."
        ));
    }

    @DeleteMapping("/question/delete/{id}")
    public ResponseEntity<DefaultResponseDto> deleteQuestion(
            @PathVariable Long id
    ) {
        questionService.deleteQuestionById(id);
        return ResponseEntity.ok(new DefaultResponseDto(
                SUCCESS,
                Map.of("data", id),
                "Question deleted successfully by mentor."
        ));
    }
}
