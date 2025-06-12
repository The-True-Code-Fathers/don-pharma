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
            // Use o nome da CLASSE Java (não da tabela)
            var query = session.createQuery("FROM ShippingArea", ShippingArea.class);
            List<ShippingArea> result = query.getResultList();
            System.out.println("Áreas encontradas: " + result.size()); // Log de debug
            return result;
        } catch (Exception e) {
            System.out.println("ERRO na consulta: " + e.getMessage());
            return List.of();
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

    @Override
    public void updateShippingArea(ShippingArea shippingArea) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.merge(shippingArea); // merge realiza atualização se o objeto já existe
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("Erro ao atualizar ShippingArea: " + e.getMessage());
        }
    }
}
