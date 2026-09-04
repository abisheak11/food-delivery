package com.fooddelivery.search;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/search/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Search Service is running")));
    }

    @Test
    void testSearchRestaurantsByCuisine() throws Exception {
        mockMvc.perform(get("/api/search/restaurants")
                        .param("cuisine", "Italian")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", containsString("Mario")))
                .andExpect(jsonPath("$[0].cuisineType", is("Italian")));
    }

    @Test
    void testSearchRestaurantsByQuery() throws Exception {
        mockMvc.perform(get("/api/search/restaurants")
                        .param("query", "Fiesta")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", containsString("Taco Fiesta")));
    }

    @Test
    void testSearchMenuItemsByPriceRange() throws Exception {
        mockMvc.perform(get("/api/search/items")
                        .param("minPrice", "5.00")
                        .param("maxPrice", "10.00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.price < 5.00)]", empty()))
                .andExpect(jsonPath("$[?(@.price > 10.00)]", empty()));
    }

    @Test
    void testSearchMenuItemsByCategory() throws Exception {
        mockMvc.perform(get("/api/search/items")
                        .param("category", "Burgers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", containsString("Cheeseburger")));
    }

    @Test
    void testGlobalSearch() throws Exception {
        mockMvc.perform(get("/api/search/all")
                        .param("keyword", "Pizza")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query", is("Pizza")))
                .andExpect(jsonPath("$.totalMatches", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.foodItems", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.foodItems[0].name", containsString("Pizza")));
    }

    @Test
    void testFindRestaurantsByDish() throws Exception {
        mockMvc.perform(get("/api/search/restaurants-by-dish")
                        .param("dishName", "Butter Chicken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].dishName", containsString("Butter Chicken")))
                .andExpect(jsonPath("$[0].restaurantName", containsString("Royal Spice")));
    }

    @Test
    void testGetCuisinesAndCategories() throws Exception {
        mockMvc.perform(get("/api/search/cuisines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem("Italian")))
                .andExpect(jsonPath("$", hasItem("Mexican")))
                .andExpect(jsonPath("$", hasItem("Indian")));

        mockMvc.perform(get("/api/search/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem("Pizza")))
                .andExpect(jsonPath("$", hasItem("Burgers")));
    }
}
