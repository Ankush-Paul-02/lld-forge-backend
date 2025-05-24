package com.devmare.lldforge.controller;

import com.devmare.lldforge.business.dto.DefaultResponseDto;
import com.devmare.lldforge.business.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.devmare.lldforge.business.dto.DefaultResponseDto.Status.SUCCESS;


@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send-verification-email")
    public ResponseEntity<DefaultResponseDto> sendVerificationEmail(@RequestParam String email) {
        emailVerificationService.sendVerificationEmail(email);
        return ResponseEntity.ok(
                new DefaultResponseDto(
                        SUCCESS,
                        Map.of("data", "Verification email sent to " + email),
                        "Verification email sent."
                )
        );
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully.");
    }
}
