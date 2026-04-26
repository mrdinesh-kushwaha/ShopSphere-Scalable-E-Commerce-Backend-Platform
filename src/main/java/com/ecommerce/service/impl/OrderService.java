package com.ecommerce.service.impl;

import com.ecommerce.dto.request.OrderRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;

    @Transactional
    public ApiResponse.OrderResponse placeOrder(String email, OrderRequest.Create request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("Cart is empty", HttpStatus.BAD_REQUEST));

        if (cart.getItems().isEmpty()) {
            throw new ApiException("Cannot place order with empty cart", HttpStatus.BAD_REQUEST);
        }

        // Validate stock and build order items
        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            Product product = cartItem.getProduct();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new ApiException(
                    "Insufficient stock for: " + product.getName() +
                    ". Available: " + product.getStockQuantity(),
                    HttpStatus.BAD_REQUEST
                );
            }

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());

            return OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
        }).toList();

        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .totalAmount(totalAmount)
                .status(Order.OrderStatus.PENDING)
                .build();

        orderItems.forEach(item -> {
            item.setOrder(order);
            order.getItems().add(item);
        });

        Order saved = orderRepository.save(order);

        // Clear cart after successful order
        cartService.clearCart(email);

        log.info("Order placed: {} for user: {}", saved.getId(), email);
        return ApiResponse.OrderResponse.from(saved);
    }

    public ApiResponse.PageResponse<ApiResponse.OrderResponse> getUserOrders(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Page<ApiResponse.OrderResponse> page = orderRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(ApiResponse.OrderResponse::from);

        return ApiResponse.PageResponse.<ApiResponse.OrderResponse>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    public ApiResponse.OrderResponse getOrderById(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));

        // Ensure the order belongs to this user (unless admin)
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        return ApiResponse.OrderResponse.from(order);
    }

    @Transactional
    public ApiResponse.OrderResponse cancelOrder(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new ApiException(
                "Cannot cancel order with status: " + order.getStatus(),
                HttpStatus.BAD_REQUEST
            );
        }

        // Restore stock
        order.getItems().forEach(item ->
            item.getProduct().setStockQuantity(
                item.getProduct().getStockQuantity() + item.getQuantity()
            )
        );

        order.setStatus(Order.OrderStatus.CANCELLED);
        return ApiResponse.OrderResponse.from(orderRepository.save(order));
    }

    // Admin: update order status
    @Transactional
    public ApiResponse.OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));

        try {
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid order status: " + status, HttpStatus.BAD_REQUEST);
        }

        return ApiResponse.OrderResponse.from(orderRepository.save(order));
    }
}
