package com.devmare.lldforge.data.repository;

import com.devmare.lldforge.data.entity.MentorshipSession;
import com.devmare.lldforge.data.entity.RazorpayOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RazorpayOrderRepository extends JpaRepository<RazorpayOrder, Long> {

    Optional<RazorpayOrder> findByRazorpayOrderId(String razorpayOrderId);

    Optional<RazorpayOrder> findBySession(MentorshipSession session);
}