package com.codefathers.repository;

import com.codefathers.model.entity.ShippingOrder;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;
import java.util.UUID;

public class ShippingOrderRepositoryImpl implements ShippingOrderRepository{

    @Override
    public void saveShippingOrder(ShippingOrder shippingOrder) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.save(shippingOrder);
            session.getTransaction().commit();
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

    @Override
    public void updateShippingOrder(ShippingOrder shippingOrder) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.update(shippingOrder);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("Erro ao atualizar pedido: " + e.getMessage());
        }
    }
}
