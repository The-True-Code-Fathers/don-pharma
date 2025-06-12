package com.codefathers.service;

import java.util.List;
import java.util.UUID;

import com.codefathers.model.dto.CreateShippingOrderDTO;
import com.codefathers.model.entity.ShippingOrder;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.interfaces.ShippingOrderRepository;
import com.codefathers.repository.interfaces.ShippingProviderRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

public class ShippingOrderService {
    private final ShippingOrderRepository shippingOrderRepository;
    private final ShippingProviderRepository shippingProviderRepository;
    private final Validator validator;

    public ShippingOrderService(ShippingOrderRepository shippingOrderRepository, ShippingProviderRepository shippingProviderRepository, Validator validator) {
        this.shippingOrderRepository = shippingOrderRepository;
        this.shippingProviderRepository = shippingProviderRepository;
        this.validator = validator;
    }

    public void createOrder(CreateShippingOrderDTO createShippingOrderDTO){
        var violations = validator.validate(createShippingOrderDTO);
        if (!violations.isEmpty()){
            throw new ConstraintViolationException(violations);
        }
        ShippingProvider shippingProvider = shippingProviderRepository.searchShippingProviderPerId(createShippingOrderDTO.getShippingProvider().getId());
        if (shippingProvider == null) {
            throw new IllegalArgumentException("ShippingProvider não encontrado para o id: " + createShippingOrderDTO.getShippingProvider().getId());
        }

        ShippingOrder order = ShippingOrder.builder()
                .shippingProvider(shippingProvider)
                .destinationState(createShippingOrderDTO.getDestinationState())
                .destinationCity(createShippingOrderDTO.getDestinationCity())
                .weight(createShippingOrderDTO.getWeight())
                .status(createShippingOrderDTO.getStatus())
                .estimatedDeliveryDays(createShippingOrderDTO.getEstimatedDeliveryDays())
                .deliveryDate(createShippingOrderDTO.getDeliveryDate())
                .shipmentDate(createShippingOrderDTO.getShipmentDate())
                .shippingCost(createShippingOrderDTO.getShippingCost())
                .build();

        shippingOrderRepository.saveShippingOrder(order);
    }

    public ShippingOrder searchShippingOrder(UUID orderId) {
        return shippingOrderRepository.searchShippingOrderPerId(orderId);
    }

    public List<ShippingOrder> listAllShippingOrders() {
        return shippingOrderRepository.listAllShippingOrders();
    }

    public ShippingOrder removeShippingOrder(UUID orderId) {
        return shippingOrderRepository.removeShippingOrderPerId(orderId);
    }
}
