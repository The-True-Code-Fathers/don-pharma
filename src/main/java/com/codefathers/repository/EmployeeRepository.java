package com.codefathers.repository;

import com.codefathers.model.entity.Employee;

import java.util.UUID;

public interface EmployeeRepository {
    Employee findById(UUID id);
}
