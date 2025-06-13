package com.codefathers.service;

import java.math.BigDecimal;

import com.codefathers.model.entity.OrderItem;
import com.codefathers.model.entity.Payment;
import com.codefathers.model.entity.PurchaseOrderItem;
import com.codefathers.model.entity.ShippingOrder;
import com.codefathers.repository.interfaces.OrderItemRepository;
import com.codefathers.repository.interfaces.PaymentRepository;
import com.codefathers.repository.interfaces.PurchaseOrderItemRepository;
import com.codefathers.repository.interfaces.ShippingOrderRepository;

public class FinancialService {

    private final PaymentRepository paymentRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShippingOrderRepository shippingOrderRepository;

    private BigDecimal paymentsTotal;
    private BigDecimal purchaseTotal;
    private BigDecimal shippingTotal;
    private BigDecimal orderTotal;

    public FinancialService(PaymentRepository paymentRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            OrderItemRepository orderItemRepository,
            ShippingOrderRepository shippingOrderRepository) {
        this.paymentRepository = paymentRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.shippingOrderRepository = shippingOrderRepository;

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
