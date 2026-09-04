package com.fooddelivery.search.config;

import com.fooddelivery.search.model.MenuItemDocument;
import com.fooddelivery.search.model.RestaurantDocument;
import com.fooddelivery.search.repository.RestaurantSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RestaurantSearchRepository restaurantRepository;

    @Override
    public void run(String... args) {
        if (restaurantRepository.count() > 0) {
            return;
        }

        log.info("Seeding sample restaurants and menu items into search index...");

        // 1. Mario's Italian Trattoria
        RestaurantDocument r1 = RestaurantDocument.builder()
                .name("Mario's Italian Trattoria")
                .cuisineType("Italian")
                .address("123 Little Italy Ave, New York, NY")
                .phone("555-0101")
                .rating(4.8)
                .isOpen(true)
                .createdAt(LocalDateTime.now())
                .build();

        r1.addMenuItem(MenuItemDocument.builder()
                .name("Margherita Pizza")
                .description("San Marzano tomato sauce, fresh mozzarella, basil, and extra virgin olive oil")
                .price(new BigDecimal("14.99"))
                .category("Pizza")
                .isAvailable(true)
                .build());

        r1.addMenuItem(MenuItemDocument.builder()
                .name("Truffle Mushroom Pasta")
                .description("Handmade fettuccine with wild forest mushrooms and black truffle cream")
                .price(new BigDecimal("18.50"))
                .category("Pasta")
                .isAvailable(true)
                .build());

        r1.addMenuItem(MenuItemDocument.builder()
                .name("Tiramisu Classico")
                .description("Espresso-soaked ladyfingers layered with rich mascarpone cheese and cocoa")
                .price(new BigDecimal("8.00"))
                .category("Dessert")
                .isAvailable(true)
                .build());

        restaurantRepository.save(r1);

        // 2. Taco Fiesta Mexican Grill
        RestaurantDocument r2 = RestaurantDocument.builder()
                .name("Taco Fiesta Mexican Grill")
                .cuisineType("Mexican")
                .address("456 Mission Blvd, San Francisco, CA")
                .phone("555-0202")
                .rating(4.6)
                .isOpen(true)
                .createdAt(LocalDateTime.now())
                .build();

        r2.addMenuItem(MenuItemDocument.builder()
                .name("Birria Beef Tacos")
                .description("Slow-cooked braised beef in corn tortillas served with rich dipping consommé")
                .price(new BigDecimal("12.50"))
                .category("Tacos")
                .isAvailable(true)
                .build());

        r2.addMenuItem(MenuItemDocument.builder()
                .name("Loaded Guacamole & Chips")
                .description("Freshly mashed Hass avocados with lime, cilantro, jalapeños, and crispy tortilla chips")
                .price(new BigDecimal("7.99"))
                .category("Appetizer")
                .isAvailable(true)
                .build());

        r2.addMenuItem(MenuItemDocument.builder()
                .name("Carne Asada Burrito")
                .description("Grilled skirt steak, seasoned black beans, Mexican rice, cheese, and salsa verde")
                .price(new BigDecimal("13.99"))
                .category("Burritos")
                .isAvailable(true)
                .build());

        restaurantRepository.save(r2);

        // 3. Royal Spice Indian Kitchen
        RestaurantDocument r3 = RestaurantDocument.builder()
                .name("Royal Spice Indian Kitchen")
                .cuisineType("Indian")
                .address("789 Curry Lane, Chicago, IL")
                .phone("555-0303")
                .rating(4.9)
                .isOpen(true)
                .createdAt(LocalDateTime.now())
                .build();

        r3.addMenuItem(MenuItemDocument.builder()
                .name("Butter Chicken")
                .description("Tender tandoori chicken cooked in a rich, creamy tomato butter sauce")
                .price(new BigDecimal("16.99"))
                .category("Curry")
                .isAvailable(true)
                .build());

        r3.addMenuItem(MenuItemDocument.builder()
                .name("Garlic Butter Naan")
                .description("Oven-baked flatbread infused with roasted garlic and fresh cilantro")
                .price(new BigDecimal("4.50"))
                .category("Bread")
                .isAvailable(true)
                .build());

        r3.addMenuItem(MenuItemDocument.builder()
                .name("Chicken Biryani")
                .description("Fragrant basmati rice layered with spiced chicken, saffron, and fried onions")
                .price(new BigDecimal("15.50"))
                .category("Rice")
                .isAvailable(true)
                .build());

        restaurantRepository.save(r3);

        // 4. Burger Kingdom & Shake Co.
        RestaurantDocument r4 = RestaurantDocument.builder()
                .name("Burger Kingdom & Shake Co.")
                .cuisineType("American")
                .address("321 Broadway, Austin, TX")
                .phone("555-0404")
                .rating(4.5)
                .isOpen(true)
                .createdAt(LocalDateTime.now())
                .build();

        r4.addMenuItem(MenuItemDocument.builder()
                .name("Double Smoked Bacon Cheeseburger")
                .description("Angus beef patties, smoked cheddar, crispy applewood bacon, and signature secret sauce")
                .price(new BigDecimal("13.50"))
                .category("Burgers")
                .isAvailable(true)
                .build());

        r4.addMenuItem(MenuItemDocument.builder()
                .name("Crispy Truffle Fries")
                .description("Hand-cut golden fries tossed in truffle oil and parmesan cheese")
                .price(new BigDecimal("6.50"))
                .category("Sides")
                .isAvailable(true)
                .build());

        r4.addMenuItem(MenuItemDocument.builder()
                .name("Salted Caramel Milkshake")
                .description("Thick handcrafted shake with vanilla bean gelato and sea salt caramel swirl")
                .price(new BigDecimal("6.00"))
                .category("Beverages")
                .isAvailable(true)
                .build());

        restaurantRepository.save(r4);

        // 5. Sakura Ramen & Sushi House
        RestaurantDocument r5 = RestaurantDocument.builder()
                .name("Sakura Ramen & Sushi House")
                .cuisineType("Japanese")
                .address("555 Sakura Way, Seattle, WA")
                .phone("555-0505")
                .rating(4.7)
                .isOpen(true)
                .createdAt(LocalDateTime.now())
                .build();

        r5.addMenuItem(MenuItemDocument.builder()
                .name("Tonkotsu Pork Ramen")
                .description("24-hour pork bone broth, tender chashu pork, soft-boiled egg, and nori")
                .price(new BigDecimal("15.99"))
                .category("Ramen")
                .isAvailable(true)
                .build());

        r5.addMenuItem(MenuItemDocument.builder()
                .name("Spicy Salmon Roll")
                .description("Fresh Atlantic salmon, cucumber, spicy mayo, and toasted sesame seeds")
                .price(new BigDecimal("11.50"))
                .category("Sushi")
                .isAvailable(true)
                .build());

        restaurantRepository.save(r5);

        log.info("Search catalog seed data initialized successfully: 5 restaurants, 14 menu items.");
    }
}
