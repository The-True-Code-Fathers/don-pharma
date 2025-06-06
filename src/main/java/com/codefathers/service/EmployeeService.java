package com.codefathers.service;

import com.codefathers.model.dto.CreateEmployeeDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.repository.EmployeeRepository;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public void createEmployee(CreateEmployeeDTO dto) {
        Employee employee = Employee.builder()
                .gender(dto.getGender())
                .fullName(dto.getFullName())
                .role(dto.getRole())
                .birthDate(dto.getBirthDate())
                .build();

        employeeRepository.saveEmployee(employee);
    }

    public List<Employee> employeeList(){
        return employeeRepository.listAllEmployees();
    }

    public void findEmployeeById(UUID id) {
        try {
            employeeList()
                    .stream()
                    .filter(employee -> employee.getId()
                    .equals(id))
                    .findFirst().
                    ifPresentOrElse(employee -> System.out.println("Encontrado: " + employee),
                            () -> System.out.println("Não foi possível encontrar o funcionário pelo id."));
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public void deleteEmployeeByID(UUID uuid) {
        employeeRepository.deleteEmployeeByID(uuid);
    }
}
