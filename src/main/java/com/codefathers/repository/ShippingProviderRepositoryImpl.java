package com.codefathers.repository;

import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.util.HibernateUtil;
import org.hibernate.Session;

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
        }
        return null;
    }

    @Override
    public List<ShippingProvider> listAllShippingProviders() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select p from ShippingProvider p", ShippingProvider.class).getResultList();
        }
    }

    @Override
    public ShippingProvider removeShippingProviderPerId(UUID shippingId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            ShippingProvider shippingProvider = session.get(ShippingProvider.class, shippingId);

            if (shippingProvider != null) {
                session.remove(shippingProvider);
                session.getTransaction().commit();
                return shippingProvider;
            } else {
                System.out.println("Transportadora não encontrada.");
            }
        } catch (Exception e) {
            System.out.println("Não foi possivel remover a transportadora pelo ID: " + e.getMessage());
        }
        return null;
    }
}
