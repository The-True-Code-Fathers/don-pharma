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

    private BigDecimal netPayment = BigDecimal.ZERO;
    private BigDecimal cashFlow = BigDecimal.valueOf(200000);
    private BigDecimal purchaseTotal = BigDecimal.ZERO;
    private BigDecimal shippingTotal = BigDecimal.ZERO;
    private BigDecimal orderTotal = BigDecimal.ZERO;

    // Construtor sem parâmetros de data, para compatibilidade caso necessário
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
    }

    public void calculateFinancialData(LocalDate from, LocalDate to) {
        this.netPayment = BigDecimal.ZERO;
        this.purchaseTotal = BigDecimal.ZERO;
        this.shippingTotal = BigDecimal.ZERO;
        this.orderTotal = BigDecimal.ZERO;
        this.cashFlow = BigDecimal.valueOf(200000);
        
        calculateOutflows(from, to);
        calculateInflows(from, to);
    }

    public void calculateOutflows(LocalDate from, LocalDate to) {

        for (Payment pagamento : paymentRepository.listByTimePeriod(from, to)) {
            netPayment = netPayment.add(pagamento.getGrossIncome())
                    .add(pagamento.getFoodVoucherAmount())
                    .add(pagamento.getMealVoucherAmount())
                    .add(pagamento.getProfitSharingAmount())
                    .subtract(pagamento.getAmountInTaxes());
        }

        for (PurchaseOrderItem purchaseOrderItem : purchaseOrderItemRepository.listByTimePeriod(from, to)) {
            BigDecimal itemTotal = purchaseOrderItem.getPrice()
                    .multiply(BigDecimal.valueOf(purchaseOrderItem.getQuantity()));
            purchaseTotal = purchaseTotal.add(itemTotal);
        }

        shippingTotal = shippingOrderRepository.listByTimePeriod(from, to).stream()
                .map(ShippingOrder::getShippingCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cashFlow = cashFlow.subtract(purchaseTotal).subtract(shippingTotal).subtract(netPayment);

        System.out.println("LOG !!!!!" + cashFlow + " " + shippingTotal + " " + purchaseTotal + " " + netPayment + " " + netPayment);
        System.out.println("LOG!!!" + purchaseOrderItemRepository.listByTimePeriod(from, to) + " " +  paymentRepository.listByTimePeriod(from, to) + " " +  shippingOrderRepository.listByTimePeriod(from, to));
    }

    public void calculateInflows(LocalDate from, LocalDate to) {
        for (OrderItem orderItem : orderItemRepository.listByTimePeriod(from, to)) {
            BigDecimal itemTotal = orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            orderTotal = orderTotal.add(itemTotal);
        }

        cashFlow = cashFlow.add(orderTotal);
        System.out.println("LOG!!! " + orderTotal);
    }

    public Map<Employee, Double> getTopPerformingSellers(LocalDate from, LocalDate to, int limit) {
        return orderRepository.findSellersRankedByOrderStatus(from, to, OrderStatus.INVOICED, limit);
    }

    public BigDecimal getPurchaseTotal() {
        return purchaseTotal;
    }

    public BigDecimal getShippingTotal() {
        return shippingTotal;
    }

    public BigDecimal getTotalOutflows() {
        return netPayment.add(purchaseTotal).add(shippingTotal);
    }

    public BigDecimal getTotalInflows() {
        return orderTotal;
    }

    public BigDecimal getNetCashFlow() {
        return orderTotal.subtract(getTotalOutflows());
    }

    public BigDecimal getCashFlow() {
        return cashFlow;
    }

    public BigDecimal getNetPayment() {
        return netPayment;
    }
}