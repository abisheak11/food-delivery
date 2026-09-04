package com.fooddelivery.search.repository;

import com.fooddelivery.search.model.RestaurantDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantSearchRepository extends JpaRepository<RestaurantDocument, Long> {

    @Query("SELECT r FROM RestaurantDocument r WHERE " +
            "(:query IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            " OR LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :query, '%')) " +
            " OR LOWER(r.address) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:cuisine IS NULL OR LOWER(r.cuisineType) = LOWER(:cuisine)) AND " +
            "(:isOpen IS NULL OR r.isOpen = :isOpen) AND " +
            "(:minRating IS NULL OR r.rating >= :minRating)")
    List<RestaurantDocument> searchRestaurants(
            @Param("query") String query,
            @Param("cuisine") String cuisine,
            @Param("isOpen") Boolean isOpen,
            @Param("minRating") Double minRating
    );

    @Query("SELECT DISTINCT r.cuisineType FROM RestaurantDocument r WHERE r.cuisineType IS NOT NULL")
    List<String> findDistinctCuisines();
}
