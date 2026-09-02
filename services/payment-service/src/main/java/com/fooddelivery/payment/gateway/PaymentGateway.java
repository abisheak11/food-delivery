package com.fooddelivery.payment.gateway;

import com.fooddelivery.payment.dto.PaymentRequestDto;
import com.fooddelivery.payment.dto.RefundRequestDto;
import com.fooddelivery.payment.dto.WebhookPayloadDto;

public interface PaymentGateway {

    /**
     * Authorizes and captures a payment charge through the payment gateway.
     *
     * @param request the payment request payload
     * @return result of the gateway transaction
     */
    GatewayTransactionResult processPayment(PaymentRequestDto request);

    /**
     * Issues a full or partial refund for a previously captured transaction.
     *
     * @param request the refund request payload
     * @param originalTransactionId the original payment transaction ID
     * @return result of the refund transaction
     */
    GatewayTransactionResult processRefund(RefundRequestDto request, String originalTransactionId);

    /**
     * Verifies the authenticity and signature of an incoming webhook from the payment provider.
     *
     * @param payload the webhook payload
     * @return true if webhook signature is valid
     */
    boolean verifyWebhook(WebhookPayloadDto payload);

    /**
     * Returns the name/identifier of this gateway provider (e.g. STRIPE, RAZORPAY, MOCK_GATEWAY).
     */
    String getProviderName();
}
