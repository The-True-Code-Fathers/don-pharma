package com.codefathers.service;
import com.codefathers.model.dto.MetaDTO;
import com.codefathers.model.enums.TipoMeta;

import java.util.List;


public class MetaService {
    public List<MetaDTO> obterMetasAtivas() {
        // Simular consulta ao banco
        return List.of(
                new MetaDTO(1L, "Meta A", TipoMeta.FINANCEIRA, 1000, 5, 30),
                new MetaDTO(2L, "Meta B", TipoMeta.QUALIDADE, 500, 3, 20)
        );
    }

    public List<MetaDTO> obterMetasPorTipo(TipoMeta tipo) {
        // Simular consulta filtrada
        return obterMetasAtivas().stream()
                .filter(meta -> meta.getTipoMeta() == tipo)
                .toList();
    }
}