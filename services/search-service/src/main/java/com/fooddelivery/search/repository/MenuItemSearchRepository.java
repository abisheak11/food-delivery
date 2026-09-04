package com.fooddelivery.search.repository;

import com.fooddelivery.search.model.MenuItemDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MenuItemSearchRepository extends JpaRepository<MenuItemDocument, Long> {

    @Query("SELECT m FROM MenuItemDocument m JOIN FETCH m.restaurant r WHERE " +
            "(:query IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            " OR LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            " OR LOWER(m.category) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:category IS NULL OR LOWER(m.category) = LOWER(:category)) AND " +
            "(:minPrice IS NULL OR m.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR m.price <= :maxPrice) AND " +
            "(:isAvailable IS NULL OR m.isAvailable = :isAvailable) AND " +
            "(:cuisine IS NULL OR LOWER(r.cuisineType) = LOWER(:cuisine))")
    List<MenuItemDocument> searchMenuItems(
            @Param("query") String query,
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("isAvailable") Boolean isAvailable,
            @Param("cuisine") String cuisine
    );

    @Query("SELECT DISTINCT m.category FROM MenuItemDocument m WHERE m.category IS NOT NULL")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT m FROM MenuItemDocument m JOIN FETCH m.restaurant r " +
            "WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :dishName, '%')) AND m.isAvailable = true AND r.isOpen = true")
    List<MenuItemDocument> findAvailableRestaurantsByDish(@Param("dishName") String dishName);
}
