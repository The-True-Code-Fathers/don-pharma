package com.codefathers.repository.interfaces;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.entity.PurchaseOrderItem;

public interface PurchaseOrderItemRepository {
    void save(PurchaseOrderItem purchaseOrderItem);

    void update(PurchaseOrderItem purchaseOrderItem);

    void delete(PurchaseOrderItem purchaseOrderItem);

    Optional<PurchaseOrderItem> findById(UUID purchaseOrderItemId);

    Optional<List<PurchaseOrderItem>> findByProductSku(String productSku);

    List<PurchaseOrderItem> findAll();

    List<PurchaseOrderItem> listByTimePeriod(LocalDate from, LocalDate to);
}
