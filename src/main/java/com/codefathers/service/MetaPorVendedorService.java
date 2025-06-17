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
                String hqlTotalVendas = "SELECT SUM(o.productsPrice) FROM orders o WHERE o.seller.id = :sellerId AND o.orderStatus = :status";
                BigDecimal totalVendas = session.createQuery(hqlTotalVendas, BigDecimal.class)
                        .setParameter("sellerId", vendedor.getId())
                        .setParameter("status", OrderStatus.INVOICED)
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

                    // 3. Buscar estoque disponível
                    String hqlEstoque = "FROM storage s WHERE s.productQuantity > 0";
                    List<Storage> estoqueDisponivel = session.createQuery(hqlEstoque, Storage.class).list();

                    List<ProdutoSugeridoDTO> produtos = new ArrayList<>();
                    BigDecimal totalMetaRestante = restante.multiply(BigDecimal.valueOf(1.02));

                    for (Storage storage : estoqueDisponivel) {
                        Product produto = storage.getProduct();
                        AverageProductPriceUtil.calculateWeightedAverageSellPrice(produto);
                        BigDecimal precoUnitario = AverageProductPriceUtil.getWeightedAverage();

                        if (produto != null && precoUnitario.compareTo(BigDecimal.ZERO) > 0) {
                            // Limitar o máximo da meta por produto (ex: 30% da meta total)
                            BigDecimal maxParaEsteProduto = metaFinanceira.multiply(BigDecimal.valueOf(0.3));

                            BigDecimal metaFaltanteParaProduto = totalMetaRestante.min(maxParaEsteProduto);
                            int qtdSugerida = metaFaltanteParaProduto.divide(precoUnitario, RoundingMode.UP).intValue();
                            qtdSugerida = Math.min(qtdSugerida, storage.getProductQuantity());

                            if (qtdSugerida > 0) {
                                ProdutoSugeridoDTO dto = new ProdutoSugeridoDTO();
                                dto.setSku(produto.getSku());
                                dto.setNome(produto.getName());
                                dto.setQuantidadeSugerida(qtdSugerida);
                                produtos.add(dto);

                                totalMetaRestante = totalMetaRestante.subtract(precoUnitario.multiply(BigDecimal.valueOf(qtdSugerida)));

                                if (totalMetaRestante.compareTo(BigDecimal.ZERO) == 0) break;
                            }
                        }
                    }
                    plano.setProdutosSugeridos(produtos);
                }

                planos.add(plano);
            }

            tx.commit();
        }

        log.debug("{}", planos);

        return planos;
    }

//    public List<PlanoVendedorDTO> calcularPlanoPorVendedor(Employee vendedor, BigDecimal metaFinanceira) {
//        List<PlanoVendedorDTO> planos = new ArrayList<>();
//
//        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
//            Transaction tx = session.beginTransaction();
//
//            // 1. Buscar vendedores
//            String hqlVendedores = "FROM employee e WHERE e.role = 'SALES'";
//            List<Employee> vendedores = session.createQuery(hqlVendedores, Employee.class).list();
//
//            for (Employee vendedor : vendedores) {
//
//                // 2. Total de vendas do vendedor
//                String hqlTotalVendas = "SELECT SUM(o.totalAmount) FROM orders o WHERE o.seller.id = :sellerId AND o.orderStatus = :status";
//                BigDecimal totalVendas = session.createQuery(hqlTotalVendas, BigDecimal.class)
//                        .setParameter("sellerId", vendedor.getId())
//                        .setParameter("status", OrderStatus.INVOICED)
//                        .uniqueResult();
//                totalVendas = totalVendas != null ? totalVendas : BigDecimal.ZERO;
//
//                BigDecimal restante = metaFinanceira.subtract(totalVendas);
//
//                PlanoVendedorDTO plano = new PlanoVendedorDTO();
//                plano.setVendedorId(vendedor.getId());
//                plano.setVendedorNome(vendedor.getFullName());
//                plano.setTotalVendido(totalVendas);
//                plano.setMeta(metaFinanceira);
//
//                if (restante.compareTo(BigDecimal.ZERO) <= 0) {
//                    plano.setStatus("META ATINGIDA");
//                    plano.setProdutosSugeridos(Collections.emptyList());
//                } else {
//                    plano.setStatus("META NÃO ATINGIDA");
//
//                    // 3. Buscar estoque disponível
//                    String hqlEstoque = "FROM storage s WHERE s.productQuantity > 0";
//                    List<Storage> estoqueDisponivel = session.createQuery(hqlEstoque, Storage.class).list();
//
//                    List<ProdutoSugeridoDTO> produtos = new ArrayList<>();
//                    BigDecimal totalMetaRestante = restante;
//
//                    for (Storage storage : estoqueDisponivel) {
//                        Product produto = storage.getProduct();
//                        BigDecimal precoUnitario = BigDecimal.valueOf(25); // Aqui o ideal é buscar o preço real
//
//                        if (produto != null && precoUnitario.compareTo(BigDecimal.ZERO) > 0) {
//                            // Limitar o máximo da meta por produto (ex: 20% da meta total)
//                            BigDecimal maxParaEsteProduto = metaFinanceira.multiply(BigDecimal.valueOf(0.2));
//
//                            BigDecimal metaFaltanteParaProduto = totalMetaRestante.min(maxParaEsteProduto);
//
//                            int qtdSugerida = metaFaltanteParaProduto.divide(precoUnitario, RoundingMode.UP).intValue();
//                            qtdSugerida = Math.min(qtdSugerida, storage.getProductQuantity());
//
//                            if (qtdSugerida > 0) {
//                                ProdutoSugeridoDTO dto = new ProdutoSugeridoDTO();
//                                dto.setSku(produto.getSku());
//                                dto.setNome(produto.getName());
//                                dto.setQuantidadeSugerida(qtdSugerida);
//                                produtos.add(dto);
//
//                                totalMetaRestante = totalMetaRestante.subtract(precoUnitario.multiply(BigDecimal.valueOf(qtdSugerida)));
//
//                                if (totalMetaRestante.compareTo(BigDecimal.ZERO) <= 0) break;
//                            }
//                        }
//                    }
//                    plano.setProdutosSugeridos(produtos);
//                }
//
//                planos.add(plano);
//            }
//
//            tx.commit();
//        }
//
//        return planos;
//    }
}
