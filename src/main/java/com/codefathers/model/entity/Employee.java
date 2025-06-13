package com.codefathers.model.entity;

import com.codefathers.model.enums.EmployeeGender;
import com.codefathers.model.enums.EmployeeRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@Data
@Entity(name = "employee")
@AllArgsConstructor
@Builder
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

    @Column(nullable = false)
    private boolean active;

    public Employee(String fullName, LocalDate birthDate, EmployeeGender gender, EmployeeRole role, boolean active) {
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.role = role;
        this.active = active;
    }



}
