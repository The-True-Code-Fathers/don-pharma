package com.codefathers.repository.implementations;

import java.util.List;

import com.codefathers.repository.interfaces.ProductRepository;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

import com.codefathers.model.entity.Product;
import com.codefathers.util.HibernateUtil;
import org.springframework.stereotype.Repository;

@Repository
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
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            String hql = "select p from product p";
            return session.createQuery(hql, Product.class).list();
        } catch (Exception e) {
            e.getMessage();
            return List.of();
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
    public void update(Product product) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.merge(product);
            session.getTransaction().commit();
        } catch (ConstraintViolationException e) {
            System.out.println(e.getMessage());
        }
    }

}
