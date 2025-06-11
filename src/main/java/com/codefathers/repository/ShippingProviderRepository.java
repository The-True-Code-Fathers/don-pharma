package com.codefathers.repository;

import com.codefathers.model.entity.ShippingProvider;

import java.util.List;
import java.util.UUID;

public interface ShippingProviderRepository {
    void saveShippingProvider(ShippingProvider shippingProvider);
    ShippingProvider searchShippingProviderPerId(UUID shippingId);
    List<ShippingProvider> listAllShippingProviders();
    ShippingProvider removeShippingProviderPerId(UUID shippingId);
    void updateShippingProvider(ShippingProvider shippingProvider);
}
