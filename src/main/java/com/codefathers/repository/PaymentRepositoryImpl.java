package com.codefathers.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Payment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PaymentRepositoryImpl implements PaymentRepository {

    private final SessionFactory sessionFactory;

    public PaymentRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        try (Session session = sessionFactory.openSession()) {
            Payment payment = session.get(Payment.class, id);
            return Optional.ofNullable(payment);
        }
    }

    @Override
    public Optional<Payment> findPaymentByEmployee(Employee employee) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Payment p WHERE p.employee = :employee";
            Payment payment = session.createQuery(hql, Payment.class)
                    .setParameter("employee", employee)
                    .uniqueResult();
            return Optional.ofNullable(payment);
        }
    }

    @Override
    public List<Payment> getAllPayments() {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Payment";
            return session.createQuery(hql, Payment.class).list();
        }
    }

    @Override
public void save(Payment payment) {
    Transaction transaction = null;
    try (Session session = sessionFactory.openSession()) {
        transaction = session.beginTransaction();

        payment.setEmployee(session.merge(payment.getEmployee()));

        session.persist(payment);
        transaction.commit();
    } catch (Exception e) {
        if (transaction != null) transaction.rollback();
        throw e;
    }
}

}
