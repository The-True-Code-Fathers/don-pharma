package com.codefathers.model.entity;

import com.codefathers.model.enums.EmployeeGender;
import com.codefathers.model.enums.EmployeeRole;
import lombok.Data;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity(name = "employee")
public class Employee {
    
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeGender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeRole role;

    public Employee(String fullName, LocalDate birthDate, EmployeeGender gender, EmployeeRole role) {
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.role = role;
    }

}
