package com.codefathers.repository.implementations;

import com.codefathers.model.entity.Employee;
import com.codefathers.repository.interfaces.EmployeeRepository;
import com.codefathers.util.HibernateUtil;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmployeeRepositoryImpl implements EmployeeRepository {

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
    public void update(Employee employee) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.merge(employee);
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
    public Optional<Employee> findById(UUID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var employee = session.get(Employee.class, id);
            return Optional.ofNullable(employee);
        } catch (Exception e) {
            System.out.println("Erro ao buscar funcionário por ID: " + e.getMessage());
            return null;
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

}
