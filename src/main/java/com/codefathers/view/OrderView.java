package com.codefathers.view;

import com.codefathers.model.dto.CreateOrderDTO;
import com.codefathers.model.entity.Order;
import com.codefathers.repository.EmployeeRepositoryImpl;
import com.codefathers.repository.OrderRepositoryImpl;
import com.codefathers.service.OrderService;
import com.codefathers.util.ValidationUtil;

public class OrderView {

    private OrderService orderService;

    public OrderView(OrderService orderService) {
        this.orderService = orderService;
    }

    public void render() {
        var jorge = CreateOrderDTO.builder().build();
        orderService.createOrder(jorge);
    }

    public static void main(String[] args) {
        new OrderView(
                new OrderService(new OrderRepositoryImpl(),
                                 new EmployeeRepositoryImpl(),
                                ValidationUtil.getValidator())
        ).render();
    }
}
