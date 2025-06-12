package com.codefathers.service;

import com.codefathers.model.dto.CreateShippingProviderDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.repository.interfaces.ShippingProviderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShippingProviderService {
    private final ShippingProviderRepository shippingProviderRepository;


    public ShippingProviderService(ShippingProviderRepository shippingProviderRepository) {
        this.shippingProviderRepository = shippingProviderRepository;
    }

    public void registerShippingProvider(CreateShippingProviderDTO dto) {
        validateShippingProviderDTO(dto);

        ShippingProvider shippingProvider = ShippingProvider.builder()
                .id(dto.getId() != null ? dto.getId() : UUID.randomUUID())
                .cnpj(dto.getCnpj())
                .name(dto.getName())
                .basePrice(dto.getBasePrice())
                .dailyCapacity(dto.getDailyCapacity())
                .shippingAreas(processShippingAreas(dto.getShippingAreas()))
                .build();

        shippingProviderRepository.saveShippingProvider(shippingProvider);
    }

    public ShippingProvider searchShippingProviderPerId(UUID shippingId) {
        if (shippingId == null) {
            throw new IllegalArgumentException("ID da transportadora não pode ser nulo");
        }

        // Usa diretamente o repositório que já está otimizado
        return shippingProviderRepository.searchShippingProviderPerId(shippingId);
    }

    public List<ShippingProvider> listAllShippingProviders() {
        return shippingProviderRepository.listAllShippingProviders();
    }

    public ShippingProvider removeShippingProvider(UUID shippingId) {
        if (shippingId == null) {
            throw new IllegalArgumentException("ID da transportadora não pode ser nulo");
        }

        ShippingProvider removed = shippingProviderRepository.removeShippingProviderPerId(shippingId);
        if (removed == null) {
            throw new IllegalArgumentException("Transportadora não encontrada para remoção");
        }
        return removed;
    }

    public void updateShippingProvider(CreateShippingProviderDTO dto) {
        validateShippingProviderDTO(dto);

        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID da transportadora é obrigatório para atualização");
        }

        ShippingProvider existing = shippingProviderRepository.searchShippingProviderPerId(dto.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Transportadora não encontrada para atualização");
        }

        existing.setName(dto.getName());
        existing.setCnpj(dto.getCnpj());
        existing.setBasePrice(dto.getBasePrice());
        existing.setDailyCapacity(dto.getDailyCapacity());
        existing.setShippingAreas(processShippingAreas(dto.getShippingAreas()));

        shippingProviderRepository.updateShippingProvider(existing);
    }

    private void validateShippingProviderDTO(CreateShippingProviderDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO da transportadora não pode ser nulo");
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da transportadora é obrigatório");
        }

        if (dto.getCnpj() == null || dto.getCnpj().trim().isEmpty()) {
            throw new IllegalArgumentException("CNPJ da transportadora é obrigatório");
        }

        if (dto.getBasePrice() == null || dto.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço base inválido");
        }

        if (dto.getDailyCapacity() == null || dto.getDailyCapacity().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Capacidade diária inválida");
        }
    }

    private List<ShippingArea> processShippingAreas(List<ShippingArea> areas) {
        if (areas == null) {
            return List.of();
        }

        return areas.stream()
                .map(area -> ShippingArea.builder()
                        .description(area.getDescription())
                        .states(area.getStates())
                        .build())
                .collect(Collectors.toList());
    }
}