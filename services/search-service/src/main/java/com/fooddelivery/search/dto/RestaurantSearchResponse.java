package com.fooddelivery.search.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSearchResponse {
    private Long id;
    private String name;
    private String cuisineType;
    private String address;
    private String phone;
    private Double rating;
    private Boolean isOpen;
    private Integer totalMenuItems;
    private List<String> sampleDishes;
}
