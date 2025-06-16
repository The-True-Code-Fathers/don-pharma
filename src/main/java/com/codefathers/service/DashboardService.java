package com.codefathers.service;

import com.codefathers.model.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardService {

    public record TimeSeriesData(List<String> categories, List<BigDecimal> data) {}

    private final OrderService orderService;

    public DashboardService(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Prepares the data needed for the sales summary chart.
     * It calls the OrderService to get the raw data, then performs the
     * dashboard-specific logic of aggregation and transformation into a DTO.
     */
    public TimeSeriesData getSalesChartData(LocalDate start, LocalDate end) {
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

        return new TimeSeriesData(categories, data);
    }
}
