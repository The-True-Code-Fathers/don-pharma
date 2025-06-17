package com.codefathers.model.dto;

import com.codefathers.model.enums.TipoRecurso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecursoDTO {

    private Long id;
    private String nome;
    private TipoRecurso tipoRecurso;
    private double quantidadeDisponivel;
    private double custoUnitario;

}
