package com.fooddelivery.payment.dto;

import com.fooddelivery.payment.model.PaymentMethod;
import com.fooddelivery.payment.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {

    private Long paymentId;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private String gatewayProvider;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
