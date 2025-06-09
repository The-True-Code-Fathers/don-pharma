package com.codefathers.service;

import com.codefathers.model.entity.ShippingOrder;
import com.codefathers.repository.ShippingOrderRepository;
import java.util.List;
import java.util.UUID;

public class ShippingOrderService {
    private ShippingOrderRepository shippingOrderRepository;

    public ShippingOrderService(ShippingOrderRepository shippingOrderRepository) {
        this.shippingOrderRepository = shippingOrderRepository;
    }

    public void saveShippingOrder(ShippingOrder shippingOrder) {
        shippingOrderRepository.saveShippingOrder(shippingOrder);
    }

    public ShippingOrder searchShippingOrder(UUID orderId) {
        return shippingOrderRepository.searchShippingOrderPerId(orderId);
    }

    public List<ShippingOrder> listAllShippingOrders() {
        return shippingOrderRepository.listAllShippingOrders();
    }

    public ShippingOrder removeShippingOrder(UUID orderId) {
        return shippingOrderRepository.removeShippingOrderPerId(orderId);
    }
}
