package com.codefathers.repository.implementations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.enums.OrderStatus;
import com.codefathers.repository.dto.MostSoldProductDTO;
import com.codefathers.repository.interfaces.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

import com.codefathers.model.entity.Product;
import com.codefathers.util.HibernateUtil;

@Slf4j
public class ProductRepositoryImpl implements ProductRepository {

    @Override
    public void save(Product product) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(product);
            session.getTransaction().commit();
        } catch (ConstraintViolationException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void update(Product product) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.merge(product);
            session.getTransaction().commit();
        } catch (ConstraintViolationException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void delete(UUID id) {

    }

    @Override
    public Optional<Product> findBySKU(String sku) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            var productFound = session.createQuery("from product where sku = :sku", Product.class)
                    .setParameter("sku", sku)
                    .uniqueResult();
            return Optional.of(productFound);
        }
    }

    @Override
    public List<Product> listAll() {
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            String hql = "select p from product p";
            return session.createQuery(hql, Product.class).list();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<MostSoldProductDTO> findMostSoldProducts(LocalDate from, LocalDate to, int limit) {

        String hql = """
            SELECT
                new com.codefathers.repository.dto.MostSoldProductDTO(p.sku, p.name, SUM(oi.quantity))
            FROM
                order_item oi
            JOIN
                oi.product p
            WHERE
                oi.order.orderStatus IN (:statuses)
                AND oi.order.createdAt BETWEEN :from and :to
            GROUP BY
                p.sku, p.name
            ORDER BY
                SUM(oi.quantity) DESC
        """;

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, MostSoldProductDTO.class)
                    .setParameter("statuses", List.of(OrderStatus.INVOICED))
                    .setParameter("from", from.atStartOfDay())
                    .setParameter("to", to.plusDays(1).atStartOfDay())
                    .setMaxResults(limit)
                    .getResultList();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public BigDecimal getTotalBuyPrice() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select sum(p.buyPrice) from Product p", BigDecimal.class)
                    .uniqueResultOptional()
                    .orElse(BigDecimal.ZERO);
        }
    }


}
