package com.codefathers.repository.interfaces;

import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Order;
import com.codefathers.model.entity.OrderItem;
import com.codefathers.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    void save(Order order);
    void update(Order order);
    void delete(Order order);
    Optional<Order> findById(UUID id);
    List<Order> listAll();
    Long count();
    BigDecimal getTotalRevenue();
    List<OrderItem> findAllOrderItemsByOrderId(UUID orderId);
    List<Order> findOrdersByTimePeriod(LocalDate start, LocalDate end);
    List<Order> findOrdersByStatus(OrderStatus status);
    List<Order> findOrdersByStatusAndTimePeriod(OrderStatus status, LocalDate start, LocalDate end);
    List<Employee> findSellersRankedByOrderStatus(LocalDate from, LocalDate to, OrderStatus status, int limit);
    List<Order> findInvoicedOrders(LocalDate start, LocalDate end);
    long countInvoicedOrders(LocalDate start, LocalDate end);

}
