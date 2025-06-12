package com.codefathers.repository;

import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.util.HibernateUtil;
import org.hibernate.Hibernate;
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
            throw new RuntimeException("Erro ao salvar transportadora", e);
        }
    }

    @Override
    public ShippingProvider searchShippingProviderPerId(UUID shippingId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            ShippingProvider provider = session.get(ShippingProvider.class, shippingId);
            if (provider != null) {
                Hibernate.initialize(provider.getShippingAreas());
            }
            return provider;
        } catch (Exception e) {
            System.out.println("Erro ao buscar a transportadora: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transportadora", e);
        }
    }

    @Override
    public List<ShippingProvider> listAllShippingProviders() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT p FROM shipping_provider p LEFT JOIN FETCH p.shippingAreas";
            List<ShippingProvider> providers = session.createQuery(hql).getResultList();
            return providers;
        } catch (Exception e) {
            System.out.println("Erro ao listar transportadoras: " + e.getMessage());
            throw new RuntimeException("Erro ao listar transportadoras", e);
        }
    }

    @Override
    public ShippingProvider removeShippingProviderPerId(UUID shippingId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            ShippingProvider shippingProvider = session.get(ShippingProvider.class, shippingId);

            if (shippingProvider != null) {
                Hibernate.initialize(shippingProvider.getShippingAreas());
                session.remove(shippingProvider);
                session.getTransaction().commit();
                return shippingProvider;
            } else {
                System.out.println("Transportadora não encontrada.");
                return null;
            }
        } catch (Exception e) {
            System.out.println("Não foi possível remover a transportadora pelo ID: " + e.getMessage());
            throw new RuntimeException("Erro ao remover transportadora", e);
        }
    }

    @Override
    public void updateShippingProvider(ShippingProvider shippingProvider) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            if (shippingProvider.getShippingAreas() != null) {
                shippingProvider.getShippingAreas().forEach(session::merge);
            }
            session.merge(shippingProvider);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("Erro ao atualizar a transportadora: " + e.getMessage());
            throw new RuntimeException("Erro ao atualizar transportadora", e);
        }
    }
}
