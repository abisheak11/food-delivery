package com.fooddelivery.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String name;
    private String cuisineType;
    private String address;
    private String phone;
    private Long ownerId;
    private Boolean isOpen;
    private List<MenuItemResponse> menuItems;
    private LocalDateTime createdAt;
}
