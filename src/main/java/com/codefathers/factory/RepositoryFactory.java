package com.codefathers.factory;

import com.codefathers.repository.implementations.*;
import com.codefathers.repository.interfaces.*;
import lombok.Getter;

public class RepositoryFactory {
    @Getter
    private static EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();

    @Getter
    private static OrderRepository orderRepository = new OrderRepositoryImpl();

    @Getter
    private static PaymentRepository paymentRepository = new PaymentRepositoryImpl();

    @Getter
    private static ProductRepository productRepository = new ProductRepositoryImpl();

    @Getter
    private static PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepositoryImpl();

    @Getter
    private static ShippingAreaRepository shippingAreaRepository = new ShippingAreaRepositoryImpl();

    @Getter
    private static ShippingOrderRepository shippingOrderRepository = new ShippingOrderRepositoryImpl();

    @Getter
    private static ShippingProviderRepository shippingProviderRepository = new ShippingProviderRepositoryImpl();

    @Getter
    private static StorageRepository storageRepository = new StorageRepositoryImpl();

    @Getter
    private static SystemUserRepository systemUserRepository = new SystemUserRepositoryImpl();
}
