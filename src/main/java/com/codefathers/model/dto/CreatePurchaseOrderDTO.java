package com.codefathers.model.dto;

import com.codefathers.model.entity.Product;

import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePurchaseOrderDTO {

    // ID will be generated
    @NotNull
    private UUID purchaserId;

    @Size(max = 2048)
    private String description;

    @NotEmpty
    private List<@Valid CreatePurchaseOrderItemDTO> item;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CreatePurchaseOrderItemDTO {
        @NotNull
        private Product product;

        @Min(1)
        private int quantity;

        @Column(name = "")
        private BigDecimal price;

    }
}
