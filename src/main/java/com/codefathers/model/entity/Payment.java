package com.codefathers.model.entity;

import lombok.Data;

import java.math.BigDecimal;

import jakarta.persistence.Column;

@Data
public class Payment {
    private final int employeeId;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal amountInTaxes;

    @Column(precision = 19, scale = 4)
    private BigDecimal grossIncome;

    @Column(precision = 19, scale = 4)
    private BigDecimal mealVoucherAmount;

    @Column(precision = 19, scale = 4)
    private BigDecimal foodVoucherAmount;

    @Column(precision = 19, scale = 4)
    private BigDecimal healthInsuranceAmount;

    @Column(precision = 19, scale = 4)
    private BigDecimal dentalInsuranceAmount;

    @Column(precision = 19, scale = 4)
    private BigDecimal profitSharingAmount;
}
