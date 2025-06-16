package com.codefathers.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.exceptions.BusinessRuleException;
import com.codefathers.model.dto.CreateOrderDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Order;
import com.codefathers.model.entity.OrderItem;
import com.codefathers.model.entity.Storage;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.model.enums.OrderStatus;
import com.codefathers.repository.interfaces.EmployeeRepository;
import com.codefathers.repository.interfaces.OrderRepository;
import com.codefathers.repository.interfaces.StorageRepository;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;
    private final StorageRepository storageRepository;
    private final Validator validator;

    public OrderService(OrderRepository orderRepository,
            EmployeeRepository employeeRepository, StorageRepository storageRepository,
            Validator validator) {
        this.orderRepository = orderRepository;
        this.employeeRepository = employeeRepository;
        this.storageRepository = storageRepository;
        this.validator = validator;
    }

    public void createOrder(@Valid CreateOrderDTO createOrderDTO) throws ConstraintViolationException {
        var violations = validator.validate(createOrderDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        Employee employee = employeeRepository.findById(createOrderDTO.getSellerId());
        boolean isSeller = employee.getRole().equals(EmployeeRole.SALES);
        boolean isManager = employee.getRole().equals(EmployeeRole.LOCAL_MANAGER);

        if (!(isSeller || isManager)) {
            throw new BusinessRuleException("Provided employee is not allowed to create order");
        }

        Order order = Order.builder()
                .seller(employee)
                .description(createOrderDTO.getDescription())
                .shippingProvider(createOrderDTO.getShippingProvider())
                .shippingPrice(BigDecimal.ZERO)
                .productsPrice(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .orderStatus(OrderStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        List<OrderItem> orderItems = createOrderDTO.getItem().stream().map(dto -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(dto.getProduct());
            orderItem.setQuantity(dto.getQuantity());
            BigDecimal itemPrice = dto.getPrice();
            orderItem.setPrice(itemPrice);
            orderItem.setOrder(order);
            orderItem.setCreatedAt(LocalDateTime.now());
            return orderItem;
        }).toList();

        for (OrderItem item : orderItems) {
            var storage = storageRepository.findByProductSku(item.getProduct().getSku()).get();
            if (storage.getProductQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("The quantity needs to be lower than stock");
            }
            storage.setProductQuantity(storage.getProductQuantity() - item.getQuantity());
            storageRepository.update(storage);
        }

        BigDecimal productsPrice = orderItems.stream()
                .map(orderItem -> orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingPrice = productsPrice.multiply(new BigDecimal("0.05"));
        BigDecimal totalPrice = productsPrice.add(shippingPrice);

        order.setShippingPrice(shippingPrice);
        order.setProductsPrice(productsPrice);
        order.setTotalAmount(totalPrice);
        order.setItems(orderItems);

        orderRepository.save(order);
    }

    public Optional<Order> findOrderById(UUID id) {
        return orderRepository.findById(id);
    }

    public void cancelOrder(UUID id) {
        Order order = orderRepository.findById(id).get();
        List<OrderItem> orderItems = order.getItems().stream().map(dto -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setQuantity(dto.getQuantity());
            orderItem.setPrice(dto.getPrice());
            orderItem.setProduct(dto.getProduct());
            return orderItem;
        }).toList();

        if (order.getOrderStatus() == OrderStatus.OPEN) {
            for (OrderItem item : orderItems) {
                String productSku = item.getProduct().getSku();

                storageRepository.findByProductSku(productSku).ifPresentOrElse(existingStorage -> {
                    existingStorage.setProductQuantity(existingStorage.getProductQuantity() + item.getQuantity());
                    storageRepository.update(existingStorage);
                }, () -> {
                    Storage newStorage = Storage.builder()
                            .product(item.getProduct())
                            .productQuantity(item.getQuantity())
                            .build();
                    storageRepository.save(newStorage);
                });
            }
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.update(order);
    }

    public void finishOrder(Order order) {
        order.setOrderStatus(OrderStatus.INVOICED);
        orderRepository.update(order);
    }

    public List<Order> findAll() {
        return orderRepository.listAll();
    }

    public List<Order> findOrdersByPeriod(LocalDate start, LocalDate end) {
        return orderRepository.findOrdersByTimePeriod(start, end);
    }

    public Long count() {
        return orderRepository.count();
    }

    public BigDecimal getTotalRevenue() {
        return BigDecimal.ZERO;
    }

    public void update(Order order) {
        orderRepository.update(order);
    }

    // Adicione este método ao seu OrderService

    public void updateOrder(UUID orderId, OrderStatus newStatus, Employee newSeller, String newDescription) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        // Verificar se o pedido pode ser editado
        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.INVOICED) {
            throw new BusinessRuleException("Cannot update order with status: " + order.getOrderStatus());
        }

        // Verificar se o novo vendedor tem permissão
        if (newSeller != null) {
            boolean isSeller = newSeller.getRole().equals(EmployeeRole.SALES);
            boolean isManager = newSeller.getRole().equals(EmployeeRole.LOCAL_MANAGER);

            if (!(isSeller || isManager)) {
                throw new BusinessRuleException("Provided employee is not allowed to be assigned to order");
            }
            order.setSeller(newSeller);
        }

        // Atualizar status se fornecido
        if (newStatus != null) {
            order.setOrderStatus(newStatus);
        }

        // Atualizar descrição
        if (newDescription != null) {
            order.setDescription(newDescription);
        }

        orderRepository.update(order);
    }

    public List<Order> findOrdersByStatusAndTimePeriod(OrderStatus status, LocalDate start, LocalDate end) {
        return orderRepository.findOrdersByStatusAndTimePeriod(status, start, end);
    }
}
