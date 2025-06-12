package com.codefathers.repository.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Payment;

public interface PaymentRepository {
    void save(Payment payment);

    void update(Payment payment);

    void delete(UUID id);

    Optional<Payment> findById(UUID id);

    List<Payment> findByEmployee(Employee employee);

    List<Payment> listAll();
}
