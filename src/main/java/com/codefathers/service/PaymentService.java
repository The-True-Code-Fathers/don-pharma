package com.codefathers.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Payment;
import com.codefathers.repository.PaymentRepository;

public class PaymentService {
    PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment createPayment(Employee employeeId, 
    BigDecimal amountInTaxes, 
    BigDecimal grossIncome,  
    BigDecimal mealVoucherAmount, 
    BigDecimal foodVoucherAmount, 
    BigDecimal healthInsuranceAmount, 
    BigDecimal dentalInsuranceAmount, 
    BigDecimal profitSharingAmount) {
        Payment payment = Payment.builder().
        employeeId(employeeId).
        amountInTaxes(amountInTaxes).
        grossIncome(grossIncome).
        mealVoucherAmount(mealVoucherAmount).
        foodVoucherAmount(foodVoucherAmount).
        healthInsuranceAmount(healthInsuranceAmount).
        dentalInsuranceAmount(dentalInsuranceAmount).
        profitSharingAmount(profitSharingAmount).
        build();
        return payment;
    }

    public Optional<Payment> findPaymentByEmployeeId(Employee employeeId) {
        return paymentRepository.findByEmployeeId(employeeId);
    }

    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.getAllPayments();
    }

}
