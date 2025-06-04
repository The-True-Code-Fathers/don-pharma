package com.codefathers.service;

import com.codefathers.model.dto.CreateEmployeeDTO;
import com.codefathers.repository.EmployeeRepository;

public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepostitory, EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public void createEmployee(CreateEmployeeDTO createEmployeeDTO) {

    }

}
