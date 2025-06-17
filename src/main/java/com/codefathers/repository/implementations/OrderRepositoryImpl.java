package com.codefathers.repository.implementations;

import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Order;
import com.codefathers.model.entity.OrderItem;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.model.enums.OrderStatus;
import com.codefathers.repository.interfaces.OrderRepository;
import com.codefathers.util.HibernateUtil;
import com.codefathers.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
public class OrderRepositoryImpl implements OrderRepository {
    @Override
    public void save(Order order) {
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

    @Override
    public List<Order> findOrdersByTimePeriod(LocalDate start, LocalDate end) {
        String hql = "from orders o where o.createdAt between :startDate and :endDate";
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            var query = session.createQuery(hql, Order.class);
            query.setParameter("startDate", start.atStartOfDay());
            // + 1 for inclusive interval + 1 for start of next day (end of previous one)
            query.setParameter("endDate", end.plusDays(1 + 1).atStartOfDay());

            List<Order> queryList = query.getResultList();
            log.debug("Orders by time period: {}", JsonUtil.toPrettyJson(queryList));

            return query.getResultList();
        } catch (Exception e) {
            log.error("Error finding orders between {} and {}", start, end, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Order> findOrdersByStatus(OrderStatus status) {
        String hql = "from orders o where o.orderStatus = :status";
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, Order.class)
                    .setParameter("status", status)
                    .getResultList();
        } catch (Exception e) {
            log.error("Error finding orders with status {}", status, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Order> findOrdersByStatusAndTimePeriod(OrderStatus status, LocalDate start, LocalDate end) {
        String hql = "from orders o where o.orderStatus = :status and o.createdAt >= :startDate and o.createdAt < :endDate";
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            var query = session.createQuery(hql, Order.class);
            query.setParameter("status", status);
            query.setParameter("startDate", start.atStartOfDay());
            query.setParameter("endDate", end.plusDays(1).atStartOfDay());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error finding orders with status {} between {} and {}", status, start, end, e);
            return Collections.emptyList();
        }
    }
    public Map<Employee, Double> findSellersRankedByOrderStatus(LocalDate from, LocalDate to, OrderStatus status, int limit) {
        // HQL query to select seller and sum of their totalAmount
        String hql = "SELECT o.seller, SUM(o.totalAmount) FROM orders o " + // 'orders' is the @Entity name
                "WHERE o.orderStatus = :status AND o.seller.role = :role " +
                "AND o.createdAt BETWEEN :startDate AND :endDate " +
                "GROUP BY o.seller " +
                "ORDER BY SUM(o.totalAmount) DESC"; // Order by the total sales amount

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            Query<Object[]> query = session.createQuery(hql, Object[].class)
                    .setParameter("status", status)
                    .setParameter("role", EmployeeRole.SALES) // Assuming EmployeeRole.SALES exists
                    .setParameter("startDate", from.atStartOfDay())
                    .setParameter("endDate", to.plusDays(1).atStartOfDay()) // endDate is exclusive in BETWEEN, so add 1 day
                    .setMaxResults(limit);

            List<Object[]> results = query.getResultList();

            Map<Employee, Double> sellersRankedBySales = new LinkedHashMap<>();
            for (Object[] result : results) {
                Employee seller = (Employee) result[0];
                Double totalSales = ((BigDecimal) result[1]).doubleValue();
                sellersRankedBySales.put(seller, totalSales);
            }

            return sellersRankedBySales;

        } catch (Exception e) {
            log.error("Error finding sellers ranked by order status", e);
            return Collections.emptyMap(); // Return an empty map on error
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

    @Override
    public List<Order> findInvoicedOrders(LocalDate start, LocalDate end) {
        String hql = "from orders o " +
                "where o.orderStatus = :status " +
                "and o.createdAt >= :start and o.createdAt < :end";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            log.debug("Açguma coisa: {}", 1);
            return session.createQuery(hql, Order.class)
                    .setParameter("status", OrderStatus.INVOICED)
                    .setParameter("start", start.atStartOfDay())
                    .setParameter("end", end.plusDays(1).atStartOfDay())
                    .getResultList();
        }
    }

    public long countInvoicedOrders(LocalDate start, LocalDate end) {
        String hql = "select count(o) from orders o " +
                "where o.orderStatus = :status " +
                "and o.createdAt >= :start and o.createdAt < :end";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            long count = session.createQuery(hql, Long.class)
                    .setParameter("status", OrderStatus.INVOICED)
                    .setParameter("start", start.atStartOfDay())
                    .setParameter("end", end.plusDays(1).atStartOfDay())
                    .uniqueResult();

            log.debug("Pedidos faturados entre {} e {}: {}", start, end, count);

            return count;
        }
    }




}
