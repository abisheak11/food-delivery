package com.fooddelivery.search.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemSearchResponse {
    private Long id;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantCuisine;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Boolean isAvailable;
}
