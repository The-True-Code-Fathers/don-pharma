package com.codefathers.model.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Data
@Builder
@Entity(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
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
