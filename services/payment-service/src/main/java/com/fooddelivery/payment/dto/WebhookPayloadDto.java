package com.fooddelivery.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPayloadDto {

    private String eventType; // e.g. "payment.succeeded", "payment.failed", "refund.processed"
    private String transactionId;
    private Long orderId;
    private BigDecimal amount;
    private String status;
    private String signature; // Simulated webhook HMAC signature
    private Map<String, Object> metadata;
}
