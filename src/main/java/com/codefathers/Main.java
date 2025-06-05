package com.codefathers;

import com.codefathers.repository.PaymentRepositoryImpl;
import com.codefathers.service.PaymentService;
import com.codefathers.view.PaymentViewModel;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
public class Main {
    public static void main(String[] args) {
        SessionFactory sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml") // Arquivo de configuração no src/main/resources
                .buildSessionFactory();

        PaymentRepositoryImpl paymentRepository = new PaymentRepositoryImpl(sessionFactory);
        PaymentService paymentService = new PaymentService(paymentRepository);
        PaymentViewModel paymentViewModel = new PaymentViewModel(paymentService);

        paymentViewModel.showMenu();

        sessionFactory.close();
    }
}