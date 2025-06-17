package com.codefathers.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.codefathers.model.dto.CreateShippingOrderDTO;
import com.codefathers.model.entity.Order;
import com.codefathers.model.entity.PurchaseOrder;
import com.codefathers.model.entity.ShippingOrder;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.interfaces.OrderRepository;
import com.codefathers.repository.interfaces.PurchaseOrderRepository;
import com.codefathers.repository.interfaces.ShippingOrderRepository;
import com.codefathers.repository.interfaces.ShippingProviderRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.Setter;

public class ShippingOrderService {
    private final ShippingOrderRepository shippingOrderRepository;
    private final ShippingProviderRepository shippingProviderRepository;
    private final OrderRepository orderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final Validator validator;

    public ShippingOrderService(ShippingOrderRepository shippingOrderRepository,
                                ShippingProviderRepository shippingProviderRepository,
                                OrderRepository orderRepository,
                                PurchaseOrderRepository purchaseOrderRepository,
                                Validator validator) {
        this.shippingOrderRepository = shippingOrderRepository;
        this.shippingProviderRepository = shippingProviderRepository;
        this.orderRepository = orderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.validator = validator;
    }

    public void createOrder(CreateShippingOrderDTO dto) {
        ShippingProvider provider = shippingProviderRepository.findById(dto.getShippingProviderId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "ShippingProvider não encontrado com ID: "
                                + dto.getShippingProviderId()));

        ShippingOrder newShippingOrder = new ShippingOrder();
        newShippingOrder.setShippingProvider(provider);
        newShippingOrder.setDestinationState(dto.getDestinationState());
        newShippingOrder.setDestinationCity(dto.getDestinationCity());
        newShippingOrder.setWeight(dto.getWeight());
        newShippingOrder.setStatus(dto.getStatus());
        newShippingOrder.setActive(true);
        newShippingOrder.setCreatedAt(LocalDateTime.now());
        newShippingOrder.setDeliveryDate(dto.getDeliveryDate());
        newShippingOrder.setShipmentDate(dto.getShipmentDate());
        newShippingOrder.setEstimatedDeliveryDays(dto.getEstimatedDeliveryDays());
        newShippingOrder.setShippingCost(dto.getShippingCost());

