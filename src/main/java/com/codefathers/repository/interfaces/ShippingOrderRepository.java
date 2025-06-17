package com.codefathers.repository.interfaces;

import com.codefathers.model.entity.ShippingOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShippingOrderRepository {
    void save(ShippingOrder shippingOrder);

    void update(ShippingOrder shippingOrder);

    void delete(UUID id);

    Optional<ShippingOrder> findById(UUID id);

    List<ShippingOrder> listAll();

    List<ShippingOrder> listByTimePeriod(LocalDate from, LocalDate to);
}
