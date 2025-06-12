package com.codefathers.repository.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.entity.ShippingArea;

public interface ShippingAreaRepository {
    void save(ShippingArea shippingArea);

    void update(ShippingArea shippingArea);

    void delete(UUID id);

    Optional<ShippingArea> findById(UUID id);

    List<ShippingArea> listAll();
}
