package com.codefathers.model.dto;

import lombok.Data;

@Data
public class KpiDto {
    private String vendasHoje;
    private String pedidosAtivos;
    private String produtosEmEstoque;
    private String faturamentoMensal;
}
