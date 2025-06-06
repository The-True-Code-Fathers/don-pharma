package com.codefathers.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Payment;

public interface PaymentRepository {
    List<Payment> getAllPayments();
    Optional<Payment> findById(UUID id);
    List<Payment> findPaymentsByEmployee(Employee employee);
    void save(Payment payment);
}
