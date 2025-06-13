package com.codefathers.model.enums;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.codefathers.util.TaxCalculatorUtil;

import lombok.Getter;

public enum EmployeeRole {

    STORAGE(Map.of(
            EmployeeBenefits.AMOUNT_IN_TAXES, new BigDecimal("00.00"),
            EmployeeBenefits.GROSS_INCOME, new BigDecimal("2000.00"),
            EmployeeBenefits.MEAL_VOUCHER, new BigDecimal("300.00"),
            EmployeeBenefits.FOOD_VOUCHER, new BigDecimal("300.00"),
            EmployeeBenefits.HEALTH_INSURANCE, new BigDecimal("3000.00"),
            EmployeeBenefits.DENTAL_INSURANCE, new BigDecimal("3000.00"),
            EmployeeBenefits.PROFIT_SHARING, new BigDecimal("500.00"))),
    SHIPPING(Map.of(
            EmployeeBenefits.AMOUNT_IN_TAXES, new BigDecimal("00.00"),
            EmployeeBenefits.GROSS_INCOME, new BigDecimal("2500.00"),
            EmployeeBenefits.MEAL_VOUCHER, new BigDecimal("350.00"),
            EmployeeBenefits.FOOD_VOUCHER, new BigDecimal("350.00"),
            EmployeeBenefits.HEALTH_INSURANCE, new BigDecimal("3500.00"),
            EmployeeBenefits.DENTAL_INSURANCE, new BigDecimal("3000.00"),
            EmployeeBenefits.PROFIT_SHARING, new BigDecimal("600.00"))),
    SAC(Map.of(
            EmployeeBenefits.AMOUNT_IN_TAXES, new BigDecimal("00.00"),
            EmployeeBenefits.GROSS_INCOME, new BigDecimal("3000.00"),
            EmployeeBenefits.MEAL_VOUCHER, new BigDecimal("400.00"),
            EmployeeBenefits.FOOD_VOUCHER, new BigDecimal("400.00"),
            EmployeeBenefits.HEALTH_INSURANCE, new BigDecimal("4000.00"),
            EmployeeBenefits.DENTAL_INSURANCE, new BigDecimal("3000.00"),
            EmployeeBenefits.PROFIT_SHARING, new BigDecimal("700.00"))),
    HR(Map.of(
            EmployeeBenefits.AMOUNT_IN_TAXES, new BigDecimal("00.00"),
            EmployeeBenefits.GROSS_INCOME, new BigDecimal("3500.00"),
            EmployeeBenefits.MEAL_VOUCHER, new BigDecimal("450.00"),
            EmployeeBenefits.FOOD_VOUCHER, new BigDecimal("450.00"),
            EmployeeBenefits.HEALTH_INSURANCE, new BigDecimal("4500.00"),
            EmployeeBenefits.DENTAL_INSURANCE, new BigDecimal("3000.00"),
            EmployeeBenefits.PROFIT_SHARING, new BigDecimal("800.00"))),
    FINANCIAL(Map.of(
            EmployeeBenefits.AMOUNT_IN_TAXES, new BigDecimal("00.00"),
            EmployeeBenefits.GROSS_INCOME, new BigDecimal("4000.00"),
            EmployeeBenefits.MEAL_VOUCHER, new BigDecimal("500.00"),
            EmployeeBenefits.FOOD_VOUCHER, new BigDecimal("500.00"),
            EmployeeBenefits.HEALTH_INSURANCE, new BigDecimal("5000.00"),
            EmployeeBenefits.DENTAL_INSURANCE, new BigDecimal("3000.00"),
            EmployeeBenefits.PROFIT_SHARING, new BigDecimal("1000.00"))),
    SALES(Map.of(
            EmployeeBenefits.AMOUNT_IN_TAXES, new BigDecimal("00.00"),
            EmployeeBenefits.GROSS_INCOME, new BigDecimal("4500.00"),
            EmployeeBenefits.MEAL_VOUCHER, new BigDecimal("600.00"),
            EmployeeBenefits.FOOD_VOUCHER, new BigDecimal("600.00"),
            EmployeeBenefits.HEALTH_INSURANCE, new BigDecimal("5500.00"),
            EmployeeBenefits.DENTAL_INSURANCE, new BigDecimal("3000.00"),
            EmployeeBenefits.PROFIT_SHARING, new BigDecimal("1200.00"))),
    LOCAL_MANAGER(Map.of(
            EmployeeBenefits.AMOUNT_IN_TAXES, new BigDecimal("00.00"),
            EmployeeBenefits.GROSS_INCOME, new BigDecimal("6000.00"),
            EmployeeBenefits.MEAL_VOUCHER, new BigDecimal("700.00"),
            EmployeeBenefits.FOOD_VOUCHER, new BigDecimal("700.00"),
            EmployeeBenefits.HEALTH_INSURANCE, new BigDecimal("6000.00"),
            EmployeeBenefits.DENTAL_INSURANCE, new BigDecimal("3000.00"),
            EmployeeBenefits.PROFIT_SHARING, new BigDecimal("1500.00")));

    @Getter
    private final Map<EmployeeBenefits, BigDecimal> BENEFITS;

    public String getLabel() {
        switch (this) {
            case LOCAL_MANAGER:
                return "Local Manager";
            case SAC:
                return "Customer Service";
            case HR:
                return "Human Resources";
            case FINANCIAL:
                return "Financial";
            case SALES:
                return "Seller";
            case STORAGE:
                return "Storage";
            case SHIPPING:
                return "Shipping";
            default:
                return this.name(); // Caso padrão
        }
    }

    EmployeeRole(Map<EmployeeBenefits, BigDecimal> benefits) {
        BigDecimal grossIncome = benefits.get(EmployeeBenefits.GROSS_INCOME);
        BigDecimal amountInTaxes = TaxCalculatorUtil.calculateIR(grossIncome);

        Map<EmployeeBenefits, BigDecimal> benefitsWithTax = new HashMap<>(benefits);
        benefitsWithTax.put(EmployeeBenefits.AMOUNT_IN_TAXES, amountInTaxes);

        this.BENEFITS = Collections.unmodifiableMap(benefitsWithTax);
    }
}
