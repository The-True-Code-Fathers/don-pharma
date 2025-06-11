package com.codefathers.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.codefathers.exceptions.BusinessRuleException;
import com.codefathers.model.dto.CreatePurchaseOrderDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.PurchaseOrder;
import com.codefathers.model.entity.PurchaseOrderItem;
import com.codefathers.model.entity.Storage;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.repository.interfaces.EmployeeRepository;
import com.codefathers.repository.interfaces.PurchaseOrderRepository;
import com.codefathers.repository.interfaces.StorageRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final EmployeeRepository employeeRepository;
    private final StorageRepository storageRepository;
    private final Validator validator;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
            EmployeeRepository employeeRepository, StorageRepository storageRepository,
            Validator validator) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.employeeRepository = employeeRepository;
        this.storageRepository = storageRepository;
        this.validator = validator;
    }

    public void createPurchaseOrder(@Valid CreatePurchaseOrderDTO createPurchaseOrderDTO)
            throws ConstraintViolationException {
        var violations = validator.validate(createPurchaseOrderDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        Employee employee = employeeRepository.searchEmployeePerId(createPurchaseOrderDTO.getPurchaserId());
        boolean isPurchaser = employee.getRole().equals(EmployeeRole.STORAGE);
        boolean isManager = employee.getRole().equals(EmployeeRole.LOCAL_MANAGER);

        if (!(isPurchaser || isManager)) {
            throw new BusinessRuleException("Provided employee is not allowed to create order");
        }

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .purchaserId(employee)
                .purchaseProductsPrice(BigDecimal.ZERO)
                .purchaseTotalAmount(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        List<PurchaseOrderItem> purchaseOrderItems = createPurchaseOrderDTO.getItem().stream().map(dto -> {
            PurchaseOrderItem purchaseOrderItem = new PurchaseOrderItem();
            purchaseOrderItem.setPurchaseOrder(purchaseOrder);
            purchaseOrderItem.setQuantity(dto.getQuantity());
            purchaseOrderItem.setPrice(dto.getPrice());
            purchaseOrderItem.setProduct(dto.getProduct());
            return purchaseOrderItem;
        }).toList();

        for (PurchaseOrderItem item : purchaseOrderItems) {
            String productSku = item.getProduct().getSku();

            storageRepository.findByProductSku(productSku).ifPresentOrElse(existingStorage -> {
                existingStorage.setProductQuantity(existingStorage.getProductQuantity() + item.getQuantity());
                storageRepository.update(existingStorage);
            }, () -> {
                Storage newStorage = Storage.builder()
                .product(item.getProduct())
                .productQuantity(item.getQuantity())
                .build();
                storageRepository.save(newStorage);
            });
        }

        BigDecimal purchaseProductsPrice = purchaseOrderItems.stream().map(PurchaseOrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal purchaseTotalAmount = purchaseProductsPrice;

        purchaseOrder.setPurchaseItems(purchaseOrderItems);
        purchaseOrder.setPurchaseProductsPrice(purchaseProductsPrice);
        purchaseOrder.setPurchaseTotalAmount(purchaseTotalAmount);

        purchaseOrderRepository.save(purchaseOrder);
    }

    public PurchaseOrder findPurchaseOrderById(UUID purchaseOrderId) {
        return purchaseOrderRepository.findById(purchaseOrderId);
    }

}