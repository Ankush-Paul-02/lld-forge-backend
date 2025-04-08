package com.devmare.lldforge.business.service.impl;

import com.devmare.lldforge.business.service.EmailService;
import com.devmare.lldforge.business.service.EmailVerificationService;
import com.devmare.lldforge.data.entity.EmailVerificationToken;
import com.devmare.lldforge.data.entity.User;
import com.devmare.lldforge.data.exception.AppInfoException;
import com.devmare.lldforge.data.repository.EmailVerificationTokenRepository;
import com.devmare.lldforge.data.repository.UserRepository;
import com.devmare.lldforge.security.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Override
    public void sendVerificationEmail(String email) {

        User currentAuthenticatedUser = customOAuth2UserService.getCurrentAuthenticatedUser();
        currentAuthenticatedUser.setEmail(email);
        userRepository.save(currentAuthenticatedUser);

        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .userId(currentAuthenticatedUser.getId())
                .token(token)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        emailVerificationTokenRepository.save(verificationToken);

        String verificationLink = "http://localhost:8081/api/v1/verify-email?token=" + token;

        Map<String, Object> variables = Map.of(
                "name", currentAuthenticatedUser.getName(),
                "verificationLink", verificationLink
        );

        emailService.sendTemplateEmail(
                currentAuthenticatedUser.getEmail(),
                "Verify your email address",
                "email-verification",
                variables
        );
    }

    @Override
    public void verifyEmail(String token) {

        Optional<EmailVerificationToken> optionalEmailVerificationToken = emailVerificationTokenRepository.findByToken(token);
        if (optionalEmailVerificationToken.isEmpty()) {
            throw new AppInfoException("Invalid or expired verification token", HttpStatus.NOT_FOUND);
        }

        EmailVerificationToken emailVerificationToken = optionalEmailVerificationToken.get();

        if (emailVerificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new AppInfoException("Verification token has expired", HttpStatus.GONE);
        }

        Optional<User> optionalUser = userRepository.findById(emailVerificationToken.getUserId());
        if (optionalUser.isEmpty()) {
            throw new AppInfoException("User not found", HttpStatus.CONFLICT);
        }
        User user = optionalUser.get();

        user.setIsEmailVerified(true);
        userRepository.save(user);

        emailVerificationTokenRepository.delete(emailVerificationToken);
    }
}
