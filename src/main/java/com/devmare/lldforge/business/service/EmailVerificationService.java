package com.devmare.lldforge.business.service;

public interface EmailVerificationService {
    void sendVerificationEmail(String email);

    void verifyEmail(String token);
}
