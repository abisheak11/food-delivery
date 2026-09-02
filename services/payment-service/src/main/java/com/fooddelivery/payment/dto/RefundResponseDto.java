package com.fooddelivery.payment.dto;

import com.fooddelivery.payment.model.RefundStatus;
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
public class RefundResponseDto {

    private Long refundId;
    private Long paymentId;
    private Long orderId;
    private BigDecimal amount;
    private RefundStatus refundStatus;
    private String reason;
    private String refundTransactionId;
    private LocalDateTime createdAt;
}
