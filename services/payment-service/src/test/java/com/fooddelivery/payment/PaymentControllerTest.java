package com.fooddelivery.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.payment.dto.*;
import com.fooddelivery.payment.kafka.PaymentEventProducer;
import com.fooddelivery.payment.model.PaymentMethod;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private PaymentEventProducer paymentEventProducer;

        @Value("${app.jwt.secret}")
        private String jwtSecret;

        private String customerToken;
        private String adminToken;

        private String generateToken(Long userId, String username, List<String> roles) {
                byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
                SecretKey key = Keys.hmacShaKeyFor(keyBytes);

                return Jwts.builder()
                                .subject(username)
                                .claim("userId", userId)
                                .claim("email", username + "@example.com")
                                .claim("fullName", "Test User")
                                .claim("roles", roles)
                                .issuedAt(new Date())
                                .expiration(new Date(System.currentTimeMillis() + 3600000))
                                .signWith(key)
                                .compact();
        }

        @BeforeEach
        void setUp() {
                customerToken = generateToken(101L, "customer1", List.of("ROLE_CUSTOMER"));
                adminToken = generateToken(1L, "admin", List.of("ROLE_ADMIN"));
        }

        @Test
        void testSuccessfulPaymentFlow() throws Exception {
                PaymentRequestDto request = PaymentRequestDto.builder()
                                .orderId(501L)
                                .orderNumber("ORD-501")
                                .userId(101L)
                                .amount(new BigDecimal("45.50"))
                                .currency("USD")
                                .paymentMethod(PaymentMethod.CREDIT_CARD)
                                .cardNumber("4111222233330000")
                                .cardCvv("123")
                                .cardExpiry("12/28")
                                .build();

                MvcResult result = mockMvc.perform(post("/api/payments/process")
                                .header("Authorization", "Bearer " + customerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.orderId", is(501)))
                                .andExpect(jsonPath("$.amount", is(45.50)))
                                .andExpect(jsonPath("$.paymentStatus", is("SUCCESS")))
                                .andExpect(jsonPath("$.transactionId", notNullValue()))
                                .andReturn();

                Number paymentIdNum = objectMapper.readTree(result.getResponse().getContentAsString()).get("paymentId")
                                .numberValue();
                Long paymentId = paymentIdNum.longValue();

                // Query Payment by ID
                mockMvc.perform(get("/api/payments/" + paymentId)
                                .header("Authorization", "Bearer " + customerToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.paymentId", is(paymentId.intValue())))
                                .andExpect(jsonPath("$.paymentStatus", is("SUCCESS")));

                // Query Payment by Order ID
                mockMvc.perform(get("/api/payments/order/501")
                                .header("Authorization", "Bearer " + customerToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.orderId", is(501)))
                                .andExpect(jsonPath("$.paymentStatus", is("SUCCESS")));
        }

        @Test
        void testDeclinedCardPaymentFlow() throws Exception {
                PaymentRequestDto request = PaymentRequestDto.builder()
                                .orderId(502L)
                                .orderNumber("ORD-502")
                                .userId(101L)
                                .amount(new BigDecimal("100.00"))
                                .currency("USD")
                                .paymentMethod(PaymentMethod.CREDIT_CARD)
                                .cardNumber("4111222233330002") // Ends with 0002 -> triggers decline
                                .cardCvv("123")
                                .cardExpiry("12/28")
                                .build();

                mockMvc.perform(post("/api/payments/process")
                                .header("Authorization", "Bearer " + customerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.orderId", is(502)))
                                .andExpect(jsonPath("$.paymentStatus", is("FAILED")))
                                .andExpect(jsonPath("$.failureReason", containsString("declined")));
        }

        @Test
        void testRefundFlow() throws Exception {
                // 1. Process payment first
                PaymentRequestDto request = PaymentRequestDto.builder()
                                .orderId(503L)
                                .orderNumber("ORD-503")
                                .userId(101L)
                                .amount(new BigDecimal("75.00"))
                                .currency("USD")
                                .paymentMethod(PaymentMethod.CREDIT_CARD)
                                .cardNumber("4111222233330000")
                                .cardCvv("123")
                                .cardExpiry("12/28")
                                .build();

                MvcResult result = mockMvc.perform(post("/api/payments/process")
                                .header("Authorization", "Bearer " + customerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andReturn();

                Number paymentIdNum = objectMapper.readTree(result.getResponse().getContentAsString()).get("paymentId")
                                .numberValue();
                Long paymentId = paymentIdNum.longValue();

                // 2. Request refund
                RefundRequestDto refundRequest = RefundRequestDto.builder()
                                .paymentId(paymentId)
                                .amount(new BigDecimal("75.00"))
                                .reason("Customer requested order cancellation")
                                .build();

                mockMvc.perform(post("/api/payments/refund")
                                .header("Authorization", "Bearer " + customerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(refundRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.refundStatus", is("SUCCESS")))
                                .andExpect(jsonPath("$.amount", is(75.00)))
                                .andExpect(jsonPath("$.refundTransactionId", notNullValue()));

                // 3. Verify Payment Status updated to REFUNDED
                mockMvc.perform(get("/api/payments/" + paymentId)
                                .header("Authorization", "Bearer " + customerToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.paymentStatus", is("REFUNDED")));
        }
}
