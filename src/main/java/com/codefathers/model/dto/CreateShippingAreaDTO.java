package com.codefathers.model.dto;

import com.codefathers.model.entity.ShippingProvider;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateShippingAreaDTO {
    @NotNull
    private ShippingProvider shippingProvider;

    private String description;

    @NotNull
    private String[] states;

    @NotNull
    private String cep;
}