            if (dto.getOrder() != null && !dto.getOrder().isEmpty()) {
            List<Order> managedSellOrders = dto.getOrder().stream()
                    .map(orderId -> orderRepository.findById(orderId)
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Pedido de Venda não encontrado com ID: "
                                            + orderId)))
                    .collect(Collectors.toList());

            managedSellOrders.forEach(order -> order.setShippingOrder(newShippingOrder));
            newShippingOrder.setOrders(managedSellOrders);
        }

        if (dto.getPurchaseOrder() != null && !dto.getPurchaseOrder().isEmpty()) {
            List<PurchaseOrder> managedPurchaseOrders = dto.getPurchaseOrder().stream()
                    .map(purchaseOrderId -> purchaseOrderRepository.findById(purchaseOrderId)
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Pedido de Compra não encontrado com ID: "
                                            + purchaseOrderId)))
                    .collect(Collectors.toList());

            managedPurchaseOrders
                    .forEach(purchaseOrder -> purchaseOrder.setShippingOrder(newShippingOrder));
            newShippingOrder.setPurchaseOrder(managedPurchaseOrders);
        }

        shippingOrderRepository.save(newShippingOrder);
    }

    public ShippingOrder searchShippingOrder(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        }
        return shippingOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Pedido não encontrado para o ID: " + orderId));
    }

    // Lista apenas as ordens ativas
    public List<ShippingOrder> listActiveShippingOrders() {
        return shippingOrderRepository.listAll().stream()
                .filter(ShippingOrder::isActive)
                .collect(Collectors.toList());
    }

    // Lista todas as ordens, ativas e inativas
    public List<ShippingOrder> listAllShippingOrdersIncludingInactive() {
        return shippingOrderRepository.listAll();
    }

    public void removeShippingOrder(UUID orderId) {
        ShippingOrder order = searchShippingOrder(orderId);

        CreateShippingOrderDTO dto = CreateShippingOrderDTO.builder()
                .shippingProviderId(order.getShippingProvider().getId())
                .destinationState(order.getDestinationState())
                .destinationCity(order.getDestinationCity())
                .weight(order.getWeight())
                .status(order.getStatus())
                .estimatedDeliveryDays(order.getEstimatedDeliveryDays())
                .shipmentDate(order.getShipmentDate())
                .deliveryDate(order.getDeliveryDate())
                .shippingCost(order.getShippingCost())
                .order(order.getOrders().stream().map(Order::getId).collect(Collectors.toList()))
                .purchaseOrder(order.getPurchaseOrder().stream().map(PurchaseOrder::getId)
                        .collect(Collectors.toList()))
                .build();

        updateOrder(orderId, dto, false);
    }

    public void updateOrder(UUID orderId, CreateShippingOrderDTO dto) {
        ShippingOrder existingOrder = searchShippingOrder(orderId);
        updateOrder(orderId, dto, existingOrder.isActive());
    }

    public void updateOrder(UUID id, CreateShippingOrderDTO dto, boolean active) {
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        List<Order> managedOrders = new ArrayList<>();
        List<PurchaseOrder> managedPurchaseOrders = new ArrayList<>();
        ShippingOrder existingOrder = searchShippingOrder(id);
        ShippingProvider provider = shippingProviderRepository.findById(dto.getShippingProviderId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Provedor de entrega não encontrado para o ID: "
                                + dto.getShippingProviderId()));

        if (dto.getOrder() != null && !dto.getOrder().isEmpty()) {
            managedOrders = dto.getOrder().stream()
                    .map(orderId -> orderRepository.findById(orderId)
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Pedido de Venda não encontrado com ID: "
                                            + orderId)))
                    .collect(Collectors.toList());

            managedOrders.forEach(order -> order.setShippingOrder(existingOrder));
            existingOrder.setOrders(managedOrders);
        }

        // Busca os Pedidos de Compra (PurchaseOrder) usando stream e findById
        if (dto.getPurchaseOrder() != null && !dto.getPurchaseOrder().isEmpty()) {
            managedPurchaseOrders = dto.getPurchaseOrder().stream()
                    .map(purchaseOrderId -> purchaseOrderRepository.findById(purchaseOrderId)
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Pedido de Compra não encontrado com ID: "
                                            + purchaseOrderId)))
                    .collect(Collectors.toList());

            managedPurchaseOrders.forEach(purchaseOrder -> purchaseOrder.setShippingOrder(existingOrder));
            existingOrder.setPurchaseOrder(managedPurchaseOrders);
        }

        existingOrder.setShippingProvider(provider);
        existingOrder.setOrders(managedOrders);
        existingOrder.setPurchaseOrder(managedPurchaseOrders);
        existingOrder.setDestinationState(dto.getDestinationState());
        existingOrder.setDestinationCity(dto.getDestinationCity());
        existingOrder.setWeight(dto.getWeight());
        existingOrder.setStatus(dto.getStatus());
        existingOrder.setEstimatedDeliveryDays(dto.getEstimatedDeliveryDays());
        existingOrder.setShipmentDate(dto.getShipmentDate());
        existingOrder.setDeliveryDate(dto.getDeliveryDate());
        existingOrder.setShippingCost(dto.getShippingCost());
        existingOrder.setActive(active); // Define o status de ativo

        shippingOrderRepository.update(existingOrder);
    }

    public void update(ShippingOrder shippingOrder) {
        shippingOrderRepository.update(shippingOrder);
    }

    public List<ShippingOrder> listAll() {
        return shippingOrderRepository.listAll();
    }

}
