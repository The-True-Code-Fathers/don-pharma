package com.codefathers.service;

import com.codefathers.model.dto.RecursoDTO;
import com.codefathers.model.enums.TipoRecurso;

import java.util.List;


public class RecursoService {
    public List<RecursoDTO> obterTodosRecursos() {
        // Simular consulta ao banco
        return List.of(
                new RecursoDTO(1L, "Verba Marketing", TipoRecurso.FINANCEIRO, 5000, 10),
                new RecursoDTO(2L, "Horas de Engenheiro", TipoRecurso.HUMANO, 300, 50),
                new RecursoDTO(3L, "Matéria Prima", TipoRecurso.MATERIAL, 1000, 5)
        );
    }

    public List<RecursoDTO> obterRecursosPorTipo(TipoRecurso tipo) {
        return obterTodosRecursos().stream()
                .filter(recurso -> recurso.getTipoRecurso() == tipo)
                .toList();
    }
}

