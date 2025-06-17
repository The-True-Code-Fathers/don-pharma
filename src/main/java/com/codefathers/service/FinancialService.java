package com.codefathers.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.codefathers.model.entity.*;
import com.codefathers.model.enums.OrderStatus;
import com.codefathers.repository.interfaces.*;

public class FinancialService {

    private final PaymentRepository paymentRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShippingOrderRepository shippingOrderRepository;
    private final OrderRepository orderRepository;

    private BigDecimal paymentsTotal;
    private BigDecimal purchaseTotal;
    private BigDecimal shippingTotal;
    private BigDecimal orderTotal;

    public FinancialService(PaymentRepository paymentRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            OrderItemRepository orderItemRepository,
            ShippingOrderRepository shippingOrderRepository,
            OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.shippingOrderRepository = shippingOrderRepository;
        this.orderRepository = orderRepository;

        calculateOutflows();
        calculateInflows();
    }

    private void calculateOutflows() {
        paymentsTotal = paymentRepository.listAll().stream()
                .map(Payment::getGrossIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        purchaseTotal = purchaseOrderItemRepository.findAll().stream()
                .map(PurchaseOrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        shippingTotal = shippingOrderRepository.listAll().stream()
                .map(ShippingOrder::getShippingCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void calculateInflows() {
        orderTotal = orderItemRepository.listAll().stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Employee> getTopPerformingSellers(LocalDate from, LocalDate to, int limit) {
        return orderRepository.findSellersRankedByOrderStatus(from, to, OrderStatus.INVOICED, limit);
    }

    public BigDecimal getPaymentsTotal() {
        return paymentsTotal;
    }

    public BigDecimal getPurchaseTotal() {
        return purchaseTotal;
    }

    public BigDecimal getShippingTotal() {
        return shippingTotal;
    }

    public BigDecimal getTotalOutflows() {
        return paymentsTotal.add(purchaseTotal).add(shippingTotal);
    }

    public BigDecimal getTotalInflows() {
        return orderTotal;
    }

    public BigDecimal getNetCashFlow() {
        return orderTotal.subtract(getTotalOutflows());
    }
}
