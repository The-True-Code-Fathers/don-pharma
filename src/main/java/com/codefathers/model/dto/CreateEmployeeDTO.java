package com.codefathers.model.dto;

import com.codefathers.model.enums.EmployeeGender;
import com.codefathers.model.enums.EmployeeRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateEmployeeDTO {
    private String fullName;
    private Date birthDate;
    private EmployeeGender gender;
    private EmployeeRole role;
}
