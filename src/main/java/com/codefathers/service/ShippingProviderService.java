package com.codefathers.service;

import com.codefathers.model.dto.CreateShippingProviderDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.interfaces.ShippingProviderRepository;
import com.codefathers.util.HibernateUtil;
import org.hibernate.Session;

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
                .cnpj(dto.getCnpj())
                .name(dto.getName())
                .basePrice(dto.getBasePrice())
                .dailyCapacity(dto.getDailyCapacity())
                .averageDeliveryDays(1)
                .build();

        // Processa as áreas de entrega
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

        // Busca a transportadora existente
        ShippingProvider existing = shippingProviderRepository.findById(dto.getId()).get();
        if (existing == null) {
            throw new IllegalArgumentException("Transportadora não encontrada");
        }

        // Atualiza os campos
        existing.setName(dto.getName());
        existing.setCnpj(dto.getCnpj());
        existing.setBasePrice(dto.getBasePrice());
        existing.setDailyCapacity(dto.getDailyCapacity());

        // Processa as áreas de entrega
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
                        .id(area.getId() != null ? area.getId() : UUID.randomUUID()) // garante UUID
                        .description(area.getDescription())
                        .states(area.getStates())
                        .shippingProvider(provider)
                        .build())
                .collect(Collectors.toList());
    }


}