package com.codefathers.model.dto;

import com.codefathers.model.entity.ShippingArea;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @NotNull
    private List<ShippingArea> shippingAreas = new ArrayList<>();
}
