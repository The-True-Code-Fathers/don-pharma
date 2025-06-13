package com.codefathers.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.codefathers.model.dto.CreatePurchaseOrderItemDTO;
import com.codefathers.model.entity.PurchaseOrderItem;
import com.codefathers.repository.interfaces.PurchaseOrderItemRepository;
import com.codefathers.repository.interfaces.PurchaseOrderRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

public class PurchaseOrderItemService {

    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final Validator validator;

    public PurchaseOrderItemService(PurchaseOrderItemRepository purchaseOrderItemRepository, PurchaseOrderRepository purchaseOrderRepository, Validator validator) {
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.validator = validator;
    }

    public void createPurchaseOrderItem(CreatePurchaseOrderItemDTO createPurchaseOrderItemDTO) {
        validateDTO(createPurchaseOrderItemDTO);

        PurchaseOrderItem item = PurchaseOrderItem.builder()
                .product(createPurchaseOrderItemDTO.getProduct())
                .quantity(createPurchaseOrderItemDTO.getQuantity())
                .price(createPurchaseOrderItemDTO.getPrice())
                .createdAt(LocalDateTime.now())
                .build();

        purchaseOrderItemRepository.save(item);
    }

    public List<PurchaseOrderItem> listAllItems() {
        return purchaseOrderItemRepository.findAll();
    }

    public Optional<PurchaseOrderItem> findById(UUID id) {
        Optional<PurchaseOrderItem> purchaseOrderItem = purchaseOrderItemRepository.findById(id);
        return purchaseOrderItem;
    }

    public void updatePurchaseOrderItem(PurchaseOrderItem item) {
        purchaseOrderItemRepository.update(item);
    }

    public void deletePurchaseOrderItem(PurchaseOrderItem item) {
        purchaseOrderItemRepository.delete(item);
    }

    private void validateDTO(CreatePurchaseOrderItemDTO createPurchaseOrderItemDTO) {
        Set<ConstraintViolation<CreatePurchaseOrderItemDTO>> violations = validator.validate(createPurchaseOrderItemDTO);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Erros de validação: " + errors);
        }
    }
}
