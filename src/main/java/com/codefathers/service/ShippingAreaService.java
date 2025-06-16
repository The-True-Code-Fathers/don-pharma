package com.codefathers.service;

import com.codefathers.model.dto.CreateShippingAreaDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.repository.interfaces.ShippingAreaRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ShippingAreaService {
    private final ShippingAreaRepository shippingAreaRepository;
    private final Validator validator;

    public ShippingAreaService(ShippingAreaRepository shippingAreaRepository, Validator validator) {
        this.shippingAreaRepository = shippingAreaRepository;
        this.validator = validator;
    }

    public void saveShippingArea(@Valid CreateShippingAreaDTO createShippingAreaDTO) {
        var violations = validator.validate(createShippingAreaDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        ShippingArea shippingArea = ShippingArea.builder()
                .shippingProvider(createShippingAreaDTO.getShippingProvider())
                .description(createShippingAreaDTO.getDescription())
                .states(createShippingAreaDTO.getStates())
                .createdAt(LocalDateTime.now())
                .active(true)
                .cep(createShippingAreaDTO.getCep())
                .build();

        shippingAreaRepository.save(shippingArea);
    }

    public ShippingArea findShippingAreaById(UUID shippingAreaId) {
        return shippingAreaRepository.findById(shippingAreaId)
                .orElseThrow(() -> new RuntimeException("Área de entrega com ID '" + shippingAreaId + "' não encontrada."));
    }

    public void updateShippingArea(@Valid ShippingArea shippingArea) {
        var violations = validator.validate(shippingArea);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        shippingAreaRepository.update(shippingArea);
    }

    public void atualizarStatusShippingArea(UUID id) {
        Optional<ShippingArea> areaOpt = shippingAreaRepository.findById(id);
        if (areaOpt.isEmpty()) {
            throw new RuntimeException("Área de entrega com ID '" + id + "' não encontrada.");
        }

        ShippingArea area = areaOpt.get();
        area.setActive(!area.isActive());
        shippingAreaRepository.update(area);
    }

    public List<ShippingArea> findAllShippingAreas() {
        try {
            List<ShippingArea> areas = shippingAreaRepository.listAll();

            // Log para diagnóstico
            System.out.println("Total de áreas encontradas: " + areas.size());
            areas.forEach(area -> {
                System.out.println("Área ID: " + area.getId());
                System.out.println("Descrição: " + area.getDescription());
                System.out.println("Estados: " + String.join(", ", area.getStates()));
                System.out.println("Transportadora: " +
                        (area.getShippingProvider() != null ? area.getShippingProvider().getName() : "Nenhuma"));
                System.out.println("Status: " + (area.isActive() ? "Ativo" : "Inativo"));
                System.out.println("-------------------");
            });

            return areas;
        } catch (Exception e) {
            System.out.println("Erro ao buscar áreas: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
}
