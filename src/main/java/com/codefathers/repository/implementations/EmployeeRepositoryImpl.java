package com.codefathers.repository.implementations;

import com.codefathers.model.entity.Employee;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.model.enums.OrderStatus;
import com.codefathers.repository.interfaces.EmployeeRepository;
import com.codefathers.util.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
public class EmployeeRepositoryImpl implements EmployeeRepository {

    @Override
    public void update(Employee employee) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.merge(employee);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void save(Employee employee) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(employee);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("Erro ao salvar funcionário: " + e.getMessage());
        }
    }

    @Override
    public List<Employee> listAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select e from employee e", Employee.class).list();
        } catch (Exception e) {
            e.getMessage();
            return List.of();
        }
    }

    @Override
    public void delete(UUID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Employee employee = session.get(Employee.class, id);

            if (employee != null) {
                session.remove(employee);
                session.getTransaction().commit();
            } else {
                System.out.println("Funcionário não encontrado.");
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("select count(*) from employee", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    @Override
    public List<Employee> findEmployeesRankedByOrderStatus(OrderStatus status, int limit) {
        String hql = "SELECT o.seller FROM orders o " +
                "WHERE o.orderStatus = :status AND o.seller.role = :role " +
                "GROUP BY o.seller " +
                "ORDER BY COUNT(o) DESC";

        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(hql, Employee.class)
                    .setParameter("status", status)
                    .setParameter("role", EmployeeRole.SALES)
                    .setMaxResults(limit)
                    .getResultList();
        } catch (Exception e) {
            log.error("Error finding employees ranked by order status and role", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Employee findById(UUID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Employee.class, id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar funcionário por ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public BigDecimal getTotalSalaries() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select sum(e.salary) from Employee e", BigDecimal.class)
                    .uniqueResultOptional()
                    .orElse(BigDecimal.ZERO);
        }
    }

}
