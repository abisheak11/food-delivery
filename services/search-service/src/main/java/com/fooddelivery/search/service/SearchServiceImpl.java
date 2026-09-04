package com.fooddelivery.search.service;

import com.fooddelivery.search.dto.*;
import com.fooddelivery.search.model.MenuItemDocument;
import com.fooddelivery.search.model.RestaurantDocument;
import com.fooddelivery.search.repository.MenuItemSearchRepository;
import com.fooddelivery.search.repository.RestaurantSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final RestaurantSearchRepository restaurantRepo;
    private final MenuItemSearchRepository menuItemRepo;

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantSearchResponse> searchRestaurants(String query, String cuisine, Boolean isOpen, Double minRating) {
        String sanitizedQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String sanitizedCuisine = (cuisine != null && !cuisine.trim().isEmpty()) ? cuisine.trim() : null;

        List<RestaurantDocument> restaurants = restaurantRepo.searchRestaurants(sanitizedQuery, sanitizedCuisine, isOpen, minRating);
        return restaurants.stream()
                .map(this::mapToRestaurantResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemSearchResponse> searchMenuItems(String query, String category, BigDecimal minPrice, BigDecimal maxPrice, Boolean isAvailable, String cuisine) {
        String sanitizedQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String sanitizedCategory = (category != null && !category.trim().isEmpty()) ? category.trim() : null;
        String sanitizedCuisine = (cuisine != null && !cuisine.trim().isEmpty()) ? cuisine.trim() : null;

        List<MenuItemDocument> items = menuItemRepo.searchMenuItems(sanitizedQuery, sanitizedCategory, minPrice, maxPrice, isAvailable, sanitizedCuisine);
        return items.stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalSearchResponse globalSearch(String keyword) {
        String q = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : "";

        List<RestaurantSearchResponse> restaurants = searchRestaurants(q, null, null, null);
        List<MenuItemSearchResponse> foodItems = searchMenuItems(q, null, null, null, null, null);

        int total = restaurants.size() + foodItems.size();

        return GlobalSearchResponse.builder()
                .query(q)
                .totalMatches(total)
                .restaurants(restaurants)
                .foodItems(foodItems)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishAvailabilityResponse> findRestaurantsByDish(String dishName) {
        if (dishName == null || dishName.trim().isEmpty()) {
            return List.of();
        }
        List<MenuItemDocument> items = menuItemRepo.findAvailableRestaurantsByDish(dishName.trim());
        return items.stream()
                .map(item -> DishAvailabilityResponse.builder()
                        .menuItemId(item.getId())
                        .dishName(item.getName())
                        .price(item.getPrice())
                        .category(item.getCategory())
                        .restaurantId(item.getRestaurant().getId())
                        .restaurantName(item.getRestaurant().getName())
                        .restaurantAddress(item.getRestaurant().getAddress())
                        .restaurantRating(item.getRestaurant().getRating())
                        .isRestaurantOpen(item.getRestaurant().getIsOpen())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableCuisines() {
        return restaurantRepo.findDistinctCuisines();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableCategories() {
        return menuItemRepo.findDistinctCategories();
    }

    private RestaurantSearchResponse mapToRestaurantResponse(RestaurantDocument doc) {
        List<String> sampleDishes = doc.getMenuItems() != null
                ? doc.getMenuItems().stream().map(MenuItemDocument::getName).limit(4).collect(Collectors.toList())
                : List.of();

        return RestaurantSearchResponse.builder()
                .id(doc.getId())
                .name(doc.getName())
                .cuisineType(doc.getCuisineType())
                .address(doc.getAddress())
                .phone(doc.getPhone())
                .rating(doc.getRating())
                .isOpen(doc.getIsOpen())
                .totalMenuItems(doc.getMenuItems() != null ? doc.getMenuItems().size() : 0)
                .sampleDishes(sampleDishes)
                .build();
    }

    private MenuItemSearchResponse mapToMenuItemResponse(MenuItemDocument doc) {
        return MenuItemSearchResponse.builder()
                .id(doc.getId())
                .restaurantId(doc.getRestaurant() != null ? doc.getRestaurant().getId() : null)
                .restaurantName(doc.getRestaurant() != null ? doc.getRestaurant().getName() : null)
                .restaurantCuisine(doc.getRestaurant() != null ? doc.getRestaurant().getCuisineType() : null)
                .name(doc.getName())
                .description(doc.getDescription())
                .price(doc.getPrice())
                .category(doc.getCategory())
                .isAvailable(doc.getIsAvailable())
                .build();
    }
}
