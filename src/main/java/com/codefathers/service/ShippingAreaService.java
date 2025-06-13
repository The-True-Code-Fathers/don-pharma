package com.codefathers.service;

import com.codefathers.model.dto.CreateShippingAreaDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.repository.interfaces.ShippingAreaRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ShippingAreaService {
    private final ShippingAreaRepository shippingAreaRepository;
    private final Validator validator;

    public ShippingAreaService(ShippingAreaRepository shippingAreaRepository, Validator validator) {
        this.shippingAreaRepository = shippingAreaRepository;
        this.validator = validator;
    }

    public void saveShippingArea(@Valid CreateShippingAreaDTO createShippingAreaDTO){
        var violations = validator.validate(createShippingAreaDTO);

        if (!violations.isEmpty()){
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

        try{
            shippingAreaRepository.save(shippingArea);
        }catch (ConstraintViolationException e){
            System.out.println("Erro ao salvar ShippingArea: " + e.getMessage());
        }
    }


    public ShippingArea findShippingAreaById(UUID shippingAreaId) {
        ShippingArea area = shippingAreaRepository.findById(shippingAreaId).get();
        if (area == null) {
            throw new RuntimeException("Área de entrega com ID '" + shippingAreaId + "' não encontrada.");
        }
        return area;
    }

    public void deleteShippingAreaById(UUID shippingAreaId) {
        var shippingArea = shippingAreaRepository.findById(shippingAreaId).get();
        if (shippingArea == null) {
            throw new RuntimeException("Não foi possível remover a área de entrega com ID '" + shippingAreaId + "'.");
        }
    }

    public void updateShippingArea(@Valid ShippingArea shippingArea) {
        var violations = validator.validate(shippingArea);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        shippingAreaRepository.update(shippingArea);
    }

    public List<ShippingArea> findAllShippingAreas() {
        try {
            List<ShippingArea> areas = shippingAreaRepository.listAll();

            // Log detalhado para diagnóstico
            System.out.println("Total de áreas encontradas: " + areas.size());
            areas.forEach(area -> {
                System.out.println("Área ID: " + area.getId());
                System.out.println("Descrição: " + area.getDescription());
                System.out.println("Estados: " + String.join(", ", area.getStates()));

                if (area.getShippingProvider() != null) {
                    System.out.println("Transportadora: " + area.getShippingProvider().getName());
                } else {
                    System.out.println("Transportadora: Nenhuma associada");
                }
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
