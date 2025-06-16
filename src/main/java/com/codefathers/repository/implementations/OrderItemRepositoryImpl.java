package com.codefathers.repository.implementations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.codefathers.model.entity.OrderItem;
import com.codefathers.repository.interfaces.OrderItemRepository;
import com.codefathers.util.HibernateUtil;

public class OrderItemRepositoryImpl implements OrderItemRepository {

    @Override
    public void save(OrderItem orderItem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(orderItem);
            transaction.commit();
        }
    }

    @Override
    public void update(OrderItem orderItem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.merge(orderItem);
            transaction.commit();
        }
    }

    @Override
    public void delete(OrderItem orderItem) {

    }

    @Override
    public Optional<OrderItem> findById(UUID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            OrderItem orderItem = session.get(OrderItem.class, id);
            return Optional.ofNullable(orderItem);
        }
    }

    public Optional<List<OrderItem>> findByProductSku(String productSku) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM order_item oi WHERE oi.product.sku = :sku";
            List<OrderItem> orderItem = session.createQuery(hql, OrderItem.class)
                    .setParameter("sku", productSku).list();
            return Optional.ofNullable(orderItem);
        }
    }

    @Override
    public List<OrderItem> listAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<OrderItem> orderItemList = session.createQuery("select i from order_item i", OrderItem.class).list();
            return orderItemList;
        }
    }

}
