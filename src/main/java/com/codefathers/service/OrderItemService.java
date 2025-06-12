package com.codefathers.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.dto.CreateOrderItemDTO;
import com.codefathers.model.entity.OrderItem;
import com.codefathers.repository.interfaces.OrderItemRepository;
import com.codefathers.repository.interfaces.OrderRepository;

public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;

    public OrderItemService(OrderItemRepository orderItemRepository, OrderRepository orderRepository) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
    }

    public void createOrderItem(CreateOrderItemDTO createOrderItemDTO) {

        OrderItem item = OrderItem.builder()
                .product(createOrderItemDTO.getProduct())
                .quantity(createOrderItemDTO.getQuantity())
                .price(createOrderItemDTO.getPrice())
                .build();

        orderItemRepository.save(item);
    }

    public List<OrderItem> listAll() {
        return orderItemRepository.listAll();
    }

    public Optional<OrderItem> findById(UUID id) {
        var orderItem = orderItemRepository.findById(id);
        return orderItem;
    }

    public void update(OrderItem orderItem) {
        orderItemRepository.update(orderItem);
    }

}
