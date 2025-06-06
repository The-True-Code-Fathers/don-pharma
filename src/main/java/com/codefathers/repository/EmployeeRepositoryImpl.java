package com.codefathers.repository;

import com.codefathers.model.entity.Employee;
import com.codefathers.util.HibernateUtil;
import org.hibernate.Session;
import java.util.List;
import java.util.UUID;


public class EmployeeRepositoryImpl implements EmployeeRepository {


    @Override
    public void saveEmployee(Employee employee) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(employee);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println("Erro ao salvar funcionário: " + e.getMessage());
        }
    }

    @Override
    public List<Employee> listAllEmployees() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select e from employee e", Employee.class).list();
        } catch (Exception e) {
            e.getMessage();
            return List.of();
        }
    }

    @Override
    public void deleteEmployeeByID(UUID id) {
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
    public Employee searchEmployeePerId(UUID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Employee.class, id);
        } catch (Exception e) {
            System.out.println("Erro ao buscar funcionário por ID: " + e.getMessage());
            return null;
        }
    }
}

