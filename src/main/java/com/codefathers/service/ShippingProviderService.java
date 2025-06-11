package com.codefathers.service;

import com.codefathers.model.dto.CreateShippingProviderDTO;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.interfaces.ShippingProviderRepository;

import java.util.List;
import java.util.UUID;

public class ShippingProviderService {
    private ShippingProviderRepository shippingProviderRepository;

    public ShippingProviderService(ShippingProviderRepository shippingProviderRepository) {
        this.shippingProviderRepository = shippingProviderRepository;
    }

    public void registerShippingProvider(CreateShippingProviderDTO createShippingProviderDTO) {
        ShippingProvider shippingProvider = ShippingProvider.builder()
                .cnpj(createShippingProviderDTO.getCnpj())
                .name(createShippingProviderDTO.getName())
                .basePrice(createShippingProviderDTO.getBasePrice())
                .dailyCapacity(createShippingProviderDTO.getDailyCapacity())
                // .shippingAreas(createShippingProviderDTO.getShippingAreas())
                .build();
        shippingProviderRepository.saveShippingProvider(shippingProvider);
    }

    public ShippingProvider searchShippingProviderPerId(UUID shippingId) {
        try {
            return listAllShippingProviders()
                    .stream()
                    .filter(shippingProvider -> shippingProvider.getId()
                    .equals(shippingId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public List<ShippingProvider> listAllShippingProviders() {
        return shippingProviderRepository.listAllShippingProviders();
    }

    public ShippingProvider removeShippingProvider(UUID shippingId) {
        return shippingProviderRepository.removeShippingProviderPerId(shippingId);
    }
}
