package com.codefathers;

import com.codefathers.model.entity.Employee;
import com.codefathers.model.enums.EmployeeGender;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.util.HibernateUtil;
import org.hibernate.Session;

import java.sql.Date;
import java.time.LocalDate;


public class Main {
    public static void main(String[] args) {
        Employee user = new Employee("Nelson Antunes", LocalDate.of(1989, 2, 26), EmployeeGender.FEMALE,
                EmployeeRole.LOCAL_MANAGER);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(user);
            session.getTransaction().commit();
        }

        HibernateUtil.shutdown();
    }
}