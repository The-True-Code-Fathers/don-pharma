package com.codefathers.repository;

import com.codefathers.model.entity.Order;
import com.codefathers.util.HibernateUtil;

import java.util.UUID;

public class OrderRepositoryImpl implements OrderRepository {
    @Override
    public void save(Order order) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.save(order);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Order findById(UUID id) {
        return new Order();
    }
}
