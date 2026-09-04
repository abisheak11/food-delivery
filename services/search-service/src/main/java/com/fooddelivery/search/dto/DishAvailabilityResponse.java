package com.fooddelivery.search.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishAvailabilityResponse {
    private Long menuItemId;
    private String dishName;
    private BigDecimal price;
    private String category;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private Double restaurantRating;
    private Boolean isRestaurantOpen;
}
