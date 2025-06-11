package com.codefathers.service;

import com.codefathers.model.dto.CreateShippingAreaDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.repository.ShippingAreaRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import java.util.List;
import java.util.UUID;

public class ShippingAreaService {
    private final ShippingAreaRepository shippingAreaRepository;
    private final Validator validator;

    public ShippingAreaService(ShippingAreaRepository shippingAreaRepository, Validator validator) {
        this.shippingAreaRepository = shippingAreaRepository;
        this.validator = validator;
    }

    public void saveShippingArea(@Valid CreateShippingAreaDTO dto){
        var violations = validator.validate(dto);

        if (!violations.isEmpty()){
            throw new ConstraintViolationException(violations);
        }

        ShippingArea shippingArea = ShippingArea.builder()
                .shippingProvider(dto.getShippingProvider())
                .description(dto.getDescription())
                .states(dto.getStates())
                .build();

        try{
            shippingAreaRepository.saveShippingArea(shippingArea);
        }catch (ConstraintViolationException e){
            System.out.println("Erro ao salvar ShippingArea: " + e.getMessage());
        }
    }


    public ShippingArea findShippingAreaById(UUID id) {
        ShippingArea area = shippingAreaRepository.searchShippingAreaPerID(id);
        if (area == null) {
            throw new RuntimeException("Área de entrega com ID '" + id + "' não encontrada.");
        }
        return area;
    }

    public void deleteShippingAreaById(UUID id) {
        ShippingArea area = shippingAreaRepository.removeShippingAreaPerId(id);
        if (area == null) {
            throw new RuntimeException("Não foi possível remover a área de entrega com ID '" + id + "'.");
        }
    }

    public void updateShippingArea(@Valid ShippingArea shippingArea) {
        var violations = validator.validate(shippingArea);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        shippingAreaRepository.updateShippingArea(shippingArea);
    }

    public List<ShippingArea> findAllShippingAreas() {
        return shippingAreaRepository.listAllShippingAreas();
    }

}
