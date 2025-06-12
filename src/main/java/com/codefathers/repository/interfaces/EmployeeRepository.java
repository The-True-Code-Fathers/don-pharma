package com.codefathers.repository.interfaces;

import com.codefathers.model.entity.Employee;

import java.util.List;
import java.util.UUID;

public interface EmployeeRepository {
    void update(Employee employee);
    void saveEmployee(Employee employee);
    Employee searchEmployeePerId(UUID id);
    List<Employee> listAllEmployees();
    void deleteEmployeeByID(UUID id);
    long count();
}

