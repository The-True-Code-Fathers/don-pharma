package com.codefathers.service;

import com.codefathers.model.dto.AtualizarStatusShippingProviderDTO;
import com.codefathers.model.dto.CreateShippingProviderDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.repository.interfaces.ShippingProviderRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ShippingProviderService {

    private final ShippingProviderRepository repository;

    public ShippingProviderService(ShippingProviderRepository repository) {
        this.repository = repository;
    }

    public void registerShippingProvider(CreateShippingProviderDTO dto) {
        ShippingProvider provider = ShippingProvider.builder()
                .name(dto.getName())
                .cnpj(dto.getCnpj())
                .basePrice(dto.getBasePrice())
                .dailyCapacity(dto.getDailyCapacity())
                .averageDeliveryDays(0)
                .active(true)
                .createdAt(java.time.LocalDateTime.now())
                .shippingAreas(dto.getShippingAreas())
                .build();

        if (dto.getShippingAreas() != null) {
            for (ShippingArea area : dto.getShippingAreas()) {
                area.setShippingProvider(provider);
            }
        }

        repository.save(provider);
    }

    public void updateShippingProvider(CreateShippingProviderDTO dto) {
        ShippingProvider existing = repository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Transportadora não encontrada"));

        existing.setName(dto.getName());
        existing.setCnpj(dto.getCnpj());
        existing.setBasePrice(dto.getBasePrice());
        existing.setDailyCapacity(dto.getDailyCapacity());
        existing.setActive(dto.isActive());

        existing.getShippingAreas().clear();
        if (dto.getShippingAreas() != null) {
            for (ShippingArea area : dto.getShippingAreas()) {
                area.setShippingProvider(existing);
                existing.getShippingAreas().add(area);
            }
        }

        repository.update(existing);
    }

    public void atualizarStatusTransportadora(AtualizarStatusShippingProviderDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new IllegalArgumentException("DTO ou ID não pode ser nulo.");
        }

        ShippingProvider provider = repository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Transportadora não encontrada."));

        provider.setActive(dto.isActive());
        repository.update(provider);
    }

    // Somente ativos
    public List<ShippingProvider> listAllShippingProviders() {
        return repository.listAll()
                .stream()
                .filter(ShippingProvider::isActive)
                .toList();
    }

    // Todos (ativos e inativos)
    public List<ShippingProvider> listAllShippingProvidersIncludingInactive() {
        return repository.listAll();
    }

    public Optional<ShippingProvider> findById(UUID id) {
        return repository.findById(id);
    }
}

