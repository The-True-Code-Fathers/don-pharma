package com.codefathers.model.dto;

import com.codefathers.model.enums.TipoMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetaDTO {

    private Long id;
    private String nome;
    private TipoMeta tipoMeta;
    private double valorObjetivo;
    private double prioridade;
    private int prazoDias;
}
