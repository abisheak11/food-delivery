package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.*;
import com.fooddelivery.restaurant.security.UserPrincipal;
import com.fooddelivery.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_RESTAURANT', 'ROLE_ADMIN')")
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        RestaurantResponse response = restaurantService.createRestaurant(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        List<RestaurantResponse> response = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        RestaurantResponse response = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/menu")
    @PreAuthorize("hasAnyRole('ROLE_RESTAURANT', 'ROLE_ADMIN')")
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        MenuItemResponse response = restaurantService.addMenuItem(id, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItemResponse>> getMenuItems(@PathVariable Long id) {
        List<MenuItemResponse> response = restaurantService.getMenuItems(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/orders")
    @PreAuthorize("hasAnyRole('ROLE_RESTAURANT', 'ROLE_ADMIN')")
    public ResponseEntity<List<RestaurantOrderResponse>> getOrdersForRestaurant(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<RestaurantOrderResponse> response = restaurantService.getOrdersForRestaurant(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/orders/{orderId}/decision")
    @PreAuthorize("hasAnyRole('ROLE_RESTAURANT', 'ROLE_ADMIN')")
    public ResponseEntity<RestaurantOrderResponse> processOrderDecision(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderDecisionRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        RestaurantOrderResponse response = restaurantService.processOrderDecision(orderId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Restaurant Service is running");
    }
}
