package com.fooddelivery.payment.dto;

import com.fooddelivery.payment.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    private String orderNumber;

    private Long userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String currency;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String idempotencyKey;

    // Optional card/payment details for gateway simulation
    private String cardNumber;
    private String cardCvv;
    private String cardExpiry;
    private String upiId;
}
