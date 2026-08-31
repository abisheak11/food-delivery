package com.fooddelivery.delivery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DeliveryServiceApplicationTests {

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.fooddelivery.delivery.kafka.DeliveryEventProducer deliveryEventProducer;

    @Test
    void contextLoads() {
    }
}
