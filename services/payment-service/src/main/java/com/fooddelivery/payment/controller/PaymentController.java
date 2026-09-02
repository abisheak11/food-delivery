package com.fooddelivery.payment.controller;

import com.fooddelivery.payment.dto.*;
import com.fooddelivery.payment.security.UserPrincipal;
import com.fooddelivery.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Endpoints for processing payments, viewing transaction status, processing refunds, and receiving gateway webhooks")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    @Operation(summary = "Process a payment", description = "Authorizes and captures payment for an order through the configured payment gateway")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payment details or card declined"),
            @ApiResponse(responseCode = "409", description = "Order already paid or duplicate idempotency key")
    })
    public ResponseEntity<PaymentResponseDto> processPayment(
            @Valid @RequestBody PaymentRequestDto request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Long authUserId = currentUser != null ? currentUser.getId() : request.getUserId();
        PaymentResponseDto response = paymentService.processPayment(request, authUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Fetch payment transaction details by its primary ID")
    public ResponseEntity<PaymentResponseDto> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        boolean isAdmin = currentUser != null && currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Long authUserId = currentUser != null ? currentUser.getId() : null;

        PaymentResponseDto response = paymentService.getPaymentById(id, authUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by Order ID", description = "Fetch payment transaction details for a given food order")
    public ResponseEntity<PaymentResponseDto> getPaymentByOrderId(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        boolean isAdmin = currentUser != null && currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Long authUserId = currentUser != null ? currentUser.getId() : null;

        PaymentResponseDto response = paymentService.getPaymentByOrderId(orderId, authUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all payments by User ID", description = "Fetch payment history for a user")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(paymentService.getPaymentsByUserId(userId));
    }

    @PostMapping("/refund")
    @Operation(summary = "Initiate a refund", description = "Issues a refund for a previously captured payment transaction")
    public ResponseEntity<RefundResponseDto> processRefund(
            @Valid @RequestBody RefundRequestDto request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        boolean isAdmin = currentUser != null && currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Long authUserId = currentUser != null ? currentUser.getId() : null;

        RefundResponseDto response = paymentService.processRefund(request, authUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}/refunds")
    @Operation(summary = "Get refunds for a payment", description = "Lists all refund operations associated with a specific payment")
    public ResponseEntity<List<RefundResponseDto>> getRefundsByPaymentId(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId) {

        return ResponseEntity.ok(paymentService.getRefundsByPaymentId(paymentId));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Payment Gateway Webhook Callback", description = "Receives asynchronous event callbacks from payment providers (e.g., Stripe, Razorpay)")
    public ResponseEntity<PaymentResponseDto> handleWebhook(
            @RequestBody WebhookPayloadDto payload) {

        log.info("Received Payment Gateway Webhook callback for transaction: {}", payload.getTransactionId());
        PaymentResponseDto response = paymentService.handleWebhook(payload);
        return ResponseEntity.ok(response);
    }
}
