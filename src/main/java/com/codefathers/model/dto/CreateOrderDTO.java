package com.codefathers.model.dto;

import com.codefathers.model.entity.Product;
import com.codefathers.model.entity.ShippingProvider;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    private LocalDateTime createdAt;


}
