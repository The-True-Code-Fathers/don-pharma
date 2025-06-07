package com.codefathers.service;

import com.codefathers.exceptions.BusinessRuleException;
import com.codefathers.model.dto.CreatePurchaseOrderDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.PurchaseOrder;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.repository.EmployeeRepository;
import com.codefathers.repository.PurchaseOrderRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

import java.util.UUID;

public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final EmployeeRepository employeeRepository;
    private final Validator validator;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                        EmployeeRepository employeeRepository,
                        Validator validator) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.employeeRepository = employeeRepository;
        this.validator = validator;
    }

    public void createPurchaseOrder(@Valid CreatePurchaseOrderDTO createPurchaseOrderDTO) throws ConstraintViolationException {
        var violations = validator.validate(createPurchaseOrderDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        Employee emp = employeeRepository.searchEmployeePerId(createPurchaseOrderDTO.getPurchaserId());
        boolean isPurchaser = emp.getRole().equals(EmployeeRole.STORAGE);
        boolean isManager = emp.getRole().equals(EmployeeRole.LOCAL_MANAGER);

        if (!(isPurchaser|| isManager)) {
            throw new BusinessRuleException("Provided employee is not allowed to create order");
        }

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .purchaserId(emp)
                .build();

        purchaseOrderRepository.save(purchaseOrder);
    }

    public PurchaseOrder findPurchaseOrderById(UUID id) {
        return purchaseOrderRepository.findById(id);
    }

    public void cancelPurchaseOrder(UUID id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id);
        purchaseOrder.setCancelled(true);
        purchaseOrderRepository.update(purchaseOrder);
    }
}
