package com.fooddelivery.order.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestaurantOrderDecisionEvent implements Serializable {

    private Long orderId;
    private String orderNumber;
    private Long restaurantId;
    private String decision; // "ACCEPTED" or "REJECTED"
    private String reason;
    private LocalDateTime decisionTime;
}
