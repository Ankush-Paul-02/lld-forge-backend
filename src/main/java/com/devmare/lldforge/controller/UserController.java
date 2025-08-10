package com.devmare.lldforge.controller;

import com.devmare.lldforge.business.dto.DefaultResponseDto;
import com.devmare.lldforge.business.dto.RazorpayCreateOrderRequestDto;
import com.devmare.lldforge.business.service.MentorshipService;
import com.devmare.lldforge.business.service.QuestionService;
import com.devmare.lldforge.business.service.RazorpayService;
import com.devmare.lldforge.data.entity.User;
import com.devmare.lldforge.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

import static com.devmare.lldforge.business.dto.DefaultResponseDto.Status.SUCCESS;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final QuestionService questionService;
    private final RazorpayService razorpayService;
    private final AuthenticationService authenticationService;
    private final MentorshipService mentorshipService;

    @GetMapping("/me")
    public ResponseEntity<DefaultResponseDto> getCurrentUserDetails() {
        User currentUser = authenticationService.fetchAuthenticatedUser();
        return ResponseEntity.ok(new DefaultResponseDto(
                SUCCESS,
                Map.of("data", currentUser),
                "Fetched current authenticated user details"
        ));
    }

    @GetMapping("/leaderboard/mentors")
    public ResponseEntity<DefaultResponseDto> getTopMentors() {
        return ResponseEntity.ok(
                new DefaultResponseDto(
                        SUCCESS,
                        Map.of("data", questionService.getMentorLeaderboard()),
                        "Top mentors fetched successfully."
                )
        );
    }

    @PostMapping("/mentorship-session/book")
    public ResponseEntity<DefaultResponseDto> bookMentorshipSession(
            @Valid @RequestBody RazorpayCreateOrderRequestDto request
    ) {
        return ResponseEntity.ok(
                new DefaultResponseDto(
                        SUCCESS,
                        Map.of("data", razorpayService.createOrder(request)),
                        "Mentorship session booked successfully."
                )
        );
    }

    @GetMapping("/mentorship-sessions")
    public ResponseEntity<DefaultResponseDto> getAllMentorshipSessionsByStudent() {
        return ResponseEntity.ok(
                new DefaultResponseDto(
                        SUCCESS,
                        Map.of("data", mentorshipService.getMentorshipSessionByStudent()),
                        "Mentorship sessions fetched successfully."
                )
        );
    }
}
