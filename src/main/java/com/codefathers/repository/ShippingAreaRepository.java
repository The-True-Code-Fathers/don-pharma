package com.codefathers.repository;

import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;

import java.util.List;
import java.util.UUID;

public interface ShippingAreaRepository {
    void saveShippingArea(ShippingArea shippingArea);
    ShippingArea searchShippingAreaPerID(UUID areaId);
    List<ShippingArea> listAllShippingAreas();
    ShippingArea removeShippingAreaPerId(UUID areaId);
}
