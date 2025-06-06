package com.codefathers.repository;

import com.codefathers.model.entity.Order;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends GenericRepository<Order, UUID> {
    void save(Order order);
    void update(Order order);
    void delete(Order order);
    Order findById(UUID id);
    List<Order> findAll();
}
