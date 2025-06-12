//package com.codefathers.service;
//import com.codefathers.model.dto.CreateEmployeeDTO;
//import com.codefathers.model.dto.CreateLinearProgrammingDTO;
//import com.codefathers.model.entity.*;
//import com.codefathers.model.enums.EmployeeRole;
//import com.codefathers.repository.implementations.OrderRepositoryImpl;
//import com.codefathers.repository.implementations.StorageRepositoryImpl;
//import com.codefathers.repository.interfaces.OrderRepository;
//import com.codefathers.repository.interfaces.StorageRepository;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//
//public class LinearProgrammingService {
//
//    OrderRepository orderRepository = new OrderRepositoryImpl();
//    StorageRepository storageRepository = new StorageRepositoryImpl();
//
//    public Set<Product> getProductsSoldBySeller(UUID sellerId) {
//        List<Order> sellerOrders = orderRepository.findBySellerId(sellerId);
//        return sellerOrders.stream()
//                .flatMap(order -> order.getItems().stream())
//                .map(OrderItem::getProduct)
//                .collect(Collectors.toSet());
//    }
//
//    public CreateLinearProgrammingDTO calculateSalesGoalForSeller(Employee seller) {
//        Set<Product> soldProducts = getProductsSoldBySeller(seller.getId());
//        double salesGoal = 0;
//
//        for (Product product : soldProducts) {
//            int stock = storageService.getQuantityStock(product.getSku());
//            salesGoal += stock * product.getSellPrice().doubleValue();
//        }
//
//        // You can retrieve current sales and add business logic here
//        double currentSales = 0; // Implement logic to compute current sales if needed
//
//        // Limit sales goal to max 20% above current sales or predefined goal
//        double maxGoal = currentSales * 1.20;
//        double finalGoal = Math.min(salesGoal, maxGoal);
//
//        return CreateLinearProgrammingDTO.builder()
//                .Id(seller.getId().toString())
//                .name(seller.getFullName())
//                .role(seller.getRole())
//                .currentSales(currentSales)
//                .salesGoal(finalGoal)
//                .additionalSalesNeeded(Math.max(0, finalGoal - currentSales))
//                .build();
//    }
//}
//
