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
import com.devmare.lldforge.security.AuthenticationService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
    public void verifyAndProcessWebhook(String payload, HttpHeaders httpHeaders) throws RazorpayException {
        String eventId = httpHeaders.getFirst("x-razorpay-event-id");
        if (eventId == null || eventId.isBlank()) {
            throw new AppInfoException("Missing Razorpay event ID", HttpStatus.BAD_REQUEST);
        }

        String webhookSignature = httpHeaders.getFirst("x-razorpay-signature");
        if (webhookSignature == null || webhookSignature.isBlank()) {
            log.error("[Webhook] Missing Razorpay signature");
            throw new AppInfoException("Missing Razorpay signature", HttpStatus.BAD_REQUEST);
        }

        boolean isSignatureValid = Utils.verifyWebhookSignature(payload, webhookSignature, RAZORPAY_API_SECRET);
        if (!isSignatureValid) {
            log.error("[Webhook] Invalid webhook signature");
            throw new AppInfoException("Invalid webhook signature", HttpStatus.UNAUTHORIZED);
        }

        log.info("[Webhook] Signature verification successful for event {}", eventId);
        log.info("[Webhook] Processing event {} from Razorpay", eventId);

        JSONObject event = new JSONObject(payload);
        String eventType = event.getString("event");

        // Extract payment details
        JSONObject paymentEntity = event.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String razorpayOrderId = paymentEntity.optString("order_id", null);
        String razorpayPaymentId = paymentEntity.optString("id", null);
        int amount = paymentEntity.optInt("amount", -1);

        if (razorpayOrderId == null || razorpayPaymentId == null) {
            log.error("[Webhook] Missing required fields for event {}", eventId);
            throw new AppInfoException("Invalid webhook payload", HttpStatus.BAD_REQUEST);
        }

        Optional<RazorpayOrder> optionalOrder = razorpayOrderRepository.findByRazorpayOrderId(razorpayOrderId);
        if (optionalOrder.isEmpty()) {
            throw new AppInfoException("Order not found", HttpStatus.NOT_FOUND);
        }

        RazorpayOrder order = optionalOrder.get();

        // Handle only captured & failed events
        switch (eventType) {
            case "payment.captured":
                handlePaymentCaptured(order, amount, razorpayPaymentId, eventId);
                break;

            case "payment.failed":
                handlePaymentFailed(order, razorpayPaymentId, eventId);
                break;

            default:
                log.info("[Webhook] Ignoring event type: {} for event {}", eventType, eventId);
        }
    }

    private void handlePaymentCaptured(RazorpayOrder order, int amount, String paymentId, String eventId) {
        if (order.getStatus() == OrderStatus.PAID) {
            log.info("[Webhook] Order already paid, skipping event {}", eventId);
            return;
        }
        if (amount != order.getAmount()) {
            throw new AppInfoException("Amount mismatch", HttpStatus.BAD_REQUEST);
        }
        order.setStatus(OrderStatus.PAID);
        order.setPaymentId(paymentId);
        order.setPaymentAt(Instant.now().getEpochSecond());
        razorpayOrderRepository.save(order);

        MentorshipSession session = order.getSession();
        if (session != null) {
            session.setStatus(MentorshipSessionStatus.PAID);
            session.setPaymentVerified(true);
            mentorshipSessionRepository.save(session);
        }
        log.info("[Webhook] Payment captured and order marked as PAID for event {}", eventId);
    }

    private void handlePaymentFailed(RazorpayOrder order, String paymentId, String eventId) {
        order.setStatus(OrderStatus.FAILED);
        order.setPaymentId(paymentId);
        razorpayOrderRepository.save(order);

        MentorshipSession session = order.getSession();
        if (session != null) {
            session.setStatus(MentorshipSessionStatus.CANCELLED);
            session.setPaymentVerified(false);
            mentorshipSessionRepository.save(session);
        }
        log.warn("[Webhook] Payment failed for event {}", eventId);
    }
}
