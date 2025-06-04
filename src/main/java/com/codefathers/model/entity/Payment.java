package com.codefathers.model.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Payment {
    private final int employeeId;
    private BigDecimal amountInTaxes;
    private BigDecimal grossIncome;
    private BigDecimal mealVoucherAmount;
    private BigDecimal foodVoucherAmount;
    private BigDecimal healthInsuranceAmount;
    private BigDecimal dentalInsuranceAmount;
    private BigDecimal profitSharingAmount;
}
