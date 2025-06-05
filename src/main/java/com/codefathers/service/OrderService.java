package com.codefathers.service;

import com.codefathers.model.dto.CreateOrderDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Order;
import com.codefathers.repository.EmployeeRepository;
import com.codefathers.repository.OrderRepository;

public class OrderService {

    private OrderRepository orderRepository;
    private EmployeeRepository employeeRepository;

    public OrderService(OrderRepository orderRepository,
                        EmployeeRepository employeeRepository) {
        this.orderRepository = orderRepository;
        this.employeeRepository = employeeRepository;
    }

    public void createOrder(CreateOrderDTO createOrderDTO) {
        Employee emp = employeeRepository.findById(createOrderDTO.getSellerId());

        Order order = Order.builder()
                .seller(emp)
                .build();

        orderRepository.save(order);
    }

}
