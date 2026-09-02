package com.fooddelivery.payment.service;

import com.fooddelivery.payment.dto.*;
import com.fooddelivery.payment.event.OrderCreatedEvent;

import java.util.List;

public interface PaymentService {

    /**
     * Processes a payment request idempotently, delegates to the gateway, updates payment record,
     * and publishes a PaymentProcessedEvent to Kafka.
     */
    PaymentResponseDto processPayment(PaymentRequestDto request, Long authenticatedUserId);

    /**
     * Retrieves payment details by payment ID.
     */
    PaymentResponseDto getPaymentById(Long paymentId, Long authenticatedUserId, boolean isAdmin);

    /**
     * Retrieves payment details by order ID.
     */
    PaymentResponseDto getPaymentByOrderId(Long orderId, Long authenticatedUserId, boolean isAdmin);

    /**
     * Retrieves all payments belonging to a specific user.
     */
    List<PaymentResponseDto> getPaymentsByUserId(Long userId);

    /**
     * Processes a refund for an existing successful payment.
     */
    RefundResponseDto processRefund(RefundRequestDto request, Long authenticatedUserId, boolean isAdmin);

    /**
     * Retrieves all refund transactions for a given payment.
     */
    List<RefundResponseDto> getRefundsByPaymentId(Long paymentId);

    /**
     * Handles incoming Kafka order-created event to pre-register payment metadata.
     */
    void handleOrderCreatedEvent(OrderCreatedEvent event);

    /**
     * Handles gateway webhook notification callbacks asynchronously.
     */
    PaymentResponseDto handleWebhook(WebhookPayloadDto payload);
}
