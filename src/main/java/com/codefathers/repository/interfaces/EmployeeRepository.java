package com.codefathers.repository.interfaces;

import com.codefathers.model.entity.Employee;

import java.util.List;
import java.util.UUID;

public interface EmployeeRepository {
    void update(Employee employee);
    void save(Employee employee);
    Employee findById(UUID id);
    List<Employee> listAll();
    void delete(UUID id);
    long count();
}

