package com.fooddelivery.payment.service;

import com.fooddelivery.payment.dto.*;
import com.fooddelivery.payment.event.OrderCreatedEvent;
import com.fooddelivery.payment.event.PaymentProcessedEvent;
import com.fooddelivery.payment.exception.DuplicatePaymentException;
import com.fooddelivery.payment.exception.PaymentNotFoundException;
import com.fooddelivery.payment.exception.PaymentProcessingException;
import com.fooddelivery.payment.exception.UnauthorizedPaymentAccessException;
import com.fooddelivery.payment.gateway.GatewayTransactionResult;
import com.fooddelivery.payment.gateway.PaymentGateway;
import com.fooddelivery.payment.kafka.PaymentEventProducer;
import com.fooddelivery.payment.model.Payment;
import com.fooddelivery.payment.model.PaymentStatus;
import com.fooddelivery.payment.model.Refund;
import com.fooddelivery.payment.model.RefundStatus;
import com.fooddelivery.payment.repository.PaymentRepository;
import com.fooddelivery.payment.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentEventProducer paymentEventProducer;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto request, Long authenticatedUserId) {
        log.info("Initiating payment processing for Order ID: {}, User: {}, Amount: {}",
                request.getOrderId(), authenticatedUserId, request.getAmount());

        // 1. Idempotency Check via Idempotency Key
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<Payment> existingByKey = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingByKey.isPresent()) {
                log.warn("Duplicate payment request detected for Idempotency Key: {}", request.getIdempotencyKey());
                return mapToResponseDto(existingByKey.get());
            }
        }

        // 2. Check if this order has already been successfully paid
        Optional<Payment> existingPaymentOpt = paymentRepository.findByOrderId(request.getOrderId());
        if (existingPaymentOpt.isPresent()) {
            Payment existing = existingPaymentOpt.get();
            if (existing.getPaymentStatus() == PaymentStatus.SUCCESS) {
                throw new DuplicatePaymentException(
                        "Order " + request.getOrderId() + " has already been paid successfully.");
            }
        }

        // 3. Prepare or reuse Payment record
        Long effectiveUserId = authenticatedUserId != null ? authenticatedUserId : request.getUserId();
        Payment payment = existingPaymentOpt.orElseGet(() -> Payment.builder()
                .orderId(request.getOrderId())
                .orderNumber(
                        request.getOrderNumber() != null ? request.getOrderNumber() : "ORD-" + request.getOrderId())
                .userId(effectiveUserId != null ? effectiveUserId : 0L)
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .gatewayProvider(paymentGateway.getProviderName())
                .idempotencyKey(request.getIdempotencyKey())
                .build());

        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);

        // 4. Delegate to the Payment Gateway
        GatewayTransactionResult gatewayResult = paymentGateway.processPayment(request);

        // 5. Update Payment status based on Gateway Response
        if (gatewayResult.isSuccessful()) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(gatewayResult.getTransactionId());
            payment.setFailureReason(null);
            log.info("Payment SUCCESS for Order ID: {}, Txn ID: {}", payment.getOrderId(),
                    gatewayResult.getTransactionId());
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setTransactionId(gatewayResult.getTransactionId());
            payment.setFailureReason(gatewayResult.getGatewayMessage());
            log.warn("Payment FAILED for Order ID: {}, Reason: {}", payment.getOrderId(),
                    gatewayResult.getGatewayMessage());
        }

        Payment savedPayment = paymentRepository.save(payment);

        // 6. Broadcast PaymentProcessedEvent to Kafka
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .paymentId(savedPayment.getId())
                .orderId(savedPayment.getOrderId())
                .orderNumber(savedPayment.getOrderNumber())
                .userId(savedPayment.getUserId())
                .amount(savedPayment.getAmount())
                .currency(savedPayment.getCurrency())
                .paymentMethod(savedPayment.getPaymentMethod())
                .paymentStatus(savedPayment.getPaymentStatus())
                .transactionId(savedPayment.getTransactionId())
                .failureReason(savedPayment.getFailureReason())
                .processedAt(LocalDateTime.now())
                .build();

        paymentEventProducer.publishPaymentProcessedEvent(event);

        return mapToResponseDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long paymentId, Long authenticatedUserId, boolean isAdmin) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));

        if (!isAdmin && authenticatedUserId != null && !payment.getUserId().equals(authenticatedUserId)) {
            throw new UnauthorizedPaymentAccessException("You are not authorized to view this payment transaction.");
        }

        return mapToResponseDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByOrderId(Long orderId, Long authenticatedUserId, boolean isAdmin) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for Order ID: " + orderId));

        if (!isAdmin && authenticatedUserId != null && !payment.getUserId().equals(authenticatedUserId)) {
            throw new UnauthorizedPaymentAccessException("You are not authorized to view this order payment.");
        }

        return mapToResponseDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RefundResponseDto processRefund(RefundRequestDto request, Long authenticatedUserId, boolean isAdmin) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(
                        () -> new PaymentNotFoundException("Payment not found with ID: " + request.getPaymentId()));

        if (!isAdmin && authenticatedUserId != null && !payment.getUserId().equals(authenticatedUserId)) {
            throw new UnauthorizedPaymentAccessException(
                    "You are not authorized to initiate a refund for this payment.");
        }

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new PaymentProcessingException("Only successfully completed payments can be refunded.");
        }

        if (request.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new PaymentProcessingException("Refund amount (" + request.getAmount()
                    + ") cannot exceed original payment amount (" + payment.getAmount() + ").");
        }

        // Delegate refund to Gateway
        GatewayTransactionResult refundResult = paymentGateway.processRefund(request, payment.getTransactionId());

        Refund refund = Refund.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(request.getAmount())
                .refundStatus(refundResult.isSuccessful() ? RefundStatus.SUCCESS : RefundStatus.FAILED)
                .reason(request.getReason())
                .refundTransactionId(refundResult.getTransactionId())
                .build();

        Refund savedRefund = refundRepository.save(refund);

        if (refundResult.isSuccessful()) {
            payment.setPaymentStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            log.info("Refund SUCCESS for Payment ID: {}, Refund Txn: {}", payment.getId(),
                    refundResult.getTransactionId());
        }

        return RefundResponseDto.builder()
                .refundId(savedRefund.getId())
                .paymentId(savedRefund.getPaymentId())
                .orderId(savedRefund.getOrderId())
                .amount(savedRefund.getAmount())
                .refundStatus(savedRefund.getRefundStatus())
                .reason(savedRefund.getReason())
                .refundTransactionId(savedRefund.getRefundTransactionId())
                .createdAt(savedRefund.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponseDto> getRefundsByPaymentId(Long paymentId) {
        return refundRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId)
                .stream()
                .map(r -> RefundResponseDto.builder()
                        .refundId(r.getId())
                        .paymentId(r.getPaymentId())
                        .orderId(r.getOrderId())
                        .amount(r.getAmount())
                        .refundStatus(r.getRefundStatus())
                        .reason(r.getReason())
                        .refundTransactionId(r.getRefundTransactionId())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Handling OrderCreatedEvent for order: {}", event.getOrderNumber());
        Optional<Payment> existing = paymentRepository.findByOrderId(event.getOrderId());
        if (existing.isEmpty()) {
            Payment payment = Payment.builder()
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .userId(event.getUserId())
                    .amount(event.getTotalAmount())
                    .currency("USD")
                    .paymentMethod(com.fooddelivery.payment.model.PaymentMethod.CREDIT_CARD) // default placeholder
                    .paymentStatus(PaymentStatus.PENDING)
                    .gatewayProvider(paymentGateway.getProviderName())
                    .build();
            paymentRepository.save(payment);
            log.info("Initialized PENDING payment record for Order ID: {}", event.getOrderId());
        }
    }

    @Override
    @Transactional
    public PaymentResponseDto handleWebhook(WebhookPayloadDto payload) {
        log.info("Handling webhook callback for transaction: {}, status: {}", payload.getTransactionId(),
                payload.getStatus());
        if (!paymentGateway.verifyWebhook(payload)) {
            throw new PaymentProcessingException("Invalid webhook signature or payload verification failed.");
        }

        Payment payment = paymentRepository.findByTransactionId(payload.getTransactionId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for transaction: " + payload.getTransactionId()));

        if ("payment.succeeded".equalsIgnoreCase(payload.getEventType())
                || "SUCCESS".equalsIgnoreCase(payload.getStatus())) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
        } else if ("payment.failed".equalsIgnoreCase(payload.getEventType())
                || "FAILED".equalsIgnoreCase(payload.getStatus())) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }

        Payment saved = paymentRepository.save(payment);
        return mapToResponseDto(saved);
    }

    private PaymentResponseDto mapToResponseDto(Payment payment) {
        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .orderNumber(payment.getOrderNumber())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionId(payment.getTransactionId())
                .gatewayProvider(payment.getGatewayProvider())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
