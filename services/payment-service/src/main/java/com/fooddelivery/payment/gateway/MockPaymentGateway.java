package com.fooddelivery.payment.gateway;

import com.fooddelivery.payment.dto.PaymentRequestDto;
import com.fooddelivery.payment.dto.RefundRequestDto;
import com.fooddelivery.payment.dto.WebhookPayloadDto;
import com.fooddelivery.payment.model.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * MockPaymentGateway simulates real-world payment gateway providers (like Stripe, Razorpay, or PayPal).
 * It demonstrates how gateway authorization, charge capture, card decline simulations,
 * and refunds work without requiring live merchant API credentials.
 */
@Slf4j
@Component
public class MockPaymentGateway implements PaymentGateway {

    public static final String PROVIDER_NAME = "MOCK_GATEWAY";

    @Override
    public GatewayTransactionResult processPayment(PaymentRequestDto request) {
        log.info("Processing payment via [{}] for Order ID: {}, Amount: {}",
                PROVIDER_NAME, request.getOrderId(), request.getAmount());

        // Cash on Delivery is auto-approved as pending collection on arrival
        if (request.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            String codTxnId = "cod_" + UUID.randomUUID().toString().substring(0, 16);
            return GatewayTransactionResult.builder()
                    .successful(true)
                    .transactionId(codTxnId)
                    .gatewayResponseCode("COD_PENDING")
                    .gatewayMessage("Cash on delivery scheduled successfully")
                    .capturedAmount(request.getAmount())
                    .build();
        }

        // Simulate Card specific failure testing rules
        if (request.getCardNumber() != null) {
            String sanitizedCard = request.getCardNumber().replaceAll("\\s+", "");
            if (sanitizedCard.endsWith("0002")) {
                return GatewayTransactionResult.builder()
                        .successful(false)
                        .transactionId("txn_err_" + UUID.randomUUID().toString().substring(0, 12))
                        .gatewayResponseCode("CARD_DECLINED")
                        .gatewayMessage("Your card was declined by the issuing bank.")
                        .build();
            } else if (sanitizedCard.endsWith("0003")) {
                return GatewayTransactionResult.builder()
                        .successful(false)
                        .transactionId("txn_err_" + UUID.randomUUID().toString().substring(0, 12))
                        .gatewayResponseCode("INSUFFICIENT_FUNDS")
                        .gatewayMessage("Insufficient funds in the account.")
                        .build();
            } else if (sanitizedCard.endsWith("0004")) {
                return GatewayTransactionResult.builder()
                        .successful(false)
                        .transactionId("txn_err_" + UUID.randomUUID().toString().substring(0, 12))
                        .gatewayResponseCode("EXPIRED_CARD")
                        .gatewayMessage("The card expiry date is invalid or expired.")
                        .build();
            }
        }

        // Simulate UPI specific failure testing rules
        if (request.getUpiId() != null) {
            String upi = request.getUpiId().toLowerCase();
            if (upi.contains("fail") || upi.contains("invalid")) {
                return GatewayTransactionResult.builder()
                        .successful(false)
                        .transactionId("upi_err_" + UUID.randomUUID().toString().substring(0, 12))
                        .gatewayResponseCode("VPA_NOT_FOUND")
                        .gatewayMessage("Virtual Payment Address (UPI ID) does not exist.")
                        .build();
            }
        }

        // Successful Payment Authorization & Capture
        String txnId = "txn_" + UUID.randomUUID().toString().replace("-", "");
        return GatewayTransactionResult.builder()
                .successful(true)
                .transactionId(txnId)
                .gatewayResponseCode("200_APPROVED")
                .gatewayMessage("Payment authorized and captured successfully.")
                .capturedAmount(request.getAmount())
                .build();
    }

    @Override
    public GatewayTransactionResult processRefund(RefundRequestDto request, String originalTransactionId) {
        log.info("Processing refund via [{}] for Payment ID: {}, Amount: {}, Original Txn: {}",
                PROVIDER_NAME, request.getPaymentId(), request.getAmount(), originalTransactionId);

        String refTxnId = "ref_" + UUID.randomUUID().toString().replace("-", "");
        return GatewayTransactionResult.builder()
                .successful(true)
                .transactionId(refTxnId)
                .gatewayResponseCode("REFUND_SUCCESS")
                .gatewayMessage("Refund processed successfully to original payment method.")
                .capturedAmount(request.getAmount())
                .build();
    }

    @Override
    public boolean verifyWebhook(WebhookPayloadDto payload) {
        // In real implementations (e.g. Stripe Webhook), you compute HMAC-SHA256 signature
        // with your Webhook Signing Secret to verify payload authenticity.
        log.info("Verifying webhook event: {} with signature: {}", payload.getEventType(), payload.getSignature());
        return payload.getSignature() != null && !payload.getSignature().isBlank();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
}
