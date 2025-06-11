package com.codefathers.model.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateShippingProviderDTO {
    @NotBlank
    private String cnpj;

    @NotBlank
    private String name;

    @NotNull
    private BigDecimal basePrice;

    @NotNull
    private BigDecimal dailyCapacity;

}
