package com.codefathers.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlocacaoDTO {
    private List<AlocacaoDTO> alocacoes = new ArrayList<>();
    private double custoTotal;
    private double eficienciaGlobal;
    private String status;
    private List<String> observacoes = new ArrayList<>();
}
