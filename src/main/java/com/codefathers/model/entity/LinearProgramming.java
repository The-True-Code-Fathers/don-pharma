package com.codefathers.model.entity;

import com.codefathers.model.enums.EmployeeRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "linearprogramming")
public class LinearProgramming {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String Id;

    @Column(nullable = false)
    private EmployeeRole role;

    @Column(nullable = false)
    private double currentSales;

    @Column(nullable = false)
    private double salesGoal;

    @Column(nullable = false)
    private double additionalSalesNeeded;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private double quantity;

}
