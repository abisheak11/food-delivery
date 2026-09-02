package com.fooddelivery.payment.repository;

import com.fooddelivery.payment.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByPaymentIdOrderByCreatedAtDesc(Long paymentId);

    List<Refund> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
