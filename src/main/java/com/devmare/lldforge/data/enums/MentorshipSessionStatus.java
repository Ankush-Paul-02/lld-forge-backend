package com.devmare.lldforge.data.enums;

public enum MentorshipSessionStatus {
    PENDING,         // Session created, waiting for payment
    PAID,            // Payment successful (confirmed by webhook)
    BOOKED,          // Session scheduled but no meeting link yet
    MEETING_LINK_PROVIDED, // Meeting link shared with the mentee
    COMPLETED,       // Session happened
    CANCELLED        // Session was cancelled (by mentor/student)
}
