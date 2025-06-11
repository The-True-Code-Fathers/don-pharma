package com.codefathers.service;

import com.codefathers.exceptions.BusinessRuleException;
import com.codefathers.model.dto.CreateOrderDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Order;
import com.codefathers.model.entity.OrderItem;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.model.enums.OrderStatus;
import com.codefathers.repository.interfaces.EmployeeRepository;
import com.codefathers.repository.interfaces.OrderRepository;
import com.codefathers.repository.interfaces.StorageRepository;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

        Employee employee = employeeRepository.searchEmployeePerId(createOrderDTO.getSellerId());
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
            BigDecimal itemPrice = dto.getProduct().getSellPrice().multiply(new BigDecimal(dto.getQuantity()));
            orderItem.setPrice(itemPrice);
            orderItem.setOrder(order);
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
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingPrice = productsPrice.multiply(new BigDecimal("0.05"));
        BigDecimal totalPrice = productsPrice.add(shippingPrice);

        order.setShippingPrice(shippingPrice);
        order.setProductsPrice(productsPrice);
        order.setTotalAmount(totalPrice);
        order.setItems(orderItems);

        orderRepository.save(order);
    }

    public Order findOrderById(UUID id) {
        return orderRepository.findById(id);
    }

    public void cancelOrder(UUID id) {
        Order order = orderRepository.findById(id);
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.update(order);
    }

}
