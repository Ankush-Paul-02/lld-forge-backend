package com.devmare.lldforge.controller;

import com.devmare.lldforge.business.dto.DefaultResponseDto;
import com.devmare.lldforge.business.dto.MentorApplicationRequestDto;
import com.devmare.lldforge.business.service.MentorApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

import static com.devmare.lldforge.business.dto.DefaultResponseDto.Status.SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mentor-application")
public class MentorApplicationController {

    private final MentorApplicationService mentorApplicationService;

    @PostMapping("/apply")
    public ResponseEntity<DefaultResponseDto> applyForMentor(
            @Valid @RequestBody MentorApplicationRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                new DefaultResponseDto(
                        SUCCESS,
                        Map.of("data", mentorApplicationService.applyForMentorApplication(requestDto)),
                        "Mentor application submmited successfully."
                )
        );
    }

    @GetMapping("/")
    public ResponseEntity<DefaultResponseDto> getMentorApplicationByUserId() {
        return ResponseEntity.ok(
                new DefaultResponseDto(
                        SUCCESS,
                        Map.of("data", mentorApplicationService.getMentorApplicationByUserId()),
                        "Mentor application submmited successfully."
                )
        );
    }
}
