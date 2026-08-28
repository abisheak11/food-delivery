package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.*;
import com.fooddelivery.restaurant.event.RestaurantOrderDecisionEvent;
import com.fooddelivery.restaurant.exception.InvalidDecisionException;
import com.fooddelivery.restaurant.exception.ResourceNotFoundException;
import com.fooddelivery.restaurant.exception.UnauthorizedRestaurantAccessException;
import com.fooddelivery.restaurant.kafka.OrderDecisionProducer;
import com.fooddelivery.restaurant.model.MenuItem;
import com.fooddelivery.restaurant.model.Restaurant;
import com.fooddelivery.restaurant.model.RestaurantOrder;
import com.fooddelivery.restaurant.model.RestaurantOrderStatus;
import com.fooddelivery.restaurant.repository.MenuItemRepository;
import com.fooddelivery.restaurant.repository.RestaurantOrderRepository;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import com.fooddelivery.restaurant.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantOrderRepository restaurantOrderRepository;
    private final OrderDecisionProducer orderDecisionProducer;

    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantRequest request, UserPrincipal currentUser) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .cuisineType(request.getCuisineType())
                .address(request.getAddress())
                .phone(request.getPhone())
                .ownerId(currentUser.getId())
                .isOpen(true)
                .build();

        Restaurant saved = restaurantRepository.save(restaurant);
        return mapToRestaurantResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::mapToRestaurantResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
        return mapToRestaurantResponse(restaurant);
    }

    @Transactional
    public MenuItemResponse addMenuItem(Long restaurantId, MenuItemRequest request, UserPrincipal currentUser) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        validateOwnership(restaurant, currentUser);

        MenuItem menuItem = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .build();

        restaurant.addMenuItem(menuItem);
        MenuItem saved = menuItemRepository.save(menuItem);

        return mapToMenuItemResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItems(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RestaurantOrderResponse> getOrdersForRestaurant(Long restaurantId, UserPrincipal currentUser) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        validateOwnership(restaurant, currentUser);

        return restaurantOrderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(this::mapToRestaurantOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantOrderResponse processOrderDecision(Long orderId, OrderDecisionRequest request, UserPrincipal currentUser) {
        RestaurantOrder order = restaurantOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant order not found for orderId: " + orderId));

        Restaurant restaurant = restaurantRepository.findById(order.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + order.getRestaurantId()));

        validateOwnership(restaurant, currentUser);

        if (order.getStatus() != RestaurantOrderStatus.PENDING) {
            throw new InvalidDecisionException("Order decision already processed. Current status: " + order.getStatus());
        }

        String decisionUpper = request.getDecision().toUpperCase();
        if ("ACCEPTED".equals(decisionUpper)) {
            order.setStatus(RestaurantOrderStatus.ACCEPTED);
        } else if ("REJECTED".equals(decisionUpper)) {
            order.setStatus(RestaurantOrderStatus.REJECTED);
            order.setRejectionReason(request.getReason());
        } else {
            throw new InvalidDecisionException("Invalid decision: " + request.getDecision() + ". Expected ACCEPTED or REJECTED.");
        }

        RestaurantOrder updatedOrder = restaurantOrderRepository.save(order);

        // Publish Kafka event to notify order-service
        RestaurantOrderDecisionEvent decisionEvent = RestaurantOrderDecisionEvent.builder()
                .orderId(updatedOrder.getOrderId())
                .orderNumber(updatedOrder.getOrderNumber())
                .restaurantId(updatedOrder.getRestaurantId())
                .decision(decisionUpper)
                .reason(request.getReason())
                .decisionTime(LocalDateTime.now())
                .build();

        orderDecisionProducer.sendOrderDecision(decisionEvent);

        return mapToRestaurantOrderResponse(updatedOrder);
    }

    private void validateOwnership(Restaurant restaurant, UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!restaurant.getOwnerId().equals(currentUser.getId()) && !isAdmin) {
            throw new UnauthorizedRestaurantAccessException("You do not have permission to manage this restaurant");
        }
    }

    private RestaurantResponse mapToRestaurantResponse(Restaurant restaurant) {
        List<MenuItemResponse> menuItems = restaurant.getMenuItems() != null
                ? restaurant.getMenuItems().stream().map(this::mapToMenuItemResponse).collect(Collectors.toList())
                : List.of();

        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .cuisineType(restaurant.getCuisineType())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .ownerId(restaurant.getOwnerId())
                .isOpen(restaurant.getIsOpen())
                .menuItems(menuItems)
                .createdAt(restaurant.getCreatedAt())
                .build();
    }

    private MenuItemResponse mapToMenuItemResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .category(item.getCategory())
                .isAvailable(item.getIsAvailable())
                .build();
    }

    private RestaurantOrderResponse mapToRestaurantOrderResponse(RestaurantOrder order) {
        return RestaurantOrderResponse.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .restaurantId(order.getRestaurantId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .deliveryAddress(order.getDeliveryAddress())
                .contactPhone(order.getContactPhone())
                .specialInstructions(order.getSpecialInstructions())
                .itemsSummary(order.getItemsSummary())
                .rejectionReason(order.getRejectionReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
