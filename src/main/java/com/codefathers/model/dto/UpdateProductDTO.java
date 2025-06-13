package com.codefathers.model.dto;

import com.codefathers.model.enums.MeasurementUnit;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UpdateProductDTO {

    @Size(max = 2048)
    private String description;

    @NotNull(message = "Campo obrigatório")
    @Positive(message = "O valor deve ser maior que zero")
    private BigDecimal buyPrice;

    @NotNull(message = "Campo obrigatório")
    @Positive(message = "O valor deve ser maior que zero")
    private BigDecimal sellPrice;

    @NotNull(message = "Campo obrigatório")
    private boolean active;

    @NotNull(message = "Campo obrigatório")
    private MeasurementUnit measurementUnit;
}
