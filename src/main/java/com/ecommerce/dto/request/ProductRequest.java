package com.ecommerce.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class ProductRequest {

    @Data
    public static class Create {
        @NotBlank(message = "Product name is required")
        private String name;

        private String description;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock cannot be negative")
        private Integer stockQuantity;

        private String imageUrl;

        @NotNull(message = "Category ID is required")
        private Long categoryId;
    }

    @Data
    public static class Update {
        private String name;
        private String description;

        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;

        @Min(value = 0, message = "Stock cannot be negative")
        private Integer stockQuantity;

        private String imageUrl;
        private Long categoryId;
        private Boolean active;
    }
}
