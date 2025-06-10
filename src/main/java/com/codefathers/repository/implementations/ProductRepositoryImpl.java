package com.codefathers.repository.implementations;

import java.util.List;

import com.codefathers.repository.interfaces.ProductRepository;
import org.hibernate.exception.ConstraintViolationException;

import com.codefathers.model.entity.Product;
import com.codefathers.util.HibernateUtil;

public class ProductRepositoryImpl implements ProductRepository {
    @Override
    public Product findBySKU(String sku) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from product where sku = :sku", Product.class).setParameter("sku", sku)
                    .uniqueResult();
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
            session.persist(product);
            session.getTransaction().commit();
        } catch (ConstraintViolationException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Product update(String sku) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from product where sku = :sku", Product.class).setParameter("sku", sku)
                    .uniqueResult();
        }
    }

}
