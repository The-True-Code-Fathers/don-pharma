package com.codefathers.repository.implementations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.entity.Payment;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.codefathers.model.entity.OrderItem;
import com.codefathers.model.entity.PurchaseOrderItem;
import com.codefathers.repository.interfaces.PurchaseOrderItemRepository;
import com.codefathers.util.HibernateUtil;

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
        try (var session =  HibernateUtil.sessionFactory.openSession()) {
            String hql = "select p from purchase_order p where createdAt between :from and :to";
            return session.createQuery(hql, PurchaseOrderItem.class)
                    .setParameter("from", from.atStartOfDay())
                    .setParameter("to", to.plusDays(2).atStartOfDay()).getResultList();
        } catch (Exception e) {
            e.getMessage();
            return List.of();
        }
    }

}
