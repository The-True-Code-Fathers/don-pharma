package com.codefathers.repository;

import com.codefathers.model.entity.Employee;
import com.codefathers.model.enums.EmployeeGender;
import com.codefathers.model.enums.EmployeeRole;

import java.time.LocalDate;
import java.util.UUID;

public class EmployeeRepositoryImpl implements EmployeeRepository {

    @Override
    public Employee searchEmployeePerId(UUID id) {
        return new Employee("Nelson", LocalDate.now(), EmployeeGender.MALE, EmployeeRole.SALES);
    }
}

