package com.codefathers.repository.implementations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.Session;

import com.codefathers.model.entity.PurchaseOrderItem;
import com.codefathers.repository.interfaces.PurchaseOrderItemRepository;
import com.codefathers.util.HibernateUtil;

public class PurchaseOrderItemRepositoryImpl implements PurchaseOrderItemRepository {

    @Override
    public void delete(PurchaseOrderItem purchaseOrderItem) {
    
    }

    @Override
    public void save(PurchaseOrderItem purchaseOrderItem) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(purchaseOrderItem);
            session.beginTransaction().commit();
        }
    }

    @Override
    public void update(PurchaseOrderItem purchaseOrderItem) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.merge(purchaseOrderItem);
            session.beginTransaction().commit();
        }
    }

    @Override
    public Optional<PurchaseOrderItem> findById(UUID id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            var purchaseOrderItem = session.get(PurchaseOrderItem.class, id);
            return Optional.of(purchaseOrderItem);
        }
    }

    @Override
    public List<PurchaseOrderItem> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "select i from purchase_order_item i";
            return session.createQuery(hql, PurchaseOrderItem.class).getResultList();
        }
    }

}
