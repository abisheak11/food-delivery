package com.fooddelivery.search.service;

import com.fooddelivery.search.dto.*;

import java.math.BigDecimal;
import java.util.List;

public interface SearchService {

    List<RestaurantSearchResponse> searchRestaurants(String query, String cuisine, Boolean isOpen, Double minRating);

    List<MenuItemSearchResponse> searchMenuItems(String query, String category, BigDecimal minPrice, BigDecimal maxPrice, Boolean isAvailable, String cuisine);

    GlobalSearchResponse globalSearch(String keyword);

    List<DishAvailabilityResponse> findRestaurantsByDish(String dishName);

    List<String> getAvailableCuisines();

    List<String> getAvailableCategories();
}
