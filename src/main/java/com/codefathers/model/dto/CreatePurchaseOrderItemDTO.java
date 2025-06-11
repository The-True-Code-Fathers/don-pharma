package com.codefathers.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.codefathers.model.entity.Product;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePurchaseOrderItemDTO {
    @NotNull
    private Product product;

    @NotNull
    private UUID purchaseOrderId;

    @NotNull
    @Min(1)
    private int quantity;

    @Column(name = "")
    private BigDecimal price;

}