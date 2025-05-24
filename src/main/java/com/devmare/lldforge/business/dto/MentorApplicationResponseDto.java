package com.devmare.lldforge.business.dto;

import com.devmare.lldforge.data.entity.MentorApplication;
import com.devmare.lldforge.data.enums.MentorApplicationStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MentorApplicationResponseDto {

    private Long id;

    private Long userId;

    private String email;

    private String experience;

    private MentorApplicationStatus status;

    private Long appliedAt;

    private Long reviewedAt;

    private Boolean isUnderReview;

    public static MentorApplicationResponseDto fromEntity(MentorApplication mentorApplication) {
        return MentorApplicationResponseDto.builder()
                .id(mentorApplication.getId())
                .userId(mentorApplication.getUser() != null ? mentorApplication.getUser().getId() : null)
                .email(mentorApplication.getEmail())
                .experience(mentorApplication.getExperience())
                .status(mentorApplication.getStatus())
                .appliedAt(mentorApplication.getAppliedAt())
                .reviewedAt(mentorApplication.getReviewedAt())
                .isUnderReview(mentorApplication.getIsUnderReview())
                .build();
    }
}
