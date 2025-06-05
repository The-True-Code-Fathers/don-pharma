package com.codefathers.model.entity;

import lombok.Data;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

@Data
@Entity(name = "payment")
public class Payment {

    @OneToOne
    @MapsId
    @JoinColumn(name = "employee_id")
    private final int employeeId;
    
    @Column(name = "amount_in_taxes", precision = 19, scale = 4, nullable = false)
    private BigDecimal amountInTaxes;

    @Column(name = "gross_income", precision = 19, scale = 4, nullable = false)
    private BigDecimal grossIncome;

    @Column(name = "meal_voucher_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal mealVoucherAmount;

    @Column(name = "food_voucher_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal foodVoucherAmount;

    @Column(name = "health_insurance_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal healthInsuranceAmount;

    @Column(name = "dental_insurance_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal dentalInsuranceAmount;

    @Column(name = "profit_sharing_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal profitSharingAmount;
    
}
