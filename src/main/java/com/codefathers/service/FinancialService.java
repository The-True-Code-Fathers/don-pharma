package com.codefathers.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.OrderItem;
import com.codefathers.model.entity.Payment;
import com.codefathers.model.entity.PurchaseOrderItem;
import com.codefathers.model.entity.ShippingOrder;
import com.codefathers.model.enums.OrderStatus;
import com.codefathers.repository.interfaces.OrderItemRepository;
import com.codefathers.repository.interfaces.OrderRepository;
import com.codefathers.repository.interfaces.PaymentRepository;
import com.codefathers.repository.interfaces.PurchaseOrderItemRepository;
import com.codefathers.repository.interfaces.ShippingOrderRepository;

public class FinancialService {

    private final PaymentRepository paymentRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShippingOrderRepository shippingOrderRepository;
    private final OrderRepository orderRepository;

    private BigDecimal paymentsTotal;
    private BigDecimal purchaseTotal = BigDecimal.ZERO;
    private BigDecimal shippingTotal;
    private BigDecimal orderTotal = BigDecimal.ZERO;

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

        for (PurchaseOrderItem purchaseOrderItem : purchaseOrderItemRepository.findAll()) {
            BigDecimal itemTotal = purchaseOrderItem.getPrice()
                    .multiply(BigDecimal.valueOf(purchaseOrderItem.getQuantity()));
            purchaseTotal = purchaseTotal.add(itemTotal);
        }

        shippingTotal = shippingOrderRepository.listAll().stream()
                .map(ShippingOrder::getShippingCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void calculateInflows() {
        for (OrderItem orderItem : orderItemRepository.listAll()) {
            BigDecimal itemTotal = orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            orderTotal = orderTotal.add(itemTotal);
        }
    }

    public Map<Employee, Double> getTopPerformingSellers(LocalDate from, LocalDate to, int limit) {
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
