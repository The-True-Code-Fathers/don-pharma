package com.codefathers.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Payment;
import com.codefathers.repository.EmployeeRepositoryImpl;
import com.codefathers.repository.PaymentRepository;

public class PaymentService {

    PaymentRepository paymentRepository;
    EmployeeRepositoryImpl employeeRepository;

    public PaymentService(PaymentRepository paymentRepository, EmployeeRepositoryImpl employeeRepository) {
        this.paymentRepository = paymentRepository;
        this.employeeRepository = employeeRepository;
    }

    public Payment createPayment(Employee employee,
            BigDecimal amountInTaxes,
            BigDecimal grossIncome,
            BigDecimal mealVoucherAmount,
            BigDecimal foodVoucherAmount,
            BigDecimal healthInsuranceAmount,
            BigDecimal dentalInsuranceAmount,
            BigDecimal profitSharingAmount) {

        if (isNegative(grossIncome, amountInTaxes, mealVoucherAmount, foodVoucherAmount,
                healthInsuranceAmount, dentalInsuranceAmount, profitSharingAmount)) {
            throw new IllegalArgumentException("Negative value");
        }

        BigDecimal totalDiscounts = amountInTaxes
                .add(mealVoucherAmount)
                .add(foodVoucherAmount)
                .add(healthInsuranceAmount)
                .add(dentalInsuranceAmount);

        if (grossIncome.compareTo(totalDiscounts) < 0) {
            throw new IllegalArgumentException("Gross income needs to be higher than total discounts");
        }

        Payment payment = Payment.builder().employee(employee).amountInTaxes(amountInTaxes).grossIncome(grossIncome)
                .mealVoucherAmount(mealVoucherAmount).foodVoucherAmount(foodVoucherAmount)
                .healthInsuranceAmount(healthInsuranceAmount).dentalInsuranceAmount(dentalInsuranceAmount)
                .profitSharingAmount(profitSharingAmount).build();

        paymentRepository.save(payment);
        return payment;
    }

    private boolean isNegative(BigDecimal... values) {
        for (BigDecimal value : values) {
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                return true;
            }
        }
        return false;
    }

    public BigDecimal calculateNetIncome(Payment payment) {
        BigDecimal totalDiscounts = payment.getAmountInTaxes()
                .add(payment.getMealVoucherAmount())
                .add(payment.getFoodVoucherAmount())
                .add(payment.getHealthInsuranceAmount())
                .add(payment.getDentalInsuranceAmount());

        return payment.getGrossIncome().subtract(totalDiscounts);
    }

    public List<Payment> findPaymentsByEmployee(Employee employee) {
        return paymentRepository.findPaymentsByEmployee(employee);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.getAllPayments();
    }

    public Employee findEmployeeById(UUID employeeId) {
        return employeeRepository.searchEmployeePerId(employeeId);
    }

    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

}
