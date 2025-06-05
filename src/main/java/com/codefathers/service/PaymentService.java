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
        Payment payment = Payment.builder().
        employee(employee).
        amountInTaxes(amountInTaxes).
        grossIncome(grossIncome).
        mealVoucherAmount(mealVoucherAmount).
        foodVoucherAmount(foodVoucherAmount).
        healthInsuranceAmount(healthInsuranceAmount).
        dentalInsuranceAmount(dentalInsuranceAmount).
        profitSharingAmount(profitSharingAmount).
        build();
        paymentRepository.save(payment);
        return payment;
    }

    public Optional<Payment> findPaymentByEmployee(Employee employee) {
        return paymentRepository.findPaymentByEmployee(employee);
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
