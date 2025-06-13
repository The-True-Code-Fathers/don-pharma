package com.codefathers.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.codefathers.model.dto.CreateShippingProviderDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.interfaces.ShippingProviderRepository;

public class ShippingProviderService {
    private final ShippingProviderRepository shippingProviderRepository;

    public ShippingProviderService(ShippingProviderRepository shippingProviderRepository) {
        this.shippingProviderRepository = shippingProviderRepository;
    }

    // Método novo adicionado
    public List<ShippingProvider> listProvidersByState(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return listAllShippingProviders();
        }

        String estadoUpper = estado.toUpperCase();
        return shippingProviderRepository.listAll().stream()
                .filter(provider -> providerAtendeEstado(provider, estadoUpper))
                .collect(Collectors.toList());
    }

    // Método auxiliar novo
    private boolean providerAtendeEstado(ShippingProvider provider, String estado) {
        return provider.getShippingAreas().stream()
                .anyMatch(area -> Arrays.asList(area.getStates()).contains(estado));
    }

    // Métodos existentes (mantidos sem alteração)
    public void registerShippingProvider(CreateShippingProviderDTO dto) {
        validateShippingProviderDTO(dto);

        ShippingProvider shippingProvider = ShippingProvider.builder()
                .cnpj(dto.getCnpj())
                .name(dto.getName())
                .basePrice(dto.getBasePrice())
                .dailyCapacity(dto.getDailyCapacity())
                .averageDeliveryDays(1)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        List<ShippingArea> areas = processShippingAreas(dto.getShippingAreas(), shippingProvider);
        shippingProvider.setShippingAreas(areas);

        shippingProviderRepository.save(shippingProvider);
    }

    public ShippingProvider searchShippingProviderPerId(UUID shippingId) {
        if (shippingId == null) {
            throw new IllegalArgumentException("ID da transportadora não pode ser nulo");
        }
        return shippingProviderRepository.findById(shippingId).get();
    }

    public List<ShippingProvider> listAllShippingProviders() {
        return shippingProviderRepository.listAll();
    }

    public void removeShippingProvider(UUID shippingId) {
        if (shippingId == null) {
            throw new IllegalArgumentException("ID da transportadora não pode ser nulo");
        }

        ShippingProvider removed = shippingProviderRepository.findById(shippingId).get();
        if (removed == null) {
            throw new IllegalArgumentException("Transportadora não encontrada para remoção");
        }
    }

    public void updateShippingProvider(CreateShippingProviderDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("ID da transportadora é obrigatório para atualização");
        }

        ShippingProvider existing = shippingProviderRepository.findById(dto.getId()).get();
        if (existing == null) {
            throw new IllegalArgumentException("Transportadora não encontrada");
        }

        existing.setName(dto.getName());
        existing.setCnpj(dto.getCnpj());
        existing.setBasePrice(dto.getBasePrice());
        existing.setDailyCapacity(dto.getDailyCapacity());

        List<ShippingArea> areas = processShippingAreas(dto.getShippingAreas(), existing);
        existing.setShippingAreas(areas);

        shippingProviderRepository.update(existing);
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

    private List<ShippingArea> processShippingAreas(List<ShippingArea> areas, ShippingProvider provider) {
        if (areas == null || areas.isEmpty()) {
            return List.of();
        }

        return areas.stream()
                .map(area -> ShippingArea.builder()
                        .id(area.getId() != null ? area.getId() : UUID.randomUUID())
                        .description(area.getDescription())
                        .states(area.getStates())
                        .shippingProvider(provider)
                        .build())
                .collect(Collectors.toList());
    }

    public void update(ShippingProvider shippingProvider) {
        shippingProviderRepository.update(shippingProvider);
    }

}