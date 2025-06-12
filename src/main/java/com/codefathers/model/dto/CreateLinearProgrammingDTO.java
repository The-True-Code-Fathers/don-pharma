package com.codefathers.model.dto;

import com.codefathers.model.enums.EmployeeRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateLinearProgrammingDTO {
    private String name;
    private String Id;
    private EmployeeRole role;
    private double currentSales;
    private double salesGoal;
    private double additionalSalesNeeded;
}
