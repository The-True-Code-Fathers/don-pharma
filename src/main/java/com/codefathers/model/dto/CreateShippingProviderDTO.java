package com.codefathers.model.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.codefathers.model.entity.ShippingArea;

import jakarta.persistence.Column;
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

    @NotNull
    private UUID id;

    @NotBlank
    private String cnpj;

    @NotBlank
    private String name;

    @NotNull
    private BigDecimal basePrice;

    @NotNull
    private BigDecimal dailyCapacity;

    @Column(name = "average_delivery_days", nullable = false)
    private int averageDeliveryDays;

    @NotNull(message = "Campo obrigatório")
    private boolean active;

}
