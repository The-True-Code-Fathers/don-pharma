package com.codefathers.repository;

import com.codefathers.model.entity.Product;
import com.codefathers.util.HibernateUtil;
import org.hibernate.exception.ConstraintViolationException;


import java.util.List;

public class ProductRepositoryImpl implements ProductRepository {
    @Override
    public Product findBySKU(String sku) {
        try( var session = HibernateUtil.getSessionFactory().openSession() ) {
            return session.createQuery("from product where sku = :sku", Product.class).setParameter("sku", sku).uniqueResult();
        }
    }

    @Override
    public List<Product> listAllProducts() {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from product", Product.class).list();
        }
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
