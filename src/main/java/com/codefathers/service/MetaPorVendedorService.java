package com.codefathers.service;

import com.codefathers.model.dto.PlanoVendedorDTO;
import com.codefathers.model.dto.ProdutoSugeridoDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Product;
import com.codefathers.model.entity.Storage;
import com.codefathers.model.enums.OrderStatus;
import com.codefathers.util.AverageProductPriceUtil;
import com.codefathers.util.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class MetaPorVendedorService {

    public List<PlanoVendedorDTO> calcularPlanoPorVendedor(BigDecimal metaFinanceira) {
        List<PlanoVendedorDTO> planos = new ArrayList<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            // 1. Buscar vendedores
            String hqlVendedores = "FROM employee e WHERE e.role = 'SALES'";
            List<Employee> vendedores = session.createQuery(hqlVendedores, Employee.class).list();

            for (Employee vendedor : vendedores) {

                // 2. Total de vendas do vendedor
                String hqlTotalVendas = """
                    SELECT SUM(o.productsPrice) 
                    FROM orders o 
                    WHERE o.seller.id = :sellerId AND o.orderStatus = :status AND o.createdAt BETWEEN :startDate AND :endDate
                """;
                BigDecimal totalVendas = session.createQuery(hqlTotalVendas, BigDecimal.class)
                        .setParameter("sellerId", vendedor.getId())
                        .setParameter("status", OrderStatus.INVOICED)
                        .setParameter("startDate", LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay())
                        .setParameter("endDate", LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).atStartOfDay())
                        .uniqueResult();

                totalVendas = totalVendas != null ? totalVendas : BigDecimal.ZERO;

                BigDecimal restante = metaFinanceira.subtract(totalVendas);

                PlanoVendedorDTO plano = new PlanoVendedorDTO();
                plano.setVendedorId(vendedor.getId());
                plano.setVendedorNome(vendedor.getFullName());
                plano.setTotalVendido(totalVendas);
                plano.setMeta(metaFinanceira);

                if (restante.compareTo(BigDecimal.ZERO) <= 0) {
                    plano.setStatus("META ATINGIDA");
                    plano.setProdutosSugeridos(Collections.emptyList());
                } else {
                    plano.setStatus("META NÃO ATINGIDA");

                    // 3. Implementação da Programação Linear - Minimização de Itens
                    List<ProdutoSugeridoDTO> produtos = resolverProgramacaoLinear(session, restante);
                    plano.setProdutosSugeridos(produtos);
                }

                planos.add(plano);
            }

            tx.commit();
        }

//        log.debug("{}", planos);
        return planos;
    }

    /**
     * Resolve o problema de programação linear para minimizar a quantidade de itens
     * Utiliza algoritmo guloso baseado na razão valor/quantidade (knapsack greedy approach)
     */
    private List<ProdutoSugeridoDTO> resolverProgramacaoLinear(Session session, BigDecimal metaRestante) {
        // 1. Buscar todos os produtos disponíveis no estoque
        String hqlEstoque = "FROM storage s WHERE s.productQuantity > 0";
        List<Storage> estoqueDisponivel = session.createQuery(hqlEstoque, Storage.class).list();

        // 2. Criar lista de candidatos com eficiência (valor unitário)
        List<MetaService.CandidatoProduto> candidatos = new ArrayList<>();

        for (Storage storage : estoqueDisponivel) {
            Product produto = storage.getProduct();
            if (produto != null) {
                AverageProductPriceUtil.calculateWeightedAverageSellPrice(produto);
                BigDecimal precoUnitario = AverageProductPriceUtil.getWeightedAverage();

                if (precoUnitario.compareTo(BigDecimal.ZERO) > 0) {
                    MetaService.CandidatoProduto candidato = new MetaService.CandidatoProduto();
                    candidato.produto = produto;
                    candidato.precoUnitario = precoUnitario;
                    candidato.quantidadeDisponivel = storage.getProductQuantity();
                    candidato.eficiencia = precoUnitario; // Maior valor unitário = maior eficiência
                    candidatos.add(candidato);
                }
            }
        }

        // 3. Ordenar por eficiência decrescente (produtos mais valiosos primeiro)
        candidatos.sort((a, b) -> b.eficiencia.compareTo(a.eficiencia));

        // 4. Algoritmo guloso para minimizar quantidade de itens
        List<ProdutoSugeridoDTO> resultado = new ArrayList<>();
        BigDecimal valorRestante = metaRestante;

        for (MetaService.CandidatoProduto candidato : candidatos) {
            if (valorRestante.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            // Calcular quantidade ótima para este produto
            // Calcular quantidade ótima para este produto
            int quantidadeNecessaria = valorRestante.divide(candidato.precoUnitario, RoundingMode.UP).intValue();

            // Aplicar restrição de máximo 20% do estoque disponível
            int quantidadeMaximaPermitida = (int) Math.floor(candidato.quantidadeDisponivel * 0.2);

            // Garantir que pelo menos 1 item possa ser sugerido se houver estoque suficiente
            if (quantidadeMaximaPermitida == 0 && candidato.quantidadeDisponivel > 0) {
                quantidadeMaximaPermitida = 1;
            }

            int quantidadeUsada = Math.min(quantidadeNecessaria, quantidadeMaximaPermitida);

            if (quantidadeUsada > 0) {
                ProdutoSugeridoDTO dto = new ProdutoSugeridoDTO();
                dto.setSku(candidato.produto.getSku());
                dto.setNome(candidato.produto.getName());
                dto.setQuantidadeSugerida(quantidadeUsada);
                resultado.add(dto);

                // Atualizar valor restante
                BigDecimal valorConsumido = candidato.precoUnitario.multiply(BigDecimal.valueOf(quantidadeUsada));
                valorRestante = valorRestante.subtract(valorConsumido);
            }
        }

        return resultado;
    }

    /**
     * Classe auxiliar para representar um candidato na programação linear
     */
    private static class CandidatoProduto {
        Product produto;
        BigDecimal precoUnitario;
        int quantidadeDisponivel;
        BigDecimal eficiencia; // Critério de otimização (valor unitário)
    }

}
