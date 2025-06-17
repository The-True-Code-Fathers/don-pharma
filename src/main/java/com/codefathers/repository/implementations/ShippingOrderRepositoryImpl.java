package com.codefathers.repository.implementations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.entity.Payment;
import com.codefathers.model.entity.PurchaseOrderItem;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.codefathers.model.entity.ShippingOrder;
import com.codefathers.repository.interfaces.ShippingOrderRepository;
import com.codefathers.util.HibernateUtil;

public class ShippingOrderRepositoryImpl implements ShippingOrderRepository {

    @Override
    public void save(ShippingOrder shippingOrder) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(shippingOrder);
            transaction.commit();
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    @Override
    public void update(ShippingOrder shippingOrder) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.merge(shippingOrder);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("Erro ao atualizar pedido: " + e.getMessage());
        }
    }

    @Override
    public void delete(UUID id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ShippingOrder shippingOrder = session.get(ShippingOrder.class, id);
            if (shippingOrder != null) {
                session.remove(shippingOrder);
                transaction.commit();
            } else {
                System.out.println("Pedido não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Não foi possivel remover o pedido da transportadora pelo ID: " + e.getMessage());
        }
    }

    @Override
    public Optional<ShippingOrder> findById(UUID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var shippingOrder = session.get(ShippingOrder.class, id);
            return Optional.ofNullable(shippingOrder);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ShippingOrder> listAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select o from shipping_order o", ShippingOrder.class).getResultList();
        }
    }

    @Override
    public List<ShippingOrder> listByTimePeriod(LocalDate from, LocalDate to) {
        try (var session =  HibernateUtil.sessionFactory.openSession()) {
            String hql = "select p from shipping_order p where createdAt between :from and :to";
            return session.createQuery(hql, ShippingOrder.class)
                    .setParameter("from", from.atStartOfDay())
                    .setParameter("to", to.plusDays(2).atStartOfDay()).getResultList();
        } catch (Exception e) {
            e.getMessage();
            return List.of();
        }
    }
}
