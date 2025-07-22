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
            @RequestHeader HttpHeaders headers,
            @RequestBody String payload
    ) {
        String signature = getHeaderIgnoreCase(headers);

        log.info("[WebhookController] Incoming Razorpay webhook -> Signature: {}, Payload Length: {}",
                signature, payload != null ? payload.length() : 0);

        if (signature == null || signature.isBlank()) {
            log.warn("[WebhookController] Missing or empty Razorpay signature header.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing X-Razorpay-Signature header.");
        }

        try {
            razorpayService.verifyAndProcessWebhook(payload, signature);
            log.info("[WebhookController] Razorpay webhook processed successfully.");
            return ResponseEntity.ok("Webhook processed successfully.");
        } catch (AppInfoException e) {
            if (e.getStatus() == HttpStatus.FORBIDDEN) {
                log.warn("[WebhookController] Signature verification failed: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature.");
            }
            log.error("[WebhookController] Known application error: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            log.error("[WebhookController] Unexpected exception during webhook processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing failed.");
        }
    }

    private String getHeaderIgnoreCase(HttpHeaders headers) {
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase("X-Razorpay-Signature"))
                .findFirst()
                .map(entry -> entry.getValue().get(0))
                .orElse(null);
    }
}
