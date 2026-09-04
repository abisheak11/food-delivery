package com.fooddelivery.search.controller;

import com.fooddelivery.search.dto.*;
import com.fooddelivery.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Endpoints for searching restaurants, cuisines, and food items")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "Unified global search across restaurants and food items")
    @GetMapping("/all")
    public ResponseEntity<GlobalSearchResponse> globalSearch(
            @Parameter(description = "Keyword to search across restaurants and dishes")
            @RequestParam(name = "keyword", required = false) String keyword) {
        return ResponseEntity.ok(searchService.globalSearch(keyword));
    }

    @Operation(summary = "Search restaurants by query, cuisine, open status, and rating")
    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantSearchResponse>> searchRestaurants(
            @Parameter(description = "Search term in restaurant name, cuisine, or address")
            @RequestParam(name = "query", required = false) String query,
            @Parameter(description = "Filter by specific cuisine type (e.g. Italian, Mexican, Indian)")
            @RequestParam(name = "cuisine", required = false) String cuisine,
            @Parameter(description = "Filter by open/closed status")
            @RequestParam(name = "isOpen", required = false) Boolean isOpen,
            @Parameter(description = "Filter by minimum customer rating (1.0 to 5.0)")
            @RequestParam(name = "minRating", required = false) Double minRating) {
        return ResponseEntity.ok(searchService.searchRestaurants(query, cuisine, isOpen, minRating));
    }

    @Operation(summary = "Search food items and dishes with price range and category filters")
    @GetMapping("/items")
    public ResponseEntity<List<MenuItemSearchResponse>> searchMenuItems(
            @Parameter(description = "Search term in dish name, description, or category")
            @RequestParam(name = "query", required = false) String query,
            @Parameter(description = "Filter by food category (e.g. Pizza, Burgers, Pasta, Dessert)")
            @RequestParam(name = "category", required = false) String category,
            @Parameter(description = "Minimum price")
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price")
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @Parameter(description = "Filter by item availability")
            @RequestParam(name = "isAvailable", required = false) Boolean isAvailable,
            @Parameter(description = "Filter dishes by restaurant cuisine")
            @RequestParam(name = "cuisine", required = false) String cuisine) {
        return ResponseEntity.ok(searchService.searchMenuItems(query, category, minPrice, maxPrice, isAvailable, cuisine));
    }

    @Operation(summary = "Find all open restaurants serving a specific dish")
    @GetMapping("/restaurants-by-dish")
    public ResponseEntity<List<DishAvailabilityResponse>> findRestaurantsByDish(
            @Parameter(description = "Dish or food name to locate restaurants for")
            @RequestParam(name = "dishName") String dishName) {
        return ResponseEntity.ok(searchService.findRestaurantsByDish(dishName));
    }

    @Operation(summary = "Get list of all distinct available cuisines in catalog")
    @GetMapping("/cuisines")
    public ResponseEntity<List<String>> getCuisines() {
        return ResponseEntity.ok(searchService.getAvailableCuisines());
    }

    @Operation(summary = "Get list of all distinct available food categories")
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(searchService.getAvailableCategories());
    }

    @Operation(summary = "Health check endpoint")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Search Service is running on port 8086");
    }
}
