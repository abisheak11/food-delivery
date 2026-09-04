package com.fooddelivery.search.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "search_restaurants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String cuisineType;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 20)
    private String phone;

    private Double rating;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isOpen = true;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<MenuItemDocument> menuItems = new ArrayList<>();

    private LocalDateTime createdAt;

    public void addMenuItem(MenuItemDocument item) {
        menuItems.add(item);
        item.setRestaurant(this);
    }
}
