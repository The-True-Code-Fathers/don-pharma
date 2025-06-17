package com.codefathers.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.codefathers.model.dto.CreateEmployeeDTO;
import com.codefathers.model.dto.UpdateEmployeeDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Product;
import com.codefathers.repository.interfaces.EmployeeRepository;

import jakarta.persistence.Id;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final Validator validator;

    public EmployeeService(EmployeeRepository employeeRepository, Validator validator) {
        this.employeeRepository = employeeRepository;
        this.validator = validator;
    }

    public void createEmployee(CreateEmployeeDTO createEmployeeDTO) {
        validateDTOFunctions(createEmployeeDTO);
        validateAge(createEmployeeDTO.getBirthDate());

        Employee employee = Employee.builder()
                .gender(createEmployeeDTO.getGender())
                .fullName(createEmployeeDTO.getFullName())
                .role(createEmployeeDTO.getRole())
                .birthDate(createEmployeeDTO.getBirthDate())
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();
        employeeRepository.save(employee);
    }

    public void updateEmployee(UpdateEmployeeDTO updateEmployeeDTO, @Valid UUID uuid) {
        validateDTOFunctions(updateEmployeeDTO);
        validateAge(updateEmployeeDTO.getBirthDate());

        Employee emp = employeeRepository.findById(uuid);

        if (emp == null) {
            throw new RuntimeException("Produto com SKU '" + uuid + "' não encontrado.");
        }
            emp.setGender(updateEmployeeDTO.getGender());
            emp.setFullName(updateEmployeeDTO.getFullName());
            emp.setRole(updateEmployeeDTO.getRole());
            emp.setActive(updateEmployeeDTO.isActive());

        try {
            employeeRepository.update(emp);
            employeeRepository.save(emp);
        } catch (ConstraintViolationException e) {
            System.out.println(e.getMessage());
        };
    }

    public void deleteEmployeeByID(UUID uuid) {
        employeeRepository.delete(uuid);
    }

    public List<Employee> employeeList() {
        return employeeRepository.listAll();
    }

    public void findEmployeeById(UUID employeeId) {
        try {
            employeeList()
                    .stream()
                    .filter(employee -> employee.getId()
                            .equals(employeeId))
                    .findFirst().ifPresentOrElse(employee -> System.out.println("Encontrado: " + employee),
                            () -> System.out.println("Não foi possível encontrar o funcionário pelo id."));
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public Long count() {
        return employeeRepository.count();
    }

    private void validateDTOFunctions(CreateEmployeeDTO createEmployeeDTO) {
        Set<ConstraintViolation<CreateEmployeeDTO>> violations = validator.validate(createEmployeeDTO);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Erros de validação: " + errors);
        }
    }

    private void validateDTOFunctions(UpdateEmployeeDTO updateEmployeeDTO) {
        Set<ConstraintViolation<UpdateEmployeeDTO>> violations = validator.validate(updateEmployeeDTO);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Erros de validação: " + errors);
        }
    }

    private void validateAge(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 16) {
            throw new IllegalArgumentException(
                    "O funcionário deve ter no mínimo 16 (dezesseis) anos para ser registrado");
        }
    }

    public void update(Employee employee) {
        employeeRepository.update(employee);
    }

}
