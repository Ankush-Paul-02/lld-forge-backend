package com.devmare.lldforge.data.entity;

import com.devmare.lldforge.data.enums.MentorApplicationStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "mentor_applications")
public class MentorApplication {

    @Id
    private String id;

    private String studentId;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Indexed(unique = true)
    private String email;

    @NotBlank(message = "Experience is required")
    @Size(min = 10, message = "Experience must be at least 10 characters")
    private String experience;

    @Builder.Default
    private MentorApplicationStatus status = MentorApplicationStatus.PENDING;

    private Long appliedAt = Instant.now().getEpochSecond();

    private Long reviewedAt;

    private String reviewedBy;

    @Builder.Default
    private Boolean isUnderReview = false;

    private String rejectionReason;
}
