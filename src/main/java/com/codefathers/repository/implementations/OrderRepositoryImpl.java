package com.codefathers.repository.implementations;

import com.codefathers.model.dto.EmployeeSalesDataDTO;
import com.codefathers.model.dto.EmployeeSalesValueDataDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Order;
import com.codefathers.model.entity.OrderItem;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.repository.interfaces.OrderRepository;
import com.codefathers.util.HibernateUtil;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.codefathers.util.HibernateUtil.sessionFactory;

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

    @Override
    public List<EmployeeSalesDataDTO> buscarVendasPorVendedor() {
        Session session = null;
        Transaction transaction = null;
        List<EmployeeSalesDataDTO> resultado = new ArrayList<>();

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            String hql = """
            SELECT e.fullName, COUNT(o.id)
            FROM employee e 
            LEFT JOIN orders o  ON o.seller.id = e.id
            WHERE e.role = :role AND e.active = :active
            GROUP BY e.id, e.fullName 
            ORDER BY COUNT(o.id) DESC
            """;

            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("role", EmployeeRole.SALES);
            query.setParameter("active", true);

            List<Object[]> resultList = query.getResultList();

            for (Object[] row : resultList) {
                String employeeName = (String) row[0];
                Long salesCount = (Long) row[1];
                resultado.add(new EmployeeSalesDataDTO(employeeName, salesCount));
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
//            resultado = criarDadosExemplo();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return resultado;
    }

    // Método para buscar apenas vendedores com vendas
    @Override
    public List<EmployeeSalesDataDTO> buscarVendedoresComVendas() {
        Session session = null;
        Transaction transaction = null;
        List<EmployeeSalesDataDTO> resultado = new ArrayList<>();

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            String hql = """
            SELECT e.fullName, COUNT(o.id)
            FROM employee e 
            INNER JOIN orders o 
            WHERE e.role = :role AND e.active = :active
            GROUP BY e.id, e.fullName 
            ORDER BY COUNT(o.id) DESC
            """;

            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("role", EmployeeRole.SALES);
            query.setParameter("active", true);
            query.setMaxResults(10); // Top 10 vendedores

            List<Object[]> resultList = query.getResultList();

            for (Object[] row : resultList) {
                String employeeName = (String) row[0];
                Long salesCount = (Long) row[1];
                resultado.add(new EmployeeSalesDataDTO(employeeName, salesCount));
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
//            resultado = criarDadosExemplo();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return resultado;
    }

    // Método para buscar vendas por período
    @Override
    public List<EmployeeSalesDataDTO> buscarVendasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        Session session = null;
        Transaction transaction = null;
        List<EmployeeSalesDataDTO> resultado = new ArrayList<>();

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            String hql = """
            SELECT e.fullName, COUNT(o.id)
            FROM employee e 
            LEFT JOIN orders o 
            WHERE e.role = :role AND e.active = :active
            AND (o.createdAt IS NULL OR o.createdAt BETWEEN :inicio AND :fim)
            GROUP BY e.id, e.fullName 
            ORDER BY COUNT(o.id) DESC
            """;

            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("role", EmployeeRole.SALES);
            query.setParameter("active", true);
            query.setParameter("inicio", inicio);
            query.setParameter("fim", fim);

            List<Object[]> resultList = query.getResultList();

            for (Object[] row : resultList) {
                String employeeName = (String) row[0];
                Long salesCount = (Long) row[1];
                resultado.add(new EmployeeSalesDataDTO(employeeName, salesCount));
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
//            resultado = criarDadosExemplo();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return resultado;
    }

    // Método para buscar vendas com valor total
    @Override
    public List<EmployeeSalesValueDataDTO> buscarVendasComValorTotal() {
        Session session = null;
        Transaction transaction = null;
        List<EmployeeSalesValueDataDTO> resultado = new ArrayList<>();

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            String hql = """
            SELECT e.fullName, COUNT(o.id), COALESCE(SUM(o.totalAmount), 0)
            FROM employee e 
            LEFT JOIN orders o 
            WHERE e.role = :role AND e.active = :active
            GROUP BY e.id, e.fullName 
            ORDER BY SUM(o.totalAmount) DESC
            """;

            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("role", EmployeeRole.SALES);
            query.setParameter("active", true);

            List<Object[]> resultList = query.getResultList();

            for (Object[] row : resultList) {
                String employeeName = (String) row[0];
                Long salesCount = (Long) row[1];
                BigDecimal totalValue = (BigDecimal) row[2];
                resultado.add(new EmployeeSalesValueDataDTO(employeeName, salesCount, totalValue));
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return resultado;
    }

    // Método para buscar vendas usando Criteria API
    @Override
    public List<EmployeeSalesDataDTO> buscarVendasComCriteria() {
        Session session = null;
        Transaction transaction = null;
        List<EmployeeSalesDataDTO> resultado = new ArrayList<>();

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

            Root<Employee> employee = cq.from(Employee.class);
            Join<Employee, Order> orders = employee.join("orders", JoinType.LEFT);

            cq.multiselect(
                    employee.get("fullName"),
                    cb.count(orders.get("id"))
            );

            cq.where(
                    cb.and(
                            cb.equal(employee.get("role"), EmployeeRole.SALES),
                            cb.equal(employee.get("active"), true)
                    )
            );

            cq.groupBy(employee.get("id"), employee.get("fullName"));
            cq.orderBy(cb.desc(cb.count(orders.get("id"))));

            List<Object[]> resultList = session.createQuery(cq).getResultList();

            for (Object[] row : resultList) {
                String employeeName = (String) row[0];
                Long salesCount = (Long) row[1];
                resultado.add(new EmployeeSalesDataDTO(employeeName, salesCount));
            }

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
//            resultado = criarDadosExemplo();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return resultado;
    }

    // Método para buscar um funcionário específico por ID
    @Override
    public Employee buscarFuncionarioPorId(UUID employeeId) {
        Session session = null;
        Transaction transaction = null;
        Employee employee = null;

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            employee = session.get(Employee.class, employeeId);

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return employee;
    }


}
