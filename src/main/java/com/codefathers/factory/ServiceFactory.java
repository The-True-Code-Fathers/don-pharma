package com.codefathers.factory;

import com.codefathers.service.*;
import com.codefathers.util.ValidatorUtil;
import lombok.Getter;

public class ServiceFactory {

    @Getter
    private static AuthService authService =
            new AuthService(RepositoryFactory.getSystemUserRepository());

    @Getter
    private static EmployeeService employeeService =
            new EmployeeService(RepositoryFactory.getEmployeeRepository(), ValidatorUtil.getValidator());


    @Getter
    private static OrderService orderService =
            new OrderService(
                    RepositoryFactory.getOrderRepository(),
                    RepositoryFactory.getEmployeeRepository(),
                    RepositoryFactory.getStorageRepository(),
                    ValidatorUtil.getValidator()
            );

    @Getter
    private static PaymentService paymentService =
            new PaymentService(
                    RepositoryFactory.getPaymentRepository(),
                    RepositoryFactory.getEmployeeRepository()
            );

    @Getter
    private static ProductService productService =
            new ProductService(
                    RepositoryFactory.getProductRepository(),
                    ValidatorUtil.getValidator()
            );

    @Getter
    private static PurchaseOrderService purchaseOrderService =
            new PurchaseOrderService(
                RepositoryFactory.getPurchaseOrderRepository(),
                RepositoryFactory.getEmployeeRepository(),
                RepositoryFactory.getStorageRepository(),
                ValidatorUtil.getValidator()
            );

    @Getter
    private static ShippingAreaService shippingAreaService =
            new ShippingAreaService(
                    RepositoryFactory.getShippingAreaRepository(),
                    ValidatorUtil.getValidator()
            );

    @Getter
    private static ShippingOrderService shippingOrderService =
            new ShippingOrderService(
                    RepositoryFactory.getShippingOrderRepository(),
                    RepositoryFactory.getShippingProviderRepository(),
                    RepositoryFactory.getOrderRepository(),
                    RepositoryFactory.getPurchaseOrderRepository(),
                    ValidatorUtil.getValidator()
            );

    @Getter
    private static ShippingProviderService shippingProvider =
            new ShippingProviderService(
                    RepositoryFactory.getShippingProviderRepository()
            );

    @Getter
    private static StorageService storageService =
            new StorageService(
                    RepositoryFactory.getStorageRepository(),
                    RepositoryFactory.getProductRepository()
            );

    @Getter
    private static FinancialService financialService =
            new FinancialService(RepositoryFactory.getPaymentRepository(),
                        RepositoryFactory.getPurchaseOrderItemRepository(),
                        RepositoryFactory.getOrderItemRepository(),
                        RepositoryFactory.getShippingOrderRepository(),
                        RepositoryFactory.getOrderRepository()
                    );

    @Getter
    private static DashboardService dashboardService =
            new DashboardService(
                ServiceFactory.getOrderService(),
                    ServiceFactory.getProductService(),
                    ServiceFactory.getFinancialService()
            );
}
