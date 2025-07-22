package com.devmare.lldforge.business.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RazorpayOrderResponseDto {
    private String orderId;
    private int amount;
    private String currency;
    private Long sessionId;
}
