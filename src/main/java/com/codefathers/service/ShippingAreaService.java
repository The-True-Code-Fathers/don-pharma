package com.codefathers.service;

import com.codefathers.model.dto.CreateShippingAreaDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.repository.interfaces.ShippingAreaRepository;
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

    public void saveShippingArea(@Valid CreateShippingAreaDTO createShippingAreaDTO){
        var violations = validator.validate(createShippingAreaDTO);

        if (!violations.isEmpty()){
            throw new ConstraintViolationException(violations);
        }

        ShippingArea shippingArea = ShippingArea.builder()
                .shippingProvider(createShippingAreaDTO.getShippingProvider())
                .description(createShippingAreaDTO.getDescription())
                .states(createShippingAreaDTO.getStates())
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
        shippingAreaRepository.delete(shippingAreaId);
    }

    public void updateShippingArea(@Valid ShippingArea shippingArea) {
        var violations = validator.validate(shippingArea);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        shippingAreaRepository.update(shippingArea);
    }

    public List<ShippingArea> findAllShippingAreas() {
        return shippingAreaRepository.listAll();
    }

}
