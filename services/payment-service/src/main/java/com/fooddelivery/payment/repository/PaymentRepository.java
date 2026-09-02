package com.fooddelivery.payment.repository;

import com.fooddelivery.payment.model.Payment;
import com.fooddelivery.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByOrderNumber(String orderNumber);

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
}
