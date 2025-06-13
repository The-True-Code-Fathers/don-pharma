package com.codefathers.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.dto.CreatePaymentDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Payment;
import com.codefathers.repository.interfaces.EmployeeRepository;
import com.codefathers.repository.interfaces.PaymentRepository;

public class PaymentService {

    PaymentRepository paymentRepository;
    EmployeeRepository employeeRepository;

    public PaymentService(PaymentRepository paymentRepository, EmployeeRepository employeeRepository) {
        this.paymentRepository = paymentRepository;
        this.employeeRepository = employeeRepository;
    }

    public Payment createPayment(CreatePaymentDTO createPaymentDTO) {
        try {
            Employee employee = employeeRepository.findById(createPaymentDTO.getEmployee().getId());

        if (isNegative(createPaymentDTO.getGrossIncome(), createPaymentDTO.getAmountInTaxes(),
                createPaymentDTO.getMealVoucherAmount(), createPaymentDTO.getFoodVoucherAmount(),
                createPaymentDTO.getHealthInsuranceAmount(), createPaymentDTO.getDentalInsuranceAmount(),
                createPaymentDTO.getProfitSharingAmount())) {
            throw new IllegalArgumentException("Negative value");
        }

        if (createPaymentDTO.getGrossIncome().compareTo(createPaymentDTO.getAmountInTaxes()) < 0) {
            throw new IllegalArgumentException("Gross income needs to be higher than amount in taxxes");
        }

        Payment payment = Payment.builder()
                .employee(employee)
                .amountInTaxes(createPaymentDTO.getAmountInTaxes())
                .grossIncome(createPaymentDTO.getGrossIncome())
                .mealVoucherAmount(createPaymentDTO.getMealVoucherAmount())
                .foodVoucherAmount(createPaymentDTO.getFoodVoucherAmount())
                .healthInsuranceAmount(createPaymentDTO.getHealthInsuranceAmount())
                .dentalInsuranceAmount(createPaymentDTO.getDentalInsuranceAmount())
                .profitSharingAmount(createPaymentDTO.getProfitSharingAmount())
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        paymentRepository.save(payment);
        return payment;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
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
        return paymentRepository.findByEmployee(employee);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.listAll();
    }

    public Employee findEmployeeById(UUID employeeId) {
        return employeeRepository.findById(employeeId);
    }

    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    public void update(Payment payment) {
        paymentRepository.update(payment);
    }

}
