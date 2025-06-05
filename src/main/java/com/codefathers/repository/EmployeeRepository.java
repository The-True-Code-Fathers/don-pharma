package com.codefathers.repository;

import com.codefathers.model.dto.CreateEmployeeDTO;
import com.codefathers.model.entity.Employee;

import java.util.List;
import java.util.UUID;

public interface EmployeeRepository {
    void saveEmployee(Employee employee);
    Employee searchEmployeePerId(UUID id);
    List<Employee> listAllEmployees();
    void deleteEmployeeByID(UUID id);
}
