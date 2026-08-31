package com.fooddelivery.delivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.delivery.dto.*;
import com.fooddelivery.delivery.model.DeliveryStatus;
import com.fooddelivery.delivery.model.PartnerStatus;
import com.fooddelivery.delivery.model.VehicleType;
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
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fooddelivery.delivery.kafka.DeliveryEventProducer deliveryEventProducer;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private String deliveryPartnerToken;
    private String adminToken;
    private String customerToken;

    private String generateToken(Long userId, String username, List<String> roles) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("email", username + "@example.com")
                .claim("fullName", "Test Delivery Guy")
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    @BeforeEach
    void setUp() {
        deliveryPartnerToken = generateToken(201L, "driver_dave", List.of("ROLE_DELIVERY"));
        adminToken = generateToken(1L, "admin", List.of("ROLE_ADMIN"));
        customerToken = generateToken(101L, "customer1", List.of("ROLE_CUSTOMER"));
    }

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/deliveries/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("delivery-service")));
    }

    @Test
    void testDeliveryPartnerLifecycleAndTaskFlow() throws Exception {
        // 1. Register / Update Delivery Partner Profile
        DeliveryPartnerProfileRequest profileRequest = DeliveryPartnerProfileRequest.builder()
                .fullName("Dave Driver")
                .phone("9876543210")
                .vehicleType(VehicleType.BIKE)
                .vehicleNumber("KA-01-AB-1234")
                .licenseNumber("DL-987654321")
                .build();

        mockMvc.perform(post("/api/deliveries/partners/profile")
                        .header("Authorization", "Bearer " + deliveryPartnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName", is("Dave Driver")))
                .andExpect(jsonPath("$.status", is("AVAILABLE")));

        // 2. Update Location
        UpdateLocationRequest locRequest = UpdateLocationRequest.builder()
                .latitude(12.9716)
                .longitude(77.5946)
                .build();

        mockMvc.perform(put("/api/deliveries/partners/location")
                        .header("Authorization", "Bearer " + deliveryPartnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(locRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLatitude", is(12.9716)))
                .andExpect(jsonPath("$.currentLongitude", is(77.5946)));

        // 3. Create a Delivery Task (as Admin)
        CreateDeliveryTaskRequest taskRequest = CreateDeliveryTaskRequest.builder()
                .orderId(9999L)
                .orderNumber("ORD-9999-TEST")
                .customerId(101L)
                .customerPhone("9988776655")
                .restaurantId(10L)
                .restaurantName("Pizza Hub")
                .pickupAddress("10 Brigade Road")
                .deliveryAddress("45 MG Road")
                .deliveryFee(new BigDecimal("5.00"))
                .notes("Ring doorbell")
                .build();

        MvcResult taskResult = mockMvc.perform(post("/api/deliveries/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber", is("ORD-9999-TEST")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andReturn();

        Number taskIdNum = objectMapper.readTree(taskResult.getResponse().getContentAsString()).get("id").numberValue();
        Long taskId = taskIdNum.longValue();

        // 4. Partner views available deliveries
        mockMvc.perform(get("/api/deliveries/available")
                        .header("Authorization", "Bearer " + deliveryPartnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        // 5. Partner accepts delivery task
        mockMvc.perform(post("/api/deliveries/" + taskId + "/accept")
                        .header("Authorization", "Bearer " + deliveryPartnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACCEPTED")))
                .andExpect(jsonPath("$.partnerName", is("Dave Driver")));

        // 6. Update Status to PICKED_UP
        UpdateDeliveryStatusRequest pickupStatus = UpdateDeliveryStatusRequest.builder()
                .status(DeliveryStatus.PICKED_UP)
                .notes("Picked up from restaurant")
                .build();

        mockMvc.perform(put("/api/deliveries/" + taskId + "/status")
                        .header("Authorization", "Bearer " + deliveryPartnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pickupStatus)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PICKED_UP")))
                .andExpect(jsonPath("$.pickedUpAt", notNullValue()));

        // 7. Update Status to DELIVERED
        UpdateDeliveryStatusRequest deliveredStatus = UpdateDeliveryStatusRequest.builder()
                .status(DeliveryStatus.DELIVERED)
                .notes("Delivered to customer")
                .build();

        mockMvc.perform(put("/api/deliveries/" + taskId + "/status")
                        .header("Authorization", "Bearer " + deliveryPartnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deliveredStatus)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DELIVERED")))
                .andExpect(jsonPath("$.deliveredAt", notNullValue()));

        // 8. Track Delivery for Order
        mockMvc.perform(get("/api/deliveries/order/9999")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(9999)))
                .andExpect(jsonPath("$.deliveryStatus", is("DELIVERED")))
                .andExpect(jsonPath("$.partnerName", is("Dave Driver")));
    }
}
