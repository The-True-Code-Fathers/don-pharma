package com.codefathers.repository;

import com.codefathers.model.entity.SystemUser;
import com.codefathers.util.HibernateUtil;

import org.hibernate.Transaction;

import java.util.Optional;

public class SystemUserRepositoryImpl implements SystemUserRepository {
    @Override
    public Optional<SystemUser> findByUsername(String username) {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            SystemUser user = session.get(SystemUser.class, username);
            return Optional.ofNullable(user);
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
