package com.devmare.lldforge.data.entity;

import com.devmare.lldforge.data.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    private String email;
    private String githubId;
    private String username;
    private String name;
    private String avatarUrl;
    private String profileUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.STUDENT;
    private Long joinedAt;

    @Builder.Default
    private Boolean isEmailVerified = false;
}
