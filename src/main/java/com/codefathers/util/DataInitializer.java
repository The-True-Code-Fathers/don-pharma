package com.codefathers.util;

import com.codefathers.model.entity.SystemUser;
import com.codefathers.repository.interfaces.SystemUserRepository;
import com.codefathers.repository.implementations.SystemUserRepositoryImpl;

public class DataInitializer {

    public static void initialize() {
        SystemUserRepository userRepository = new SystemUserRepositoryImpl();

        // Use the repository to check and create users
        if (userRepository.findByUsername("admin").isEmpty()) {
            SystemUser adminUser = new SystemUser();
            adminUser.setUsername("admin");
            adminUser.setPasswordHash(AuthUtil.PasswordUtil.hashPassword("adminpass")); // HASH THE PASSWORD
            adminUser.setRole("ADMIN");
            userRepository.save(adminUser);
            System.out.println("Created initial admin user.");
        }

        if (userRepository.findByUsername("user").isEmpty()) {
            SystemUser regularUser = new SystemUser();
            regularUser.setUsername("user");
            regularUser.setPasswordHash(AuthUtil.PasswordUtil.hashPassword("password")); // HASH THE PASSWORD
            regularUser.setRole("USER");
            userRepository.save(regularUser);
            System.out.println("Created initial regular user.");
        }
    }
}
