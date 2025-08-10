package com.devmare.lldforge.business.service;

import com.devmare.lldforge.business.dto.RazorpayCreateOrderRequestDto;
import com.devmare.lldforge.business.dto.RazorpayOrderResponseDto;

import com.razorpay.RazorpayException;
import org.springframework.http.HttpHeaders;

public interface RazorpayService {

    RazorpayOrderResponseDto createOrder(RazorpayCreateOrderRequestDto request);

    void verifyAndProcessWebhook(String payload, HttpHeaders httpHeaders) throws RazorpayException;
}
