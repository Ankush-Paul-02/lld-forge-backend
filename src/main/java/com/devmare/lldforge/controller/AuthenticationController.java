package com.devmare.lldforge.controller;

import com.devmare.lldforge.business.dto.DefaultResponseDto;
import com.devmare.lldforge.business.dto.LoginRequestDto;
import com.devmare.lldforge.business.dto.RefreshTokenRequest;
import com.devmare.lldforge.business.dto.SignupUserRequestDto;
import com.devmare.lldforge.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

import static com.devmare.lldforge.business.dto.DefaultResponseDto.Status.SUCCESS;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<DefaultResponseDto> signupUser(@Valid @RequestBody SignupUserRequestDto request) {
        authenticationService.signupUser(request);
        return ResponseEntity.ok(
                new DefaultResponseDto(
                        SUCCESS,
                        Map.of("data", "User registered successfully"),
                        "User registered successfully"
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<DefaultResponseDto> loginUser(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(
                new DefaultResponseDto(
                        SUCCESS,
                        Map.of("data", authenticationService.login(request)),
                        "User logged in successfully"
                )
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<DefaultResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(
                new DefaultResponseDto(
                        SUCCESS,
                        Map.of("data", authenticationService.refreshToken(request)),
                        "Token refreshed successfully"
                )
        );
    }
}
