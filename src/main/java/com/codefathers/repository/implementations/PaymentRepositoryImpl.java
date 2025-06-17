package com.codefathers.repository.implementations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.repository.interfaces.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Payment;
import com.codefathers.util.HibernateUtil;

@Slf4j
public class PaymentRepositoryImpl implements PaymentRepository {

    @Override
    public void save(Payment payment) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(payment);
            transaction.commit();
        }
    }

    @Override
    public void update(Payment payment) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.merge(payment);
            transaction.commit();
        }
    }

    @Override
    public void delete(UUID id) {

    }

    @Override
    public Optional<Payment> findById(UUID id) {
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            Payment payment = session.get(Payment.class, id);
            return Optional.ofNullable(payment);
        }
    }

    @Override
    public List<Payment> findByEmployee(Employee employee) {
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            String hql = "select p from payment p where p.employee = :employee";
            return session.createQuery(hql, Payment.class)
                    .setParameter("employee", employee)
                    .list();
        }
    }

    @Override
    public List<Payment> listAll() {
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            String hql = "select p from payment p";
            return session.createQuery(hql, Payment.class).list();
        } catch (Exception e) {
            e.getMessage();
            return List.of();
        }
    }

    @Override
    public List<Payment> listByTimePeriod(LocalDate from, LocalDate to) {
        try (var session =  HibernateUtil.sessionFactory.openSession()) {
            String hql = "select p from payment p where createdAt between :from and :to";
            return session.createQuery(hql, Payment.class)
                    .setParameter("from", from.atStartOfDay())
                    .setParameter("to", to.plusDays(2).atStartOfDay()).getResultList();
        } catch (Exception e) {
            e.getMessage();
            return List.of();
        }
    }

}
