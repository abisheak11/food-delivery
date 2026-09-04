package com.fooddelivery.payment;

import com.fooddelivery.payment.dto.PaymentRequestDto;
import com.fooddelivery.payment.dto.PaymentResponseDto;
import com.fooddelivery.payment.event.OrderCreatedEvent;
import com.fooddelivery.payment.exception.DuplicatePaymentException;
import com.fooddelivery.payment.gateway.MockPaymentGateway;
import com.fooddelivery.payment.kafka.PaymentEventProducer;
import com.fooddelivery.payment.model.Payment;
import com.fooddelivery.payment.model.PaymentMethod;
import com.fooddelivery.payment.model.PaymentStatus;
import com.fooddelivery.payment.repository.PaymentRepository;
import com.fooddelivery.payment.repository.RefundRepository;
import com.fooddelivery.payment.service.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    private MockPaymentGateway mockPaymentGateway;
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        mockPaymentGateway = new MockPaymentGateway();
        paymentService = new PaymentServiceImpl(paymentRepository, refundRepository, mockPaymentGateway, paymentEventProducer);
    }

    @Test
    void testProcessPaymentSuccess() {
        PaymentRequestDto request = PaymentRequestDto.builder()
                .orderId(101L)
                .orderNumber("ORD-101")
                .userId(1L)
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .cardNumber("4111222233330000")
                .build();

        Payment savedEntity = Payment.builder()
                .id(1L)
                .orderId(101L)
                .orderNumber("ORD-101")
                .userId(1L)
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.SUCCESS)
                .transactionId("txn_test_123")
                .build();

        when(paymentRepository.findByOrderId(101L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedEntity);

        PaymentResponseDto response = paymentService.processPayment(request, 1L);

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        verify(paymentEventProducer, times(1)).publishPaymentProcessedEvent(any());
    }

    @Test
    void testProcessPaymentDuplicateThrowsException() {
        PaymentRequestDto request = PaymentRequestDto.builder()
                .orderId(102L)
                .amount(new BigDecimal("20.00"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        Payment existingPaid = Payment.builder()
                .id(2L)
                .orderId(102L)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        when(paymentRepository.findByOrderId(102L)).thenReturn(Optional.of(existingPaid));

        assertThrows(DuplicatePaymentException.class, () -> paymentService.processPayment(request, 1L));
    }

    @Test
    void testHandleOrderCreatedEvent() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(200L)
                .orderNumber("ORD-200")
                .userId(5L)
                .totalAmount(new BigDecimal("35.00"))
                .build();

        when(paymentRepository.findByOrderId(200L)).thenReturn(Optional.empty());

        paymentService.handleOrderCreatedEvent(event);

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }
}
