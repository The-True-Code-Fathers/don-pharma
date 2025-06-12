package com.codefathers.repository.interfaces;

import com.codefathers.model.entity.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository {
    void save(Employee employee);
    void update(Employee employee);
    void delete(UUID id);
    Optional<Employee> findById(UUID id);
    List<Employee> listAll();
}
