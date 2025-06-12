package com.codefathers.model.dto;

import com.codefathers.model.enums.UmSelect;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductDTO {
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Deve conter pelo menos uma letra"
    )
    private String sku;

    @NotNull
    @Size(
            max = 50,
            message = "Máximo de 50 caracteres"
    )
    private String name;

    private String description;

    @Positive (message = "O valor deve ser positivo")
    private BigDecimal buyPrice;

    @Positive (message = "O valor deve ser positivo")
    private BigDecimal sellPrice;

    private boolean active = true;

    @NotNull(message = "Campo obrigatório")
    private UmSelect umSelect;
}