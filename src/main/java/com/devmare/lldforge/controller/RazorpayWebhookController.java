package com.devmare.lldforge.controller;

import com.devmare.lldforge.business.service.RazorpayService;
import com.devmare.lldforge.data.exception.AppInfoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/webhook")
public class RazorpayWebhookController {

    private final RazorpayService razorpayService;

    @PostMapping("/payment")
    public ResponseEntity<String> handlePaymentWebhook(
            @RequestHeader HttpHeaders httpHeaders,
            @RequestBody String payload
    ) {
        log.info("[WebhookController] Incoming Razorpay webhook");
        log.info("[WebhookController] Headers: {}", httpHeaders);
        log.info("[WebhookController] Payload length: {}", payload.length());

        try {
            razorpayService.verifyAndProcessWebhook(payload, httpHeaders);
            log.info("[WebhookController] Razorpay webhook processed successfully");
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (AppInfoException e) {
            if (e.getStatus() == HttpStatus.FORBIDDEN) {
                log.warn("[WebhookController] Signature verification failed: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
            }
            log.error("[WebhookController] Application error: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            log.error("[WebhookController] Unexpected error processing webhook", e);
            return ResponseEntity.internalServerError().body("Webhook processing failed");
        }
    }
}