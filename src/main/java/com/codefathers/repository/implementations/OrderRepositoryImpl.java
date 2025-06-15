package com.codefathers.repository.implementations;

import com.codefathers.model.entity.Order;
import com.codefathers.model.entity.OrderItem;
import com.codefathers.repository.interfaces.OrderRepository;
import com.codefathers.util.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class OrderRepositoryImpl implements OrderRepository {
    @Override
    public void save(Order order) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(order);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void update(Order order) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(order);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void delete(Order order) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(order);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Order> findById(UUID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var order = session.get(Order.class, id);
            return Optional.ofNullable(order);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Order> listAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select o from orders o", Order.class).list();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<OrderItem> findAllOrderItemsByOrderId(UUID orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select oi from order_item oi " +
                                    "join fetch oi.orders o " +
                                    "join fetch oi.product p " +
                                    "where o.id = :ordersId",
                            OrderItem.class)
                    .setParameter("ordersId", orderId)
                    .getResultList();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return List.of();
        }
    }

    public List<Order> findOrdersByTimePeriod(LocalDate start, LocalDate end) {
        String hql = "from orders o where o.createdAt between :startDate and :endDate";
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            var query = session.createQuery(hql, Order.class);
            query.setParameter("startDate", start.atStartOfDay());
            // + 1 for inclusive interval + 1 for start of next day (end of previous one)
            query.setParameter("endDate", end.plusDays(1 + 1).atStartOfDay());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error finding orders between {} and {}", start, end, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select count(*) from orders", Long.class).uniqueResult();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    public BigDecimal getTotalRevenue() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select sum(revenue) from orders", BigDecimal.class)
                    .uniqueResult();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
}
