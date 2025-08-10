package com.devmare.lldforge.data.entity;

import com.devmare.lldforge.data.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "razorpay_orders")
public class RazorpayOrder extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String razorpayOrderId; // from Razorpay

    private String paymentId; // filled after payment success

    private Long paymentAt;

    @Column(nullable = false)
    private Integer amount; // in paise

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @ManyToOne
    @JoinColumn(name = "payer_id")
    private User payer;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @OneToOne
    @JoinColumn(name = "session_id")
    private MentorshipSession session;

    private String receiptId; // for internal tracking

    private Long createdAt;
}
