package com.devmare.lldforge.controller;

import com.devmare.lldforge.business.dto.DefaultResponseDto;
import com.devmare.lldforge.data.entity.User;
import com.devmare.lldforge.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.devmare.lldforge.business.dto.DefaultResponseDto.Status.SUCCESS;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final AuthenticationService authenticationService;

    @GetMapping("/me")
    public ResponseEntity<DefaultResponseDto> getCurrentUserDetails() {
        User currentUser = authenticationService.fetchAuthenticatedUser();
        return ResponseEntity.ok(new DefaultResponseDto(
                SUCCESS,
                Map.of("user", currentUser),
                "Fetched current authenticated user details"
        ));
    }
}
