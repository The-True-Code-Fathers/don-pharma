package com.codefathers.repository.interfaces;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.entity.OrderItem;

public interface OrderItemRepository {
    void save(OrderItem orderItem);
    void update(OrderItem orderItem);
    void delete(OrderItem orderItem);
    Optional<OrderItem> findById(UUID id);
    Optional<List<OrderItem>> findByProductSku(String productSku);
    List<OrderItem> listAll();
    List<OrderItem> listByTimePeriod(LocalDate from, LocalDate to); 
}
