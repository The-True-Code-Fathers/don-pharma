package com.codefathers.repository.interfaces;

import com.codefathers.model.entity.ShippingOrder;

import java.util.List;
import java.util.UUID;

public interface ShippingOrderRepository {
    void saveShippingOrder(ShippingOrder shippingOrder);
    ShippingOrder searchShippingOrderPerId(UUID orderId);
    List<ShippingOrder> listAllShippingOrders();
    ShippingOrder removeShippingOrderPerId(UUID orderId);
}
