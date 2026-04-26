package com.ecommerce.service.impl;

import com.ecommerce.dto.request.OrderRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<ApiResponse.CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(ApiResponse.CategoryResponse::from)
                .toList();
    }

    public ApiResponse.CategoryResponse getCategoryById(Long id) {
        return ApiResponse.CategoryResponse.from(findById(id));
    }

    @Transactional
    public ApiResponse.CategoryResponse createCategory(OrderRequest.CategoryCreate request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ApiException("Category already exists: " + request.getName(), HttpStatus.CONFLICT);
        }
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return ApiResponse.CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public ApiResponse.CategoryResponse updateCategory(Long id, OrderRequest.CategoryCreate request) {
        Category category = findById(id);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return ApiResponse.CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = findById(id);
        if (!category.getProducts().isEmpty()) {
            throw new ApiException("Cannot delete category with existing products", HttpStatus.BAD_REQUEST);
        }
        categoryRepository.delete(category);
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException("Category not found with id: " + id, HttpStatus.NOT_FOUND));
    }
}
