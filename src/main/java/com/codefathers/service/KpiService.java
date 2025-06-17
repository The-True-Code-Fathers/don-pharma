package com.codefathers.service;

import com.codefathers.model.entity.Storage;
import com.codefathers.repository.dto.MostSoldProductDTO;
import com.codefathers.repository.interfaces.OrderRepository;
import com.codefathers.repository.interfaces.ProductRepository;
import com.codefathers.repository.interfaces.StorageRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
@RequiredArgsConstructor
public class KpiService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StorageRepository storageRepository;

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
}
