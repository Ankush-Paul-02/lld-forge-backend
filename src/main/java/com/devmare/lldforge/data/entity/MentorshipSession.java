package com.devmare.lldforge.data.entity;

import com.devmare.lldforge.data.enums.MentorshipSessionStatus;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mentorship_sessions")
public class MentorshipSession extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MentorshipSessionStatus status = MentorshipSessionStatus.PENDING;

    private String message;

    private Long createdAt;

    private Long scheduledAt;       // ⏰ Session scheduled time (Epoch Seconds)
    private Integer durationInMinutes; // ⏱️ Duration in minutes

    private Long startedAt;         // ⏱️ When session started (nullable)
    private Long endedAt;           // ⏹️ When session ended (nullable)

    private Boolean paymentVerified; // ✅ Set true after webhook confirms
    private String meetingLink;      // 🔗 Zoom/Meet link generated post-payment
}
