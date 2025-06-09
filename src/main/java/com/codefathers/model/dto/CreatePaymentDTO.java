package com.codefathers.model.dto;

import java.math.BigDecimal;

import com.codefathers.model.entity.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreatePaymentDTO {

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee", nullable = false)
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
