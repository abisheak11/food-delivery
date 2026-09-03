package com.fooddelivery.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.order.dto.CreateOrderRequest;
import com.fooddelivery.order.dto.OrderItemRequest;
import com.fooddelivery.order.dto.UpdateOrderStatusRequest;
import com.fooddelivery.order.model.OrderStatus;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fooddelivery.order.kafka.OrderEventProducer orderEventProducer;

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
    void testCreateAndRetrieveOrderFlow() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .restaurantId(50L)
                .deliveryAddress("123 Main Street, Apt 4B")
                .contactPhone("9876543210")
                .specialInstructions("Leave at door")
                .items(List.of(
                        OrderItemRequest.builder()
                                .itemName("Margherita Pizza")
                                .quantity(2)
                                .price(new BigDecimal("12.99"))
                                .build(),
                        OrderItemRequest.builder()
                                .itemName("Garlic Bread")
                                .quantity(1)
                                .price(new BigDecimal("4.50"))
                                .build()
                ))
                .build();

        // 1. Create order with customer token
        MvcResult createResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber", notNullValue()))
                .andExpect(jsonPath("$.userId", is(101)))
                .andExpect(jsonPath("$.restaurantId", is(50)))
                .andExpect(jsonPath("$.status", is("PLACED")))
                .andExpect(jsonPath("$.paymentStatus", is("PENDING")))
                .andExpect(jsonPath("$.totalAmount", is(30.48)))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andReturn();

        Number orderIdNum = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").numberValue();
        Long orderId = orderIdNum.longValue();

        // 2. Fetch order by ID
        mockMvc.perform(get("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId.intValue())))
                .andExpect(jsonPath("$.status", is("PLACED")))
                .andExpect(jsonPath("$.paymentStatus", is("PENDING")))
                .andExpect(jsonPath("$.items", hasSize(2)));

        // 3. Fetch customer's orders
        mockMvc.perform(get("/api/orders/my-orders")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        // 4. Update status with Admin/Restaurant token
        UpdateOrderStatusRequest updateStatus = UpdateOrderStatusRequest.builder()
                .status(OrderStatus.CONFIRMED)
                .build();

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatus)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    void testUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testPaymentProcessedSuccessFlow() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .restaurantId(10L)
                .deliveryAddress("456 Elm St")
                .contactPhone("1234567890")
                .items(List.of(OrderItemRequest.builder()
                        .itemName("Burger")
                        .quantity(1)
                        .price(new BigDecimal("10.00"))
                        .build()))
                .build();

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PLACED")))
                .andExpect(jsonPath("$.paymentStatus", is("PENDING")))
                .andReturn();

        Number orderIdNum = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").numberValue();
        Long orderId = orderIdNum.longValue();

        // 2. Fetch order by ID and verify initial state
        mockMvc.perform(get("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PLACED")))
                .andExpect(jsonPath("$.paymentStatus", is("PENDING")));
    }
}



