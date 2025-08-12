package com.devmare.lldforge.business.service.impl;

import com.devmare.lldforge.business.dto.MentorshipSessionResponseDto;
import com.devmare.lldforge.business.service.MentorshipService;
import com.devmare.lldforge.data.entity.RazorpayOrder;
import com.devmare.lldforge.data.entity.User;
import com.devmare.lldforge.data.repository.MentorshipSessionRepository;
import com.devmare.lldforge.data.repository.RazorpayOrderRepository;
import com.devmare.lldforge.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentorshipServiceImpl implements MentorshipService {

    private final AuthenticationService authenticationService;
    private final RazorpayOrderRepository razorpayOrderRepository;
    private final MentorshipSessionRepository mentorshipSessionRepository;

    @Override
    public List<MentorshipSessionResponseDto> getMentorshipSessionByStudent() {

        User currentUser = authenticationService.fetchAuthenticatedUser();

        return mentorshipSessionRepository.findAllByStudentOrderByCreatedAtDesc(currentUser).stream().map(
                session -> {
                    Optional<RazorpayOrder> optionalRazorpayOrder = razorpayOrderRepository.findBySession(session);
                    if (optionalRazorpayOrder.isEmpty()) {
                        log.info("Razorpay order not found for session with id {}", session.getId());
                        return null;
                    }
                    RazorpayOrder razorpayOrder = optionalRazorpayOrder.get();

                    return MentorshipSessionResponseDto.builder()
                            .id(session.getId())
                            .mentorId(session.getMentor().getId())
                            .mentorName(session.getMentor().getName())
                            .mentorshipSessionStatus(session.getStatus().name())
                            .createdAt(session.getCreatedAt())
                            .scheduledAt(session.getScheduledAt())
                            .durationInMinutes(session.getDurationInMinutes())
                            .startedAt(session.getStartedAt())
                            .endedAt(session.getEndedAt())
                            .paymentVerified(session.getPaymentVerified())
                            .meetingLink(session.getMeetingLink())
                            .orderId(razorpayOrder.getRazorpayOrderId())
                            .amount(razorpayOrder.getAmount())
                            .orderStatus(razorpayOrder.getStatus().name())
                            .paymentAt(razorpayOrder.getPaymentAt())
                            .build();
                }
        ).toList();
    }
}
