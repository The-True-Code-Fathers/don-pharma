package com.codefathers.repository;

import com.codefathers.model.entity.SystemUser;

import java.util.Optional;

public interface SystemUserRepository {
    Optional<SystemUser> findByUsername(String username);
    void save(SystemUser user);
}
