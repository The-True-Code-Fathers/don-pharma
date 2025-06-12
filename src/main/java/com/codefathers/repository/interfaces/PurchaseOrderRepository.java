package com.codefathers.repository.interfaces;

import com.codefathers.model.entity.PurchaseOrder;
import com.codefathers.model.entity.PurchaseOrderItem;

import java.util.List;
import java.util.UUID;

public interface PurchaseOrderRepository extends GenericRepository<PurchaseOrder, UUID>{
        void save(PurchaseOrder purchaseOrder);
        void update(PurchaseOrder purchaseOrder);
        PurchaseOrder findById(UUID id);
        List<PurchaseOrder> findAll();
        List<PurchaseOrderItem> findAllPurchaseOrderItemByPurchaseOrderId(UUID purchaseOrderId);
    }

