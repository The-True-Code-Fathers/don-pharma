package com.codefathers.repository.implementations;

import com.codefathers.model.entity.ShippingArea;
import com.codefathers.repository.interfaces.ShippingAreaRepository;
import com.codefathers.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ShippingAreaRepositoryImpl implements ShippingAreaRepository {

    @Override
    public void save(ShippingArea shippingArea) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(shippingArea);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("Erro:" + e.getMessage());
        }
    }

    @Override
    public void update(ShippingArea shippingArea) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.merge(shippingArea); // merge realiza atualização se o objeto já existe
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("Erro ao atualizar ShippingArea: " + e.getMessage());
        }
    }

    @Override
    public void delete(UUID id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            ShippingArea shippingArea = session.get(ShippingArea.class, id);
            if (shippingArea != null) {
                transaction = session.beginTransaction();
                session.remove(shippingArea);
                transaction.commit();
            } else {
                System.out.println("Não foi possivel remover a área da transportadora.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    @Override
    public Optional<ShippingArea> findById(UUID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var shippingArea = session.get(ShippingArea.class, id);
            return Optional.ofNullable(shippingArea);
        } catch (Exception e) {
            System.out.println("Erro ao buscar a área de transporte: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ShippingArea> listAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM shipping_area", ShippingArea.class).getResultList();
        }
    }

}
