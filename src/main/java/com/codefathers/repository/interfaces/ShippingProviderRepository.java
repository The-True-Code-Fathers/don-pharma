package com.codefathers.repository.interfaces;

import com.codefathers.model.entity.ShippingProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShippingProviderRepository {
    void save(ShippingProvider shippingProvider);

    void update(ShippingProvider shippingProvider);

    void delete(UUID id);

    Optional<ShippingProvider> findByID(UUID id);

    List<ShippingProvider> listAll();
}
