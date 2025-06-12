package com.codefathers.repository.interfaces;

import com.codefathers.model.entity.SystemUser;

import java.util.Optional;

public interface SystemUserRepository {
    void save(SystemUser user);
    Optional<SystemUser> findByUsername(String username);
}
