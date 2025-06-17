package com.codefathers.service;

import com.codefathers.model.entity.Order;
import com.codefathers.model.enums.OrderStatus;
import com.codefathers.repository.dto.MostSoldProductDTO;
import com.codefathers.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class DashboardService {

    public record SeriesData(List<String> categories, List<BigDecimal> data) {}

    private final OrderService orderService;
    private final ProductService productService;
    private final FinancialService financialService;

    public DashboardService(OrderService orderService,
                            ProductService productService,
                            FinancialService financialService) {
        this.orderService = orderService;
        this.productService = productService;
        this.financialService = financialService;
    }

    /**
     * Prepares the data needed for the sales summary chart.
     * It calls the OrderService to get the raw data, then performs the
     * dashboard-specific logic of aggregation and transformation into a DTO.
     */
    public SeriesData getSalesChartData(LocalDate start, LocalDate end) {
        List<Order> orders = orderService.findOrdersByPeriod(start, end);

        // 2. Perform dashboard-specific business logic (aggregation)
        Map<LocalDate, BigDecimal> salesByDay = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().toLocalDate(),
                        Collectors.mapping(Order::getProductsPrice, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        List<String> categories = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        var formatter = DateTimeFormatter.ofPattern("d/M");

        start.datesUntil(end.plusDays(1)).forEach(day -> {
            categories.add(day.format(formatter));
            data.add(salesByDay.getOrDefault(day, BigDecimal.ZERO));
        });

        return new SeriesData(categories, data);
    }

    public SeriesData getTopProductChartData(LocalDate from, LocalDate to, int limit) {
        List<MostSoldProductDTO> mostSoldProducts = productService.findMostSoldProducts(from, to, limit);

        SeriesData data = mostSoldProducts.stream().collect(Collectors.teeing(
                        Collectors.mapping(MostSoldProductDTO::name, Collectors.toList()),
                        Collectors.mapping(
                                product -> BigDecimal.valueOf(product.totalQuantity()),
                                Collectors.toList()
                        ),
                        SeriesData::new
                )
        );

        return data;
    }

    public SeriesData getOrderStatusChartData(LocalDate from, LocalDate to) {
        List<Order> ordersInPeriod = orderService.findOrdersByPeriod(from, to);

        if (ordersInPeriod == null || ordersInPeriod.isEmpty()) {
            return new SeriesData(new ArrayList<>(), new ArrayList<>());
        }

        Map<OrderStatus, Long> statusCount = ordersInPeriod.stream()
                .collect(Collectors.groupingBy(Order::getOrderStatus, Collectors.counting()));

        BigDecimal totalOrders = BigDecimal.valueOf(ordersInPeriod.size());

        List<String> categories = Arrays.stream(OrderStatus.values())
                .map(OrderStatus::name)
                .toList();

        List<BigDecimal> data = Arrays.stream(OrderStatus.values())
                .map(status -> {
                    long count = statusCount.getOrDefault(status, 0L);
                    return BigDecimal.valueOf(count)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalOrders, 2, RoundingMode.HALF_UP);
                })
                .toList();

        log.debug("Percentile: {}", JsonUtil.toPrettyJson(data));
        log.debug("Order Status: {}", JsonUtil.toPrettyJson(categories));

        return new SeriesData(categories, data);
    }

    public SeriesData getSellersPerformanceChartData(LocalDate from, LocalDate to) {
        financialService.getTopPerformingSellers(from, to, 5);
        return new SeriesData(new ArrayList<>(), new ArrayList<>());
    }
}
