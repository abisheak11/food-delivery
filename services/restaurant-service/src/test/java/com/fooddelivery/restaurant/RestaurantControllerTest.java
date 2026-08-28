package com.fooddelivery.restaurant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.restaurant.dto.CreateRestaurantRequest;
import com.fooddelivery.restaurant.dto.MenuItemRequest;
import com.fooddelivery.restaurant.dto.OrderDecisionRequest;
import com.fooddelivery.restaurant.kafka.OrderDecisionProducer;
import com.fooddelivery.restaurant.model.RestaurantOrder;
import com.fooddelivery.restaurant.model.RestaurantOrderStatus;
import com.fooddelivery.restaurant.repository.RestaurantOrderRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantOrderRepository restaurantOrderRepository;

    @MockBean
    private OrderDecisionProducer orderDecisionProducer;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private String restaurantOwnerToken;

    private String generateToken(Long userId, String username, List<String> roles) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("email", username + "@example.com")
                .claim("fullName", "Restaurant Owner")
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    @BeforeEach
    void setUp() {
        restaurantOwnerToken = generateToken(200L, "pizza_palace_owner", List.of("ROLE_RESTAURANT"));
    }

    @Test
    void testRestaurantAndOrderDecisionFlow() throws Exception {
        // 1. Create restaurant
        CreateRestaurantRequest restaurantReq = CreateRestaurantRequest.builder()
                .name("Pizza Palace")
                .cuisineType("Italian")
                .address("789 Broadway Ave")
                .phone("555-1234")
                .build();

        MvcResult createRestResult = mockMvc.perform(post("/api/restaurants")
                        .header("Authorization", "Bearer " + restaurantOwnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restaurantReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Pizza Palace")))
                .andExpect(jsonPath("$.ownerId", is(200)))
                .andReturn();

        Number restIdNum = objectMapper.readTree(createRestResult.getResponse().getContentAsString()).get("id").numberValue();
        Long restaurantId = restIdNum.longValue();

        // 2. Add Menu Item
        MenuItemRequest menuReq = MenuItemRequest.builder()
                .name("Pepperoni Feast")
                .description("Crispy crust with mozzarella and pepperoni")
                .price(new BigDecimal("14.99"))
                .category("Pizza")
                .isAvailable(true)
                .build();

        mockMvc.perform(post("/api/restaurants/" + restaurantId + "/menu")
                        .header("Authorization", "Bearer " + restaurantOwnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(menuReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Pepperoni Feast")));

        // 3. Simulate an incoming Kafka order arriving at the restaurant
        RestaurantOrder incomingOrder = restaurantOrderRepository.save(RestaurantOrder.builder()
                .orderId(999L)
                .orderNumber("ORD-TEST-001")
                .restaurantId(restaurantId)
                .customerId(101L)
                .totalAmount(new BigDecimal("29.98"))
                .status(RestaurantOrderStatus.PENDING)
                .deliveryAddress("456 Elm St")
                .build());

        // 4. Check incoming orders
        mockMvc.perform(get("/api/restaurants/" + restaurantId + "/orders")
                        .header("Authorization", "Bearer " + restaurantOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].orderNumber", is("ORD-TEST-001")));

        // 5. Accept the order
        OrderDecisionRequest acceptReq = OrderDecisionRequest.builder()
                .decision("ACCEPTED")
                .reason("Ingredients ready, starting preparation")
                .build();

        mockMvc.perform(post("/api/restaurants/orders/" + incomingOrder.getOrderId() + "/decision")
                        .header("Authorization", "Bearer " + restaurantOwnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACCEPTED")));
    }
}
