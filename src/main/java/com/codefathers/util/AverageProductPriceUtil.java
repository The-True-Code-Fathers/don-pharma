package com.codefathers.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.codefathers.model.entity.OrderItem;
import com.codefathers.model.entity.Product;
import com.codefathers.model.entity.PurchaseOrderItem;
import com.codefathers.repository.implementations.OrderItemRepositoryImpl;
import com.codefathers.repository.implementations.OrderRepositoryImpl;
import com.codefathers.repository.implementations.ProductRepositoryImpl;
import com.codefathers.repository.implementations.PurchaseOrderItemRepositoryImpl;
import com.codefathers.repository.implementations.PurchaseOrderRepositoryImpl;
import com.codefathers.repository.interfaces.OrderItemRepository;
import com.codefathers.repository.interfaces.OrderRepository;
import com.codefathers.repository.interfaces.ProductRepository;
import com.codefathers.repository.interfaces.PurchaseOrderItemRepository;
import com.codefathers.repository.interfaces.PurchaseOrderRepository;
import com.codefathers.service.OrderItemService;
import com.codefathers.service.ProductService;
import com.codefathers.service.PurchaseOrderItemService;

public class AverageProductPriceUtil {

    static ProductRepository productRepository = new ProductRepositoryImpl();
    static OrderItemRepository orderItemRepository = new OrderItemRepositoryImpl();
    static OrderRepository orderRepository = new OrderRepositoryImpl();
    static PurchaseOrderItemRepository purchaseOrderItemRepository = new PurchaseOrderItemRepositoryImpl();
    static PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepositoryImpl();
    static ProductService productService = new ProductService(productRepository, ValidatorUtil.getValidator());
    static OrderItemService orderItemService = new OrderItemService(orderItemRepository, orderRepository);
    static PurchaseOrderItemService purchaseOrderItemService = new PurchaseOrderItemService(purchaseOrderItemRepository,
            purchaseOrderRepository, ValidatorUtil.getValidator());

    public static String calculateWeightedAverageSellPrice(Product product) {
        try {
            List<OrderItem> saleOrdersItems = orderItemService.findByProductSku(product.getSku()).orElse(List.of());
            if (saleOrdersItems.isEmpty())
                return "R$ 0.00";

            BigDecimal totalValue = BigDecimal.ZERO;
            BigDecimal totalQuantity = BigDecimal.ZERO;

            for (OrderItem orderItem : saleOrdersItems) {
                BigDecimal orderItemValue = orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
                totalValue = totalValue.add(orderItemValue);
                totalQuantity = totalQuantity.add(BigDecimal.valueOf(orderItem.getQuantity()));
            }

            if (totalQuantity.compareTo(BigDecimal.ZERO) == 0)
                return "R$ 0.00";

            BigDecimal weightedAverage = totalValue.divide(totalQuantity, 2, RoundingMode.HALF_UP);
            return "R$ " + String.format("%.2f", weightedAverage);
        } catch (Exception e) {
            return "Error";
        }
    }

    public static String calculateWeightedAverageBuyPrice(Product product) {
        try {
            List<PurchaseOrderItem> purchaseOrdersItems = purchaseOrderItemService.findByProductSku(product.getSku());
            if (purchaseOrdersItems.isEmpty())
                return "R$ 0.00";

            BigDecimal totalValue = BigDecimal.ZERO;
            BigDecimal totalQuantity = BigDecimal.ZERO;

            for (PurchaseOrderItem order : purchaseOrdersItems) {
                BigDecimal orderValue = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
                totalValue = totalValue.add(orderValue);
                totalQuantity = totalQuantity.add(BigDecimal.valueOf(order.getQuantity()));
            }

            if (totalQuantity.compareTo(BigDecimal.ZERO) == 0)
                return "R$ 0.00";

            BigDecimal weightedAverage = totalValue.divide(totalQuantity, 2, RoundingMode.HALF_UP);
            return "R$ " + String.format("%.2f", weightedAverage);
        } catch (Exception e) {
            return "Error";
        }
    }
}
