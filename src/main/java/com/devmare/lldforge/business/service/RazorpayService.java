package com.devmare.lldforge.business.service;

import com.devmare.lldforge.business.dto.RazorpayCreateOrderRequestDto;
import com.devmare.lldforge.business.dto.RazorpayOrderResponseDto;

public interface RazorpayService {

    RazorpayOrderResponseDto createOrder(RazorpayCreateOrderRequestDto request);

    void verifyAndProcessWebhook(String payload, String signature);
}
