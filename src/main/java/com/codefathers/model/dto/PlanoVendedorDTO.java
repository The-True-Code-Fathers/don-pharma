package com.codefathers.model.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanoVendedorDTO {

    private UUID vendedorId;
    private String vendedorNome;
    private BigDecimal totalVendido;
    @Positive
    private BigDecimal meta;
    private String status;
    private List<ProdutoSugeridoDTO> produtosSugeridos;
    private LocalDateTime createdAt;

}
