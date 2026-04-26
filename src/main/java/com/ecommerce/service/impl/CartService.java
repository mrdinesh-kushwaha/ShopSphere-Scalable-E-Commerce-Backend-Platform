package com.ecommerce.service.impl;

import com.ecommerce.dto.request.OrderRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ApiResponse.CartResponse getCart(String email) {
        Cart cart = getOrCreateCart(email);
        return ApiResponse.CartResponse.from(cart);
    }

    @Transactional
    public ApiResponse.CartResponse addItem(String email, OrderRequest.CartItemAdd request) {
        Cart cart = getOrCreateCart(email);

        Product product = productRepository.findById(request.getProductId())
                .filter(Product::isActive)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new ApiException(
                "Insufficient stock. Available: " + product.getStockQuantity(), HttpStatus.BAD_REQUEST
            );
        }

        // Check if product already in cart — update quantity
        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                    item -> item.setQuantity(item.getQuantity() + request.getQuantity()),
                    () -> cart.getItems().add(
                        CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(request.getQuantity())
                            .build()
                    )
                );

        return ApiResponse.CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public ApiResponse.CartResponse updateItem(String email, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(email);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ApiException("Cart item not found", HttpStatus.NOT_FOUND));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            if (item.getProduct().getStockQuantity() < quantity) {
                throw new ApiException(
                    "Insufficient stock. Available: " + item.getProduct().getStockQuantity(),
                    HttpStatus.BAD_REQUEST
                );
            }
            item.setQuantity(quantity);
        }

        return ApiResponse.CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public ApiResponse.CartResponse removeItem(String email, Long itemId) {
        Cart cart = getOrCreateCart(email);
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        return ApiResponse.CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(String email) {
        Cart cart = getOrCreateCart(email);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }
}
