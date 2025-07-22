package com.devmare.lldforge.data.enums;

public enum MentorshipSessionStatus {
    PENDING,         // Session created, waiting for payment
    PAID,            // Payment successful (confirmed by webhook)
    COMPLETED,       // Session happened
    CANCELLED        // Session was cancelled (by mentor/student)
}
