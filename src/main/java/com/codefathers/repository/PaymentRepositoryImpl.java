package com.codefathers.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Payment;
import com.codefathers.util.HibernateUtil;

public class PaymentRepositoryImpl implements PaymentRepository {

    @Override
    public Optional<Payment> findById(UUID id) {
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            Payment payment = session.get(Payment.class, id);
            return Optional.ofNullable(payment);
        }
    }

    @Override
    public List<Payment> findPaymentsByEmployee(Employee employee) {
    try (Session session = HibernateUtil.sessionFactory.openSession()) {
        String hql = "select p from payment p where p.employee = :employee";
        return session.createQuery(hql, Payment.class)
                .setParameter("employee", employee)
                .list();
    }
}

    @Override
    public List<Payment> getAllPayments() {
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            String hql = "select p from payment p";
            return session.createQuery(hql, Payment.class).list();
        } catch (Exception e) {
            e.getMessage();
            return List.of();
        }
    }

    @Override
    public void save(Payment payment) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            payment.setEmployee(session.merge(payment.getEmployee()));

            session.persist(payment);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            throw e;
        }
    }

}
