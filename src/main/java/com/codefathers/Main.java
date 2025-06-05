package com.codefathers;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.codefathers.repository.EmployeeRepositoryImpl;
import com.codefathers.repository.PaymentRepositoryImpl;
import com.codefathers.service.PaymentService;
import com.codefathers.view.PaymentViewModel;
public class Main {
    public static void main(String[] args) {
        SessionFactory sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml") // Arquivo de configuração no src/main/resources
                .buildSessionFactory();
        EmployeeRepositoryImpl employeeRepository = new EmployeeRepositoryImpl();
        PaymentRepositoryImpl paymentRepository = new PaymentRepositoryImpl(sessionFactory);
        PaymentService paymentService = new PaymentService(paymentRepository, employeeRepository);
        PaymentViewModel paymentViewModel = new PaymentViewModel(paymentService);

        paymentViewModel.showMenu();

        sessionFactory.close();
    }
}