package com.codefathers.service;

import com.codefathers.model.dto.CreateShippingOrderDTO;
import com.codefathers.model.entity.ShippingOrder;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.ShippingOrderRepository;
import com.codefathers.repository.ShippingProviderRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import java.util.List;
import java.util.UUID;

public class ShippingOrderService {
    private final ShippingOrderRepository shippingOrderRepository;
    private final ShippingProviderRepository shippingProviderRepository;
    private final Validator validator;

    public ShippingOrderService(ShippingOrderRepository shippingOrderRepository, ShippingProviderRepository shippingProviderRepository, Validator validator) {
        this.shippingOrderRepository = shippingOrderRepository;
        this.shippingProviderRepository = shippingProviderRepository;
        this.validator = validator;
    }

    public void saveShippingOrder(ShippingOrder shippingOrder){
        shippingOrderRepository.saveShippingOrder(shippingOrder);
    }

    public void createOrder(CreateShippingOrderDTO dto){
        var violations = validator.validate(dto);
        if (!violations.isEmpty()){
            throw new ConstraintViolationException(violations);
        }
        ShippingProvider provider = shippingProviderRepository.searchShippingProviderPerId(dto.getShippingProviderId());
        if (provider == null) {
            throw new IllegalArgumentException("ShippingProvider não encontrado para o id: " + dto.getShippingProviderId());
        }

        ShippingOrder order = ShippingOrder.builder()
                .shippingProvider(provider)
                .destinationState(dto.getDestinationState())
                .destinationCity(dto.getDestinationCity())
                .weight(dto.getWeight())
                .status(dto.getStatus())
                .estimatedDeliveryDays(dto.getEstimatedDeliveryDays())
                .shipmentDate(dto.getShipmentDate())
                .deliveryDate(dto.getDeliveryDate())
                .shippingCost(dto.getShippingCost())
                .build();

        saveShippingOrder(order);
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

    public void updateOrder(UUID orderId, CreateShippingOrderDTO dto) {
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        ShippingOrder existingOrder = shippingOrderRepository.searchShippingOrderPerId(orderId);
        if (existingOrder == null) {
            throw new IllegalArgumentException("Pedido não encontrado para o id: " + orderId);
        }

        ShippingProvider provider = shippingProviderRepository.searchShippingProviderPerId(dto.getShippingProviderId());
        if (provider == null) {
            throw new IllegalArgumentException("ShippingProvider não encontrado para o id: " + dto.getShippingProviderId());
        }

        existingOrder.setShippingProvider(provider);
        existingOrder.setDestinationState(dto.getDestinationState());
        existingOrder.setDestinationCity(dto.getDestinationCity());
        existingOrder.setWeight(dto.getWeight());
        existingOrder.setStatus(dto.getStatus());
        existingOrder.setEstimatedDeliveryDays(dto.getEstimatedDeliveryDays());
        existingOrder.setShipmentDate(dto.getShipmentDate());
        existingOrder.setDeliveryDate(dto.getDeliveryDate());
        existingOrder.setShippingCost(dto.getShippingCost());

        shippingOrderRepository.updateShippingOrder(existingOrder);
    }

}
