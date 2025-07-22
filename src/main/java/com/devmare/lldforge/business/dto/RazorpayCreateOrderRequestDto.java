package com.devmare.lldforge.business.dto;


import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@Builder
public class RazorpayCreateOrderRequestDto {

    @Min(value = 1000, message = "Amount must be at least ₹10 (1000 paise)")
    private int amount;

    @NotBlank(message = "Currency must not be blank")
    @Pattern(regexp = "INR", message = "Only INR currency is supported")
    private String currency;

    @NotNull(message = "Receiver ID (mentor ID) is required")
    private Long receiverId;

    @Size(max = 500, message = "Message can't be longer than 500 characters")
    private String message;

    @NotNull(message = "Scheduled time is required")
    @Future(message = "Scheduled time must be in the future")
    private Long scheduledAt;

    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 180, message = "Duration must not exceed 180 minutes")
    private int durationInMinutes;
}
