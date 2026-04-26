package com.ecommerce.config;

import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return; // Skip if already seeded

        log.info("Seeding sample data...");

        // --- Users ---
        User admin = userRepository.save(User.builder()
                .firstName("Admin").lastName("User")
                .email("admin@ecommerce.com")
                .password(passwordEncoder.encode("Admin@1234"))
                .role(User.Role.ROLE_ADMIN).build());

        User customer = userRepository.save(User.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com")
                .password(passwordEncoder.encode("User@1234"))
                .role(User.Role.ROLE_USER).build());

        cartRepository.save(Cart.builder().user(customer).build());

        // --- Categories ---
        Category electronics = categoryRepository.save(
                Category.builder().name("Electronics").description("Gadgets and devices").build());
        Category clothing = categoryRepository.save(
                Category.builder().name("Clothing").description("Fashion and apparel").build());
        Category books = categoryRepository.save(
                Category.builder().name("Books").description("Books and literature").build());

        // --- Products ---
        productRepository.saveAll(List.of(
            Product.builder().name("iPhone 15 Pro").description("Apple flagship smartphone")
                .price(new BigDecimal("99999")).stockQuantity(50)
                .category(electronics).build(),
            Product.builder().name("Samsung Galaxy S24").description("Android flagship smartphone")
                .price(new BigDecimal("79999")).stockQuantity(40)
                .category(electronics).build(),
            Product.builder().name("MacBook Air M2").description("Thin and powerful laptop")
                .price(new BigDecimal("114900")).stockQuantity(20)
                .category(electronics).build(),
            Product.builder().name("Levi's 501 Jeans").description("Classic straight fit jeans")
                .price(new BigDecimal("3999")).stockQuantity(100)
                .category(clothing).build(),
            Product.builder().name("Nike Dri-FIT T-Shirt").description("Sports performance t-shirt")
                .price(new BigDecimal("1499")).stockQuantity(200)
                .category(clothing).build(),
            Product.builder().name("Clean Code").description("A handbook of agile software craftsmanship")
                .price(new BigDecimal("799")).stockQuantity(60)
                .category(books).build(),
            Product.builder().name("Spring Boot in Action").description("Practical guide to Spring Boot")
                .price(new BigDecimal("999")).stockQuantity(45)
                .category(books).build()
        ));

        log.info("Sample data seeded successfully!");
        log.info("Admin login  → email: admin@ecommerce.com  | password: Admin@1234");
        log.info("User login   → email: john@example.com     | password: User@1234");
    }
}
