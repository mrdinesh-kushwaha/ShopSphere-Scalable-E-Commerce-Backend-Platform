package com.ecommerce.service.impl;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ApiResponse.PageResponse<ApiResponse.ProductResponse> getAllProducts(Pageable pageable) {
        Page<ApiResponse.ProductResponse> page = productRepository
                .findByActiveTrue(pageable)
                .map(ApiResponse.ProductResponse::from);
        return toPageResponse(page);
    }

    public ApiResponse.ProductResponse getProductById(Long id) {
        Product product = findProductById(id);
        return ApiResponse.ProductResponse.from(product);
    }

    public ApiResponse.PageResponse<ApiResponse.ProductResponse> searchProducts(
            String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<ApiResponse.ProductResponse> page = productRepository
                .searchProducts(keyword, categoryId, minPrice, maxPrice, pageable)
                .map(ApiResponse.ProductResponse::from);
        return toPageResponse(page);
    }

    @Transactional
    public ApiResponse.ProductResponse createProduct(ProductRequest.Create request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException("Category not found", HttpStatus.NOT_FOUND));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .category(category)
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created: {}", saved.getName());
        return ApiResponse.ProductResponse.from(saved);
    }

    @Transactional
    public ApiResponse.ProductResponse updateProduct(Long id, ProductRequest.Update request) {
        Product product = findProductById(id);

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStockQuantity() != null) product.setStockQuantity(request.getStockQuantity());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) product.setActive(request.getActive());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ApiException("Category not found", HttpStatus.NOT_FOUND));
            product.setCategory(category);
        }

        return ApiResponse.ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProductById(id);
        product.setActive(false); // Soft delete
        productRepository.save(product);
        log.info("Product soft-deleted: {}", id);
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ApiException("Product not found with id: " + id, HttpStatus.NOT_FOUND));
    }

    private <T> ApiResponse.PageResponse<T> toPageResponse(Page<T> page) {
        return ApiResponse.PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
