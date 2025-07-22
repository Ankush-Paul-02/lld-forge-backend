package com.devmare.lldforge.business.service.impl;

import com.devmare.lldforge.business.dto.RazorpayCreateOrderRequestDto;
import com.devmare.lldforge.business.dto.RazorpayOrderResponseDto;
import com.devmare.lldforge.business.service.RazorpayService;
import com.devmare.lldforge.data.entity.MentorshipSession;
import com.devmare.lldforge.data.entity.RazorpayOrder;
import com.devmare.lldforge.data.entity.User;
import com.devmare.lldforge.data.enums.MentorshipSessionStatus;
import com.devmare.lldforge.data.enums.OrderStatus;
import com.devmare.lldforge.data.exception.AppInfoException;
import com.devmare.lldforge.data.repository.MentorshipSessionRepository;
import com.devmare.lldforge.data.repository.RazorpayOrderRepository;
import com.devmare.lldforge.data.repository.UserRepository;
import com.devmare.lldforge.data.utils.RazorpaySignatureUtil;
import com.devmare.lldforge.security.AuthenticationService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayServiceImpl implements RazorpayService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final RazorpayOrderRepository razorpayOrderRepository;
    private final MentorshipSessionRepository mentorshipSessionRepository;
    @Value("${razorpay.api.key}")
    private String RAZORPAY_API_KEY;
    @Value("${razorpay.api.secret}")
    private String RAZORPAY_API_SECRET;

    @Override
    @Transactional
    public RazorpayOrderResponseDto createOrder(RazorpayCreateOrderRequestDto request) {
        log.info("Creating order with request: {}", request);
        User currentUser = authenticationService.fetchAuthenticatedUser();
        Optional<User> optionalReceiver = userRepository.findById(request.getReceiverId());
        if (optionalReceiver.isEmpty()) {
            throw new AppInfoException("Receiver not found", HttpStatus.NOT_FOUND);
        }
        User receiver = optionalReceiver.get();

        /// Create the mentorship session first
        MentorshipSession session = MentorshipSession.builder()
                .mentor(receiver)
                .student(currentUser)
                .message(request.getMessage())
                .scheduledAt(request.getScheduledAt())
                .durationInMinutes(request.getDurationInMinutes())
                .status(MentorshipSessionStatus.PENDING)
                .paymentVerified(false)
                .createdAt(Instant.now().getEpochSecond())
                .build();

        mentorshipSessionRepository.save(session);

        try {
            ///  Create the Razorpay order
            RazorpayClient client = new RazorpayClient(RAZORPAY_API_KEY, RAZORPAY_API_SECRET);

            JSONObject options = new JSONObject();
            options.put("amount", request.getAmount());
            options.put("currency", request.getCurrency());
            options.put("receipt", "rcpt_" + Instant.now().getEpochSecond());
            options.put("payment_capture", 1);

            Order razorpayOrder = client.orders.create(options);
            log.info("Razorpay order created: {}", razorpayOrder);

            RazorpayOrder order = RazorpayOrder.builder()
                    .razorpayOrderId(razorpayOrder.get("id"))
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(OrderStatus.CREATED)
                    .payer(currentUser)
                    .receiver(receiver)
                    .session(session)
                    .receiptId(razorpayOrder.get("receipt"))
                    .createdAt(Instant.now().getEpochSecond())
                    .build();

            razorpayOrderRepository.save(order);
            return RazorpayOrderResponseDto.builder()
                    .orderId(order.getRazorpayOrderId())
                    .amount(order.getAmount())
                    .currency(order.getCurrency())
                    .sessionId(session.getId())
                    .build();
        } catch (RazorpayException e) {
            throw new AppInfoException("Error while creating Razorpay order: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public void verifyAndProcessWebhook(String payload, String signature) {
        log.info("[Webhook] Received Razorpay webhook. Verifying signature...");
        /// Verify signature
        boolean isValid = RazorpaySignatureUtil.verifySignature(payload, signature, RAZORPAY_API_SECRET);
        if (!isValid) {
            log.warn("[Webhook] Invalid Razorpay webhook signature. Possible spoof attempt.");
            throw new AppInfoException("Invalid webhook signature", HttpStatus.FORBIDDEN);
        }

        log.info("[Webhook] Signature verified. Parsing event payload...");
        /// Parse payload
        JSONObject event = new JSONObject(payload);
        String eventType = event.getString("event");

        if (!"payment.captured".equals(eventType)) {
            log.info("[Webhook] Ignored event type: {}", eventType);
            return;
        }

        JSONObject paymentEntity = event.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
        String razorpayOrderId = paymentEntity.getString("order_id");
        String razorpayPaymentId = paymentEntity.getString("id");
        int amount = paymentEntity.optInt("amount", 0);

        if (razorpayOrderId == null || razorpayPaymentId == null) {
            log.error("[Webhook] Missing required fields in Razorpay payload. Order ID: {}, Payment ID: {}", razorpayOrderId, razorpayPaymentId);
            throw new AppInfoException("Invalid webhook payload", HttpStatus.BAD_REQUEST);
        }

        log.info("[Webhook] Payment captured for orderId={}, paymentId={}, amount={}", razorpayOrderId, razorpayPaymentId, amount);

        /// Lookup order
        Optional<RazorpayOrder> optionalRazorpayOrder = razorpayOrderRepository.findByRazorpayOrderId(razorpayOrderId);
        if (optionalRazorpayOrder.isEmpty()) {
            log.error("[Webhook] RazorpayOrder not found for razorpayOrderId={}", razorpayOrderId);
            throw new AppInfoException("Order not found for webhook", HttpStatus.NOT_FOUND);
        }

        RazorpayOrder order = optionalRazorpayOrder.get();

        if (order.getStatus() == OrderStatus.PAID) {
            log.info("[Webhook] Order already marked as paid. Skipping duplicate webhook for orderId={}", razorpayOrderId);
            return;
        }

        // Update order
        order.setStatus(OrderStatus.PAID);
        order.setPaymentId(razorpayPaymentId);
        razorpayOrderRepository.save(order);
        log.info("[Webhook] Order status updated to PAID for orderId={}", razorpayOrderId);

        // Update session
        MentorshipSession session = order.getSession();
        if (session != null) {
            session.setStatus(MentorshipSessionStatus.PAID);
            session.setPaymentVerified(true);
            mentorshipSessionRepository.save(session);
        } else {
            log.warn("[Webhook] No mentorship session linked to orderId={}", razorpayOrderId);
        }

        log.info("Webhook processed for orderId={}, paymentId={}", razorpayOrderId, razorpayPaymentId);
    }
}
