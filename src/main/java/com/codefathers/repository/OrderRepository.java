package com.codefathers.repository;

import com.codefathers.model.entity.Order;

import java.util.UUID;

public interface OrderRepository {
    void save(Order order);
    Order findById(UUID id);
}
