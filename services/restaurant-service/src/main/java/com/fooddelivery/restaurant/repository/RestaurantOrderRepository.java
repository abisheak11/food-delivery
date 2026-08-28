package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.model.RestaurantOrder;
import com.fooddelivery.restaurant.model.RestaurantOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, Long> {
    Optional<RestaurantOrder> findByOrderId(Long orderId);
    Optional<RestaurantOrder> findByOrderNumber(String orderNumber);
    List<RestaurantOrder> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
    List<RestaurantOrder> findByRestaurantIdAndStatusOrderByCreatedAtDesc(Long restaurantId, RestaurantOrderStatus status);
}
