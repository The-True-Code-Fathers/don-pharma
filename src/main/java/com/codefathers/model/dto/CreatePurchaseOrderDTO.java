package com.codefathers.model.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePurchaseOrderDTO {

    private UUID purchaserId;

    @Size(max = 2048)
    private String description;

    @NotEmpty
    private List<@Valid CreatePurchaseOrderItemDTO> item;

    private BigDecimal purchaseTotalPriceAmount;

}
