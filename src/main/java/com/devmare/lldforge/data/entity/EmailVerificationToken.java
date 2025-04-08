package com.devmare.lldforge.data.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "email_verification_tokens")
public class EmailVerificationToken {

    @Id
    private String id;

    private String userId;
    private String token;
    private Instant createdAt;
    private Instant expiresAt;
}
