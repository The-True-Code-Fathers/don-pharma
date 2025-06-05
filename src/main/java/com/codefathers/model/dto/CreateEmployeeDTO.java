package com.codefathers.model.dto;

import com.codefathers.model.enums.EmployeeGender;
import com.codefathers.model.enums.EmployeeRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateEmployeeDTO {
    private String fullName;
    private LocalDate birthDate;
    private EmployeeGender gender;
    private EmployeeRole role;
}
