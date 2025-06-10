package com.codefathers.repository.interfaces;

import com.codefathers.model.entity.ShippingArea;

import java.util.List;
import java.util.UUID;

public interface ShippingAreaRepository {
    void saveShippingArea(ShippingArea shippingArea);
    ShippingArea searchShippingAreaPerID(UUID areaId);
    List<ShippingArea> listAllShippingAreas();
    ShippingArea removeShippingAreaPerId(UUID areaId);
}
