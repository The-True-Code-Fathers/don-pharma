package com.codefathers.repository.implementations;

import com.codefathers.model.entity.SystemUser;
import com.codefathers.repository.interfaces.SystemUserRepository;
import com.codefathers.util.HibernateUtil;

import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SystemUserRepositoryImpl implements SystemUserRepository {
    @Override
    public Optional<SystemUser> findByUsername(String username) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            Query<SystemUser> query = session.createQuery(
                    "FROM SystemUser su WHERE su.username = :username", SystemUser.class);
            query.setParameter("username", username);
            SystemUser user = query.uniqueResult(); // Use uniqueResult() for a single expected result

            return Optional.ofNullable(user);
        } catch (Exception e) {
            System.err.println("Error finding user by username: " + username);
            return Optional.empty(); // Return empty optional on error
        }
    }

    @Override
    public void save(SystemUser user) {
        Transaction tx = null;
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
        }
    }
}
