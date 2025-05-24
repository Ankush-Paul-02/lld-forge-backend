package com.devmare.lldforge.data.entity;

import com.devmare.lldforge.data.enums.MentorApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "mentor_applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"email"})
)
public class MentorApplication extends BaseEntity {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Experience is required")
    @Size(min = 10, message = "Experience must be at least 10 characters")
    private String experience;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MentorApplicationStatus status = MentorApplicationStatus.PENDING;

    private Long appliedAt = Instant.now().getEpochSecond();

    private Long reviewedAt;

    @OneToOne
    private User reviewedBy;

    @Builder.Default
    private Boolean isUnderReview = false;

    private String rejectionReason;

    @OneToOne
    private User user;
}
