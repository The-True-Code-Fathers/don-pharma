package com.codefathers.service;

import com.codefathers.model.dto.PlanoVendedorDTO;
import com.codefathers.model.dto.ProdutoSugeridoDTO;



import java.math.BigDecimal;
import java.util.List;

public class teste {
    public static void main(String[] args) {
        // Definindo uma meta financeira
        BigDecimal metaFinanceira = new BigDecimal("60000.00");  // Meta de exemplo

        // Criando uma instância do serviço MetaPorVendedorService
        MetaService metaPorVendedorService = new MetaService();

        // Chamando o método calcularPlanoPorVendedor
        List<PlanoVendedorDTO> planos = metaPorVendedorService.calcularPlanoPorVendedor(metaFinanceira);

        // Exibir os resultados
        if (planos != null && !planos.isEmpty()) {
            for (PlanoVendedorDTO plano : planos) {
                System.out.println("Vendedor: " + plano.getVendedorNome());
                System.out.println("Total Vendido: " + plano.getTotalVendido());
                System.out.println("Meta Financeira: " + plano.getMeta());
                System.out.println("Status: " + plano.getStatus());

                if ("META NÃO ATINGIDA".equals(plano.getStatus())) {
                    System.out.println("Produtos Sugeridos:");

                    for (ProdutoSugeridoDTO produto : plano.getProdutosSugeridos()) {
                        System.out.println("SKU: " + produto.getSku() +
                                ", Nome: " + produto.getNome() +
                                ", Quantidade Sugerida: " + produto.getQuantidadeSugerida());
                    }
                }
                System.out.println("-----------------------------------------");
            }
        } else {
            System.out.println("Nenhum plano de vendedor encontrado.");
        }
    }
}
