package com.codefathers.model.entity;

import com.codefathers.model.enums.EmployeeGender;
import com.codefathers.model.enums.EmployeeRole;
import lombok.Data;

import java.util.Date;

@Data
public class Employee {
    private int id;
    private String fullName;
    private Date birthDate;
    private EmployeeGender gender;
    private EmployeeRole role;

}
