package com.codefathers.repository.interfaces;

import com.codefathers.model.dto.EmployeeSalesDataDTO;
import com.codefathers.model.dto.EmployeeSalesValueDataDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Order;
import com.codefathers.model.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    List<EmployeeSalesDataDTO> buscarVendasPorVendedor();
    List<EmployeeSalesDataDTO> buscarVendedoresComVendas();
    List<EmployeeSalesDataDTO> buscarVendasPorPeriodo(LocalDateTime inicio, LocalDateTime fim);
    List<EmployeeSalesValueDataDTO> buscarVendasComValorTotal();
    List<EmployeeSalesDataDTO> buscarVendasComCriteria();
    Employee buscarFuncionarioPorId(UUID employeeId);
}
