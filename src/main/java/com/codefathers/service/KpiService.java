package com.codefathers.service;

import com.codefathers.model.entity.*;
import com.codefathers.repository.dto.MostSoldProductDTO;
import com.codefathers.repository.interfaces.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
@RequiredArgsConstructor
public class KpiService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StorageRepository storageRepository;
    private final PaymentRepository paymentRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final ShippingOrderRepository shippingOrderRepository;

    /**
     * Total de pedidos em um intervalo de datas
     */
    public long getTotalOrders(LocalDate start, LocalDate end) {
        return orderRepository.findOrdersByTimePeriod(start, end).size();
    }

    /**
     * Receita total no intervalo de datas
     */
    public BigDecimal getTotalRevenue(LocalDate start, LocalDate end) {
        // Pode adaptar para somar somente os pedidos no período
        List<?> orders = orderRepository.findOrdersByTimePeriod(start, end);
        return orders.stream()
                .map(order -> ((com.codefathers.model.entity.Order) order).getProductsPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalSpendings(LocalDate start, LocalDate end) {
        BigDecimal netPayment = BigDecimal.ZERO;
        for (Payment pagamento : paymentRepository.listByTimePeriod(start, end)) {
            netPayment = netPayment.add(pagamento.getGrossIncome()).add(pagamento.getFoodVoucherAmount()).add(pagamento.getMealVoucherAmount()).add(pagamento.getProfitSharingAmount()).subtract(pagamento.getAmountInTaxes());
        }

        BigDecimal paymentsTotal = paymentRepository.listAll().stream()
                .map(Payment::getGrossIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal purchaseTotal = BigDecimal.ZERO;

        for (PurchaseOrderItem purchaseOrderItem : purchaseOrderItemRepository.findAll()) {
            BigDecimal itemTotal = purchaseOrderItem.getPrice()
                    .multiply(BigDecimal.valueOf(purchaseOrderItem.getQuantity()));
            purchaseTotal = purchaseTotal.add(itemTotal);
        }

        BigDecimal shippingTotal = shippingOrderRepository.listAll().stream()
                .map(ShippingOrder::getShippingCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return purchaseTotal.add(shippingTotal).add(netPayment);
    }


    /**
     * Produtos mais vendidos no período
     */
    public List<MostSoldProductDTO> getMostSoldProducts(LocalDate start, LocalDate end, int limit) {
        return productRepository.findMostSoldProducts(start, end, limit);
    }

    /**
     * Consulta o estoque disponível de um produto pelo SKU
     */
    public Optional<Integer> getStockQuantityBySku(String sku) {
        Optional<Storage> storageOpt = storageRepository.findByProductSku(sku);
        return storageOpt.map(Storage::getProductQuantity);
    }

    /**
     * Total geral em estoque (somatório das quantidades)
     */
    public int getTotalStockQuantity() {
        List<Storage> allStorage = storageRepository.listAll();
        return allStorage.stream()
                .mapToInt(Storage::getProductQuantity)
                .sum();
    }

    public long getTotalInvoicedOrders(LocalDate start, LocalDate end) {
        return orderRepository.countInvoicedOrders(start, end);
    }



    public BigDecimal getAverageTicket(LocalDate start, LocalDate end) {
        List<Order> invoicedOrders = orderRepository.findInvoicedOrders(start, end);
        if (invoicedOrders.isEmpty()) return BigDecimal.ZERO;

        BigDecimal total = invoicedOrders.stream()
                .map(Order::getProductsPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(invoicedOrders.size()), 2, RoundingMode.HALF_UP);
    }


}
