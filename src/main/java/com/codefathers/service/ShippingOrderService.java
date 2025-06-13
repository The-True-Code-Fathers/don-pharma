package com.codefathers.service;

import java.time.LocalDateTime;
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

    public ShippingOrderService(ShippingOrderRepository shippingOrderRepository,
            ShippingProviderRepository shippingProviderRepository,
            Validator validator) {
        this.shippingOrderRepository = shippingOrderRepository;
        this.shippingProviderRepository = shippingProviderRepository;
        this.validator = validator;
    }

    public void createOrder(CreateShippingOrderDTO dto) {
        // Validação do DTO
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        // Busca o provedor de entrega
        ShippingProvider provider = shippingProviderRepository
                .findById(dto.getShippingProviderId()).get();

        if (provider == null) {
            throw new IllegalArgumentException(
                    "Provedor de entrega não encontrado para o id: " + dto.getShippingProviderId());
        }

        // Cria o novo pedido
        ShippingOrder order = ShippingOrder.builder()
                .shippingProvider(provider)
                .destinationState(dto.getDestinationState())
                .destinationCity(dto.getDestinationCity())
                .weight(dto.getWeight())
                .status(dto.getStatus())
                .estimatedDeliveryDays(dto.getEstimatedDeliveryDays())
                .deliveryDate(dto.getDeliveryDate())
                .shipmentDate(dto.getShipmentDate())
                .shippingCost(dto.getShippingCost())
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        shippingOrderRepository.save(order);
    }

    public ShippingOrder searchShippingOrder(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        }
        return shippingOrderRepository.findById(orderId).get();
    }

    public List<ShippingOrder> listAllShippingOrders() {
        return shippingOrderRepository.listAll();
    }

    public void removeShippingOrder(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        }

        var removedOrder = shippingOrderRepository.findById(orderId);
        if (removedOrder.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado para o ID: " + orderId);
        }
    }

    public void updateOrder(UUID orderId, CreateShippingOrderDTO dto) {
        // Validação do DTO
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        var existingOrder = shippingOrderRepository.findById(orderId).get();
        if (existingOrder == null) {
            throw new IllegalArgumentException("Pedido não encontrado para o id: " + orderId);
        }

        ShippingProvider provider = shippingProviderRepository
                .findById(dto.getShippingProviderId()).get();

        if (provider == null) {
            throw new IllegalArgumentException(
                    "Provedor de entrega não encontrado para o id: " + dto.getShippingProviderId());
        }

        // Atualiza os campos do pedido
        existingOrder.setShippingProvider(provider);
        existingOrder.setDestinationState(dto.getDestinationState());
        existingOrder.setDestinationCity(dto.getDestinationCity());
        existingOrder.setWeight(dto.getWeight());
        existingOrder.setStatus(dto.getStatus());
        existingOrder.setEstimatedDeliveryDays(dto.getEstimatedDeliveryDays());
        existingOrder.setShipmentDate(dto.getShipmentDate());
        existingOrder.setDeliveryDate(dto.getDeliveryDate());
        existingOrder.setShippingCost(dto.getShippingCost());

        shippingOrderRepository.update(existingOrder);
    }

    public void update(ShippingOrder shippingOrder) {
        shippingOrderRepository.update(shippingOrder);
    }

    public void updateOrder(UUID id, CreateShippingOrderDTO dto, boolean active) throws Exception {
        // Busca o pedido pelo ID
        ShippingOrder existingOrder = shippingOrderRepository.findById(id)
                .orElseThrow(() -> new Exception("Pedido não encontrado com ID: " + id));

        // Atualiza os campos da entidade com os valores do DTO
        ShippingProvider provider = shippingProviderRepository.findById(dto.getShippingProviderId())
                .orElseThrow(
                        () -> new Exception("Provedor de frete não encontrado com ID: " + dto.getShippingProviderId()));

        existingOrder.setShippingProvider(provider);
        existingOrder.setDestinationState(dto.getDestinationState());
        existingOrder.setDestinationCity(dto.getDestinationCity());
        existingOrder.setWeight(dto.getWeight());
        existingOrder.setStatus(dto.getStatus());
        existingOrder.setEstimatedDeliveryDays(dto.getEstimatedDeliveryDays());
        existingOrder.setShipmentDate(dto.getShipmentDate());
        existingOrder.setDeliveryDate(dto.getDeliveryDate());
        existingOrder.setShippingCost(dto.getShippingCost());
        existingOrder.setActive(active);

        shippingOrderRepository.update(existingOrder);
    }

}