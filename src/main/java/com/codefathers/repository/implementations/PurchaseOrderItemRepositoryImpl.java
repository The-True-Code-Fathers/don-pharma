package com.codefathers.repository.implementations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.entity.Payment;
import com.codefathers.model.enums.PurchaseOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.codefathers.model.entity.OrderItem;
import com.codefathers.model.entity.PurchaseOrderItem;
import com.codefathers.repository.interfaces.PurchaseOrderItemRepository;
import com.codefathers.util.HibernateUtil;

@Slf4j
public class PurchaseOrderItemRepositoryImpl implements PurchaseOrderItemRepository {

    @Override
    public void save(PurchaseOrderItem purchaseOrderItem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(purchaseOrderItem);
            transaction.commit();
        }
    }

    @Override
    public void update(PurchaseOrderItem purchaseOrderItem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(purchaseOrderItem);
            transaction.commit();
        }
    }

    @Override
    public void delete(PurchaseOrderItem purchaseOrderItem) {

    }

    @Override
    public Optional<PurchaseOrderItem> findById(UUID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var purchaseOrderItem = session.get(PurchaseOrderItem.class, id);
            return Optional.ofNullable(purchaseOrderItem);
        }
    }

    @Override
    public List<PurchaseOrderItem> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "select i from purchase_order_item i";
            return session.createQuery(hql, PurchaseOrderItem.class).getResultList();
        }
    }

    public Optional<List<PurchaseOrderItem>> findByProductSku(String productSku) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM purchase_order_item poi WHERE poi.product.sku = :sku";
            List<PurchaseOrderItem> purchaseOrderItem = session.createQuery(hql, PurchaseOrderItem.class)
                    .setParameter("sku", productSku).list();
            return Optional.ofNullable(purchaseOrderItem);
        }
    }

    @Override
    public List<PurchaseOrderItem> listByTimePeriod(LocalDate from, LocalDate to) {

        log.debug("Ricardo eletro");

        try (var session =  HibernateUtil.sessionFactory.openSession()) {
            String hql = """
                SELECT
                    poi
                FROM
                    purchase_order_item poi
                JOIN
                    poi.purchaseOrder po
                WHERE
                    po.purchaseOrderStatus = :status
                    AND poi.createdAt between :from and :to
            """;

            // String hql = "select p from purchase_order_item join order o where op.status = :status and ;
            var q = session.createQuery(hql, PurchaseOrderItem.class)
                    .setParameter("from", from.atStartOfDay())
                    .setParameter("to", to.plusDays(2).atStartOfDay())
                    .setParameter("status", PurchaseOrderStatus.INVOICED);

            return q.getResultList();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return List.of();
        }
    }

}
