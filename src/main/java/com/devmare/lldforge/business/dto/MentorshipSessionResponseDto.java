package com.devmare.lldforge.business.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MentorshipSessionResponseDto {
    private Long id;
    private Long mentorId;
    private String mentorName;
    private String mentorshipSessionStatus;
    private Long createdAt;
    private Long scheduledAt;
    private Integer durationInMinutes;
    private Long startedAt;
    private Long endedAt;
    private Boolean paymentVerified;
    private String meetingLink;
    private String orderId;
    private Integer amount;
    private Long paymentAt;
    private String orderStatus;
}
