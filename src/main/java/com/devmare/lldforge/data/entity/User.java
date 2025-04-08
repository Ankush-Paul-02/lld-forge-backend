package com.devmare.lldforge.data.entity;

import com.devmare.lldforge.data.enums.Role;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String email;
    private String githubId;
    private String username;
    private String name;
    private String avatarUrl;
    private String profileUrl;

    @Builder.Default
    private Role role = Role.STUDENT;
    private Long joinedAt;

    @Builder.Default
    private Boolean isEmailVerified = false;
}
