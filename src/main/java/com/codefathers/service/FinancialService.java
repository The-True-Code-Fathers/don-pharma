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
    
    private PaymentRepository paymentRepository;
    private PurchaseOrderItemRepository purchaseOrderItemRepository;
    private OrderItemRepository orderItemRepository;
    private ShippingOrderRepository shippingOrderRepository;

    public FinancialService(PaymentRepository paymentRepository, PurchaseOrderItemRepository purchaseOrderItemRepository, OrderItemRepository orderItemRepository, ShippingOrderRepository shippingOrderRepository) {
        this.paymentRepository = paymentRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.shippingOrderRepository = shippingOrderRepository;
    }

     public BigDecimal getTotalOutflows() {
        BigDecimal paymentsTotal = paymentRepository.listAll().stream()
                .map(Payment::getGrossIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal purchaseTotal = purchaseOrderItemRepository.findAll().stream()
                .map(PurchaseOrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingTotal = shippingOrderRepository.listAll().stream()
                .map(ShippingOrder::getShippingCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return paymentsTotal.add(purchaseTotal).add(shippingTotal);
    }

    // Total das entradas de caixa (vendas)
    public BigDecimal getTotalInflows() {
        return orderItemRepository.listAll().stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Saldo líquido (entradas - saídas)
    public BigDecimal getNetCashFlow() {
        return getTotalInflows().subtract(getTotalOutflows());
    }
}