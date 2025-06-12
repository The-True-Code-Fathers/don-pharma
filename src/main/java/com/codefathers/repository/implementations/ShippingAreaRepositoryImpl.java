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
    public Optional<ShippingArea> findById(UUID areaId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var shippingArea = session.get(ShippingArea.class, areaId);
            return Optional.ofNullable(shippingArea);
        } catch (Exception e) {
            System.out.println("Erro ao buscar a área de transporte: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ShippingArea> listAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Consulta corrigida com JOIN FETCH para carregar todas as relações
            String hql = "SELECT sa FROM shipping_area sa LEFT JOIN FETCH sa.shippingProvider";
            var query = session.createQuery(hql, ShippingArea.class);

            List<ShippingArea> result = query.getResultList();
            System.out.println("Áreas encontradas: " + result.size());

            if (!result.isEmpty()) {
                System.out.println("Primeira área: " + result.get(0).getDescription());
                System.out.println("Transportadora: " +
                        (result.get(0).getShippingProvider() != null ?
                                result.get(0).getShippingProvider().getName() : "Nenhuma"));
            }

            return result;
        } catch (Exception e) {
            System.out.println("ERRO na consulta: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public void delete(UUID areaId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            ShippingArea shippingArea = session.get(ShippingArea.class, areaId);

            if (shippingArea != null) {
                session.remove(shippingArea);
                session.getTransaction().commit();
            } else {
                System.out.println("Não foi possivel remover a área da transportadora.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
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

}
