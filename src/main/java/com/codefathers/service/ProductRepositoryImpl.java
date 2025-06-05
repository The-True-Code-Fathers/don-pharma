package com.codefathers.service;

import com.codefathers.model.entity.Product;
import com.codefathers.util.HibernateUtil;
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;

public class ProductRepositoryImpl implements ProductRepository {
    @Override
    public Product findBySKU(String sku) {
        return null;
    }

    @Override
    public void save(Product product) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.save(product);
            session.getTransaction().commit();
        } catch (ConstraintViolationException e) {
            System.out.println(e.getMessage());
        }
    }


}
