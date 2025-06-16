package com.codefathers.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProdutoSugeridoDTO {

    private String sku;
    private String nome;
    private int quantidadeSugerida;

}
