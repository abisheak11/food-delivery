package com.fooddelivery.search.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSearchResponse {
    private String query;
    private int totalMatches;
    private List<RestaurantSearchResponse> restaurants;
    private List<MenuItemSearchResponse> foodItems;
}
