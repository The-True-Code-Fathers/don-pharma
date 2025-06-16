package com.codefathers.service;

import com.codefathers.model.dto.EmployeeSalesDataDTO;
import com.codefathers.model.dto.EmployeeSalesValueDataDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Order;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.repository.dto.MostSoldProductDTO;
import com.codefathers.util.JsonUtil;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.codefathers.util.HibernateUtil.sessionFactory;

@Slf4j
public class DashboardService {

    public record SeriesData(List<String> categories, List<BigDecimal> data) {}

    private final OrderService orderService;
    private final ProductService productService;

    public DashboardService(OrderService orderService, ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
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

        log.debug("{}", JsonUtil.toPrettyJson(data));

        return data;
    }

    public SeriesData getTopEmployeeChartData() {
        List<EmployeeSalesDataDTO> employeeSoldProducts = orderService.buscarVendasPorVendedor();

        SeriesData data = employeeSoldProducts.stream().collect(Collectors.teeing(
                        Collectors.mapping(EmployeeSalesDataDTO::getEmployeeName, Collectors.toList()),
                        Collectors.mapping(
                                product -> BigDecimal.valueOf(product.getSalesCount()),
                                Collectors.toList()
                        ),
                        SeriesData::new
                )
        );

        log.debug("{}", JsonUtil.toPrettyJson(data));

        return data;
    }



}
