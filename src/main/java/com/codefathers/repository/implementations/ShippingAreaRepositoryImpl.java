package com.codefathers.repository.implementations;

import com.codefathers.model.entity.ShippingArea;
import com.codefathers.repository.interfaces.ShippingAreaRepository;
import com.codefathers.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;
import java.util.UUID;

public class ShippingAreaRepositoryImpl implements ShippingAreaRepository {
    @Override
    public void saveShippingArea(ShippingArea shippingArea) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(shippingArea);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("Erro:" + e.getMessage());
        }
    }

    @Override
    public ShippingArea searchShippingAreaPerID(UUID areaId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(ShippingArea.class, areaId);
        } catch (Exception e) {
            System.out.println("Erro ao buscar a área de transporte: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ShippingArea> listAllShippingAreas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select a from shipping_area a", ShippingArea.class).getResultList();
        }
    }

    @Override
    public ShippingArea removeShippingAreaPerId(UUID areaId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            ShippingArea shippingArea = session.get(ShippingArea.class, areaId);

            if (shippingArea != null) {
                session.remove(shippingArea);
                session.getTransaction().commit();
                return shippingArea;
            } else {
                System.out.println("Não foi possivel remover a área da transportadora.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
        return null;
    }
}
