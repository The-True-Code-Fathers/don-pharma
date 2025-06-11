package com.codefathers.repository.implementations;

import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.interfaces.ShippingProviderRepository;
import com.codefathers.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.awt.event.HierarchyBoundsAdapter;
import java.util.List;
import java.util.UUID;

public class ShippingProviderRepositoryImpl implements ShippingProviderRepository {
    @Override
    public void saveShippingProvider(ShippingProvider shippingProvider) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(shippingProvider);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("Erro ao salvar a transportadora: " + e.getMessage());
        }
    }

    @Override
    public ShippingProvider searchShippingProviderPerId(UUID shippingId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(ShippingProvider.class, shippingId);
        } catch (Exception e) {
           System.out.println("Erro ao buscar a transportadora: " + e.getMessage());
           return null;
        }
    }

    @Override
    public List<ShippingProvider> listAllShippingProviders() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select p from shipping_provider p", ShippingProvider.class).getResultList();
        }
    }

    @Override
    public ShippingProvider removeShippingProviderPerId(UUID shippingId) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            ShippingProvider shippingProvider = session.get(ShippingProvider.class, shippingId);
            if (shippingProvider != null) {
                session.remove(shippingProvider);
                transaction.commit();
                System.out.println("Transportadora removida com sucesso!");
                return shippingProvider;
            } else {
                transaction.rollback();
                System.out.println("Transportadora não encontrada.");
                return null;
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            } else {
                System.out.println("Não foi possivel remover a transportadora pelo ID: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }
}
