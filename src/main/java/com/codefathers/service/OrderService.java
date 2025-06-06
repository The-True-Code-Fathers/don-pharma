package com.codefathers.service;

import com.codefathers.model.dto.CreateOrderDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Order;
import com.codefathers.repository.EmployeeRepository;
import com.codefathers.repository.OrderRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

import java.util.UUID;

public class OrderService {

    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;
    private final Validator validator;

    public OrderService(OrderRepository orderRepository,
                        EmployeeRepository employeeRepository,
                        Validator validator) {
        this.orderRepository = orderRepository;
        this.employeeRepository = employeeRepository;
        this.validator = validator;
    }

    public void createOrder(@Valid CreateOrderDTO createOrderDTO) {

        var violations = validator.validate(createOrderDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        Employee emp = employeeRepository.findById(createOrderDTO.getSellerId());
        Order order = Order.builder()
                .seller(emp)
                .build();
        orderRepository.save(order);
    }

    public Order findOrderById(UUID id) {
        return new Order();
    }

}
