package com.fooddelivery.order.service;

import com.fooddelivery.order.dto.*;
import com.fooddelivery.order.event.OrderCreatedEvent;
import com.fooddelivery.order.event.OrderItemEventDto;
import com.fooddelivery.order.event.PaymentProcessedEvent;
import com.fooddelivery.order.exception.InvalidOrderStateException;
import com.fooddelivery.order.exception.ResourceNotFoundException;
import com.fooddelivery.order.exception.UnauthorizedOrderAccessException;
import com.fooddelivery.order.kafka.OrderEventProducer;
import com.fooddelivery.order.model.Order;
import com.fooddelivery.order.model.OrderItem;
import com.fooddelivery.order.model.OrderStatus;
import com.fooddelivery.order.model.PaymentStatus;
import com.fooddelivery.order.repository.OrderRepository;
import com.fooddelivery.order.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, UserPrincipal currentUser) {
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(currentUser.getId())
                .restaurantId(request.getRestaurantId())
                .deliveryAddress(request.getDeliveryAddress())
                .contactPhone(request.getContactPhone())
                .specialInstructions(request.getSpecialInstructions())
                .status(OrderStatus.PLACED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        for (OrderItemRequest itemReq : request.getItems()) {
            BigDecimal subTotal = itemReq.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subTotal);

            OrderItem orderItem = OrderItem.builder()
                    .itemName(itemReq.getItemName())
                    .quantity(itemReq.getQuantity())
                    .price(itemReq.getPrice())
                    .subTotal(subTotal)
                    .build();

            order.addItem(orderItem);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Publish OrderCreatedEvent to Kafka topic (consumed by payment-service to initialize payment record)
        List<OrderItemEventDto> eventItems = savedOrder.getItems().stream()
                .map(item -> OrderItemEventDto.builder()
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subTotal(item.getSubTotal())
                        .build())
                .collect(Collectors.toList());

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .userId(savedOrder.getUserId())
                .restaurantId(savedOrder.getRestaurantId())
                .totalAmount(savedOrder.getTotalAmount())
                .deliveryAddress(savedOrder.getDeliveryAddress())
                .contactPhone(savedOrder.getContactPhone())
                .specialInstructions(savedOrder.getSpecialInstructions())
                .items(eventItems)
                .createdAt(savedOrder.getCreatedAt())
                .build();

        orderEventProducer.sendOrderCreatedEvent(event);

        return mapToOrderResponse(savedOrder);
    }

    @Transactional
    public void handlePaymentProcessedEvent(PaymentProcessedEvent event) {
        log.info("Processing PaymentProcessedEvent in OrderService: orderId={}, orderNumber={}, status={}",
                event.getOrderId(), event.getOrderNumber(), event.getPaymentStatus());

        Order order = null;
        if (event.getOrderId() != null) {
            order = orderRepository.findById(event.getOrderId()).orElse(null);
        }
        if (order == null && event.getOrderNumber() != null) {
            order = orderRepository.findByOrderNumber(event.getOrderNumber()).orElse(null);
        }

        if (order == null) {
            log.warn("Order not found for PaymentProcessedEvent: orderId={}, orderNumber={}",
                    event.getOrderId(), event.getOrderNumber());
            return;
        }

        if ("SUCCESS".equalsIgnoreCase(event.getPaymentStatus())) {
            order.setPaymentStatus(PaymentStatus.SUCCESS);
            Order updatedOrder = orderRepository.save(order);
            log.info("Order ID {} paymentStatus updated to SUCCESS. Broadcasting OrderPaidEvent to restaurant and delivery.", updatedOrder.getId());

            // Build and broadcast OrderPaidEvent to restaurant & delivery services
            List<OrderItemEventDto> eventItems = updatedOrder.getItems().stream()
                    .map(item -> OrderItemEventDto.builder()
                            .itemName(item.getItemName())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .subTotal(item.getSubTotal())
                            .build())
                    .collect(Collectors.toList());

            OrderCreatedEvent paidEvent = OrderCreatedEvent.builder()
                    .orderId(updatedOrder.getId())
                    .orderNumber(updatedOrder.getOrderNumber())
                    .userId(updatedOrder.getUserId())
                    .restaurantId(updatedOrder.getRestaurantId())
                    .totalAmount(updatedOrder.getTotalAmount())
                    .deliveryAddress(updatedOrder.getDeliveryAddress())
                    .contactPhone(updatedOrder.getContactPhone())
                    .specialInstructions(updatedOrder.getSpecialInstructions())
                    .items(eventItems)
                    .createdAt(updatedOrder.getCreatedAt())
                    .build();

            orderEventProducer.sendOrderPaidEvent(paidEvent);
        } else if ("FAILED".equalsIgnoreCase(event.getPaymentStatus())) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.warn("Order ID {} paymentStatus updated to FAILED and status to CANCELLED. Reason: {}", order.getId(), event.getFailureReason());
        }
    }



    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, UserPrincipal currentUser) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        boolean isAdmin = hasRole(currentUser, "ROLE_ADMIN");
        boolean isOwner = order.getUserId().equals(currentUser.getId());
        boolean isRestaurant = hasRole(currentUser, "ROLE_RESTAURANT");
        boolean isDelivery = hasRole(currentUser, "ROLE_DELIVERY");

        if (!isAdmin && !isOwner && !isRestaurant && !isDelivery) {
            throw new UnauthorizedOrderAccessException("You do not have permission to view this order");
        }

        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber, UserPrincipal currentUser) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));

        boolean isAdmin = hasRole(currentUser, "ROLE_ADMIN");
        boolean isOwner = order.getUserId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedOrderAccessException("You do not have permission to view this order");
        }

        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(UserPrincipal currentUser) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByRestaurant(Long restaurantId, UserPrincipal currentUser) {
        List<Order> orders = orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(UserPrincipal currentUser) {
        return orderRepository.findAll().stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request, UserPrincipal currentUser) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // State validation checks
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Cannot update status of an order that is already " + order.getStatus());
        }

        order.setStatus(request.getStatus());
        Order updatedOrder = orderRepository.save(order);

        return mapToOrderResponse(updatedOrder);
    }

    private boolean hasRole(UserPrincipal user, String role) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subTotal(item.getSubTotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .restaurantId(order.getRestaurantId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .deliveryAddress(order.getDeliveryAddress())
                .contactPhone(order.getContactPhone())
                .specialInstructions(order.getSpecialInstructions())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}

