package com.codefathers.repository;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.codefathers.model.entity.Storage;
import com.codefathers.util.HibernateUtil;

public class StorageRepositoryImpl implements StorageRepository {

    @Override
    public void save(Storage storage) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(storage);
            transaction.commit();
        }
    }

    @Override
    public void update(Storage storage) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.merge(storage);
            transaction.commit();
        }
    }

    @Override
    public Optional<Storage> findByProductSku(String productSku) {
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            String hql = "select s from storage s where s.product.sku = :productId";
            Storage storage = session.createQuery(hql, Storage.class)
                    .setParameter("productId", productSku)
                    .uniqueResult();
            return Optional.ofNullable(storage);
        }
    }

    @Override
    public List<Storage> getAllStorages() {
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            String hql = "from storage";
            return session.createQuery(hql, Storage.class).list();
        } catch (Exception e) {
            e.getMessage();
            return List.of();
        }
    }

}
