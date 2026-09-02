package com.fooddelivery.payment.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds", indexes = {
        @Index(name = "idx_refund_payment_id", columnList = "payment_id"),
        @Index(name = "idx_refund_order_id", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false, length = 32)
    private RefundStatus refundStatus;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "refund_transaction_id", length = 64)
    private String refundTransactionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
