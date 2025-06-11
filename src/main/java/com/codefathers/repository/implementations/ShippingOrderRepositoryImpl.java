package com.codefathers.repository.implementations;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.codefathers.model.entity.ShippingOrder;
import com.codefathers.repository.interfaces.ShippingOrderRepository;
import com.codefathers.util.HibernateUtil;

@ApplicationScoped
public class ShippingOrderRepositoryImpl implements ShippingOrderRepository {

    @Override
    public void saveShippingOrder(ShippingOrder shippingOrder) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(shippingOrder);
            transaction.commit();
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    @Override
    public ShippingOrder searchShippingOrderPerId(UUID orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(ShippingOrder.class, orderId);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ShippingOrder> listAllShippingOrders() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select o from shipping_order o", ShippingOrder.class).getResultList();
        }
    }

    @Override
    public ShippingOrder removeShippingOrderPerId(UUID orderId) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            ShippingOrder shippingOrder = session.get(ShippingOrder.class, orderId);

            if (shippingOrder != null) {
                session.remove(shippingOrder);
                session.getTransaction().commit();
                return shippingOrder;
            } else {
                System.out.println("Pedido não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Não foi possivel remover o pedido da transportadora pelo ID: " + e.getMessage());
        }
        return null;
    }
}
