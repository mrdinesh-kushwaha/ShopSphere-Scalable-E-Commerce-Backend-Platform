package com.ecommerce.dto.response;

import com.ecommerce.entity.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ApiResponse {

    @Data @Builder
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private UserResponse user;
    }

    @Data @Builder
    public static class UserResponse {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String role;
        private LocalDateTime createdAt;

        public static UserResponse from(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .role(user.getRole().name())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }

    @Data @Builder
    public static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private String imageUrl;
        private boolean active;
        private String categoryName;
        private Long categoryId;
        private LocalDateTime createdAt;

        public static ProductResponse from(Product product) {
            return ProductResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .stockQuantity(product.getStockQuantity())
                    .imageUrl(product.getImageUrl())
                    .active(product.isActive())
                    .categoryName(product.getCategory().getName())
                    .categoryId(product.getCategory().getId())
                    .createdAt(product.getCreatedAt())
                    .build();
        }
    }

    @Data @Builder
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String description;
        private int productCount;

        public static CategoryResponse from(Category category) {
            return CategoryResponse.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .description(category.getDescription())
                    .productCount(category.getProducts().size())
                    .build();
        }
    }

    @Data @Builder
    public static class OrderResponse {
        private Long id;
        private BigDecimal totalAmount;
        private String status;
        private String shippingAddress;
        private List<OrderItemResponse> items;
        private LocalDateTime createdAt;

        public static OrderResponse from(Order order) {
            return OrderResponse.builder()
                    .id(order.getId())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus().name())
                    .shippingAddress(order.getShippingAddress())
                    .items(order.getItems().stream().map(OrderItemResponse::from).toList())
                    .createdAt(order.getCreatedAt())
                    .build();
        }
    }

    @Data @Builder
    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        public static OrderItemResponse from(OrderItem item) {
            return OrderItemResponse.builder()
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subtotal(item.getSubtotal())
                    .build();
        }
    }

    @Data @Builder
    public static class CartResponse {
        private Long id;
        private List<CartItemResponse> items;
        private BigDecimal totalPrice;

        public static CartResponse from(Cart cart) {
            return CartResponse.builder()
                    .id(cart.getId())
                    .items(cart.getItems().stream().map(CartItemResponse::from).toList())
                    .totalPrice(cart.getTotalPrice())
                    .build();
        }
    }

    @Data @Builder
    public static class CartItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal subtotal;

        public static CartItemResponse from(CartItem item) {
            return CartItemResponse.builder()
                    .id(item.getId())
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .unitPrice(item.getProduct().getPrice())
                    .quantity(item.getQuantity())
                    .subtotal(item.getSubtotal())
                    .build();
        }
    }

    @Data @Builder
    public static class MessageResponse {
        private String message;
        private boolean success;
    }

    @Data @Builder
    public static class PageResponse<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean last;
    }
}
