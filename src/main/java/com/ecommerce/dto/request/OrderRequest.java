package com.ecommerce.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class OrderRequest {

    @Data
    public static class Create {
        @NotBlank(message = "Shipping address is required")
        private String shippingAddress;
    }

    @Data
    public static class CartItemAdd {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }

    @Data
    public static class CategoryCreate {
        @NotBlank(message = "Category name is required")
        private String name;

        private String description;
    }
}
