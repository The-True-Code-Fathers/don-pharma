package com.codefathers.model.dto;

import com.codefathers.model.entity.Product;
import com.codefathers.model.entity.ShippingProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderDTO {

    // ID will be generated
    @NotNull
    private UUID sellerId;

    @Size(max = 2048)
    private String description;

    @NotEmpty
    private List<@Valid CreateOrderItemDTO> item;

    // Products price will be computed
    @NotNull
    private ShippingProvider shippingProvider;

    // Shipping price will be computed
    // Total amount will be computer
    // Invoiced is false by default
    // Cancelled is false by default

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CreateOrderItemDTO {
        @NotNull
        private Product product;

        @Min(1)
        private int quantity;
        // Price will be computed
    }
}
