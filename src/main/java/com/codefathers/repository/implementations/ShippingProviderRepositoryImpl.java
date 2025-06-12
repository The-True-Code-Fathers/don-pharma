package com.codefathers.repository.implementations;

import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.interfaces.ShippingProviderRepository;
import com.codefathers.util.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ShippingProviderRepositoryImpl implements ShippingProviderRepository {

    @Override
    public void save(ShippingProvider shippingProvider) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            if (shippingProvider.getId() == null) {
                session.persist(shippingProvider); // Novo registro
            } else {
                session.merge(shippingProvider); // Atualização
            }

            session.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar transportadora", e);
        }
    }

    @Override
    public Optional<ShippingProvider> findById(UUID shippingId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            ShippingProvider provider = session.get(ShippingProvider.class, shippingId);
            if (provider != null) {
                Hibernate.initialize(provider.getShippingAreas());
            }
            return Optional.ofNullable(provider);
        } catch (Exception e) {
            System.out.println("Erro ao buscar a transportadora: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transportadora", e);
        }
    }

    @Override
    public List<ShippingProvider> listAll() {
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
    public void delete(UUID shippingId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            ShippingProvider shippingProvider = session.get(ShippingProvider.class, shippingId);

            if (shippingProvider != null) {
                Hibernate.initialize(shippingProvider.getShippingAreas());
                session.remove(shippingProvider);
                session.getTransaction().commit();
            } else {
                System.out.println("Transportadora não encontrada.");
            }
        } catch (Exception e) {
            System.out.println("Não foi possível remover a transportadora pelo ID: " + e.getMessage());
            throw new RuntimeException("Erro ao remover transportadora", e);
        }
    }

    @Override
    public void update(ShippingProvider shippingProvider) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            // Atualiza o provider e suas áreas
            ShippingProvider merged = session.merge(shippingProvider);

            // Garante que as áreas estão sincronizadas
            if (shippingProvider.getShippingAreas() != null) {
                shippingProvider.getShippingAreas().forEach(area -> {
                    area.setShippingProvider(merged);
                    session.merge(area);
                });
            }

            session.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar transportadora", e);
        }
    }
}
