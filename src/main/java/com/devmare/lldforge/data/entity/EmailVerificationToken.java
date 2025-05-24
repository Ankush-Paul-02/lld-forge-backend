package com.devmare.lldforge.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken extends BaseEntity {

    @OneToOne
    private User user;
    private String token;
    private Instant createdAt;
    private Instant expiresAt;
}
