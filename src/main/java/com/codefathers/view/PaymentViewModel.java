package com.codefathers.view;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Payment;
import com.codefathers.service.PaymentService;

public class PaymentViewModel {

    private final PaymentService paymentService;
    private Payment currentPayment;
    private final Scanner scanner = new Scanner(System.in);

    public PaymentViewModel(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n===== Payment Management =====");
            System.out.println("1 - List All Payments");
            System.out.println("2 - Create Payment");
            System.out.println("3 - Find Payment by Payment UUID");
            System.out.println("4 - Find Payment by Employee UUID");
            System.out.println("5 - Exit");
            System.out.print("Choose an option: ");

            String option = scanner.nextLine();

            switch (option) {
                case "1" -> listAllPayments();
                case "2" -> createPaymentInteractive();
                case "3" -> findPaymentByIdInteractive();
                case "4" -> findPaymentByEmployeeIdInteractive();
                case "5" -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid option! Try again.");
            }
        }
    }

    // 🚀 Método para criar Payment com interação
    private void createPaymentInteractive() {
        try {
            System.out.println("\n--- Create Payment ---");

            System.out.print("Enter Employee UUID: ");
            UUID employeeId = UUID.fromString(scanner.nextLine());
            Employee employee = new Employee();
            employee.setId(employeeId);

            System.out.print("Enter Gross Income: ");
            BigDecimal grossIncome = new BigDecimal(scanner.nextLine());

            System.out.print("Enter Amount in Taxes: ");
            BigDecimal amountInTaxes = new BigDecimal(scanner.nextLine());

            System.out.print("Enter Meal Voucher Amount: ");
            BigDecimal mealVoucherAmount = new BigDecimal(scanner.nextLine());

            System.out.print("Enter Food Voucher Amount: ");
            BigDecimal foodVoucherAmount = new BigDecimal(scanner.nextLine());

            System.out.print("Enter Health Insurance Amount: ");
            BigDecimal healthInsuranceAmount = new BigDecimal(scanner.nextLine());

            System.out.print("Enter Dental Insurance Amount: ");
            BigDecimal dentalInsuranceAmount = new BigDecimal(scanner.nextLine());

            System.out.print("Enter Profit Sharing Amount: ");
            BigDecimal profitSharingAmount = new BigDecimal(scanner.nextLine());

            currentPayment = paymentService.createPayment(
                    employee,
                    amountInTaxes,
                    grossIncome,
                    mealVoucherAmount,
                    foodVoucherAmount,
                    healthInsuranceAmount,
                    dentalInsuranceAmount,
                    profitSharingAmount);

            System.out.println("✅ Payment created successfully!");
            System.out.println(currentPayment);

        } catch (Exception e) {
            System.out.println("❌ Error creating payment: " + e.getMessage());
        }
    }

    // 🔍 Buscar por Payment ID
    private void findPaymentByIdInteractive() {
        try {
            System.out.println("\n--- Find Payment by Payment UUID ---");

            System.out.print("Enter Payment UUID: ");
            UUID id = UUID.fromString(scanner.nextLine());

            Optional<Payment> payment = paymentService.findById(id);

            if (payment.isPresent()) {
                System.out.println("✅ Payment found:");
                System.out.println(payment.get());
            } else {
                System.out.println("❌ Payment not found.");
            }

        } catch (Exception e) {
            System.out.println("❌ Invalid UUID or error: " + e.getMessage());
        }
    }

    // 🔍 Buscar por Employee ID
    private void findPaymentByEmployeeIdInteractive() {
        try {
            System.out.println("\n--- Find Payment by Employee UUID ---");

            System.out.print("Enter Employee UUID: ");
            System.out.print("Enter Employee UUID: ");
            UUID employeeUUID = UUID.fromString(scanner.nextLine());

            // supondo que exista um construtor com id
            Employee employee = new Employee();
            employee.setId(employeeUUID);

            Optional<Payment> payment = paymentService.findPaymentByEmployeeId(employee);

            if (payment.isPresent()) {
                System.out.println("✅ Payment found for employee:");
                System.out.println(payment.get());
            } else {
                System.out.println("❌ No payment found for this employee.");
            }

        } catch (Exception e) {
            System.out.println("❌ Invalid UUID or error: " + e.getMessage());
        }
    }

    // 📜 Listar todos os pagamentos
    private void listAllPayments() {
        System.out.println("\n--- List of All Payments ---");

        List<Payment> payments = paymentService.getAllPayments();

        if (payments.isEmpty()) {
            System.out.println("No payments found.");
        } else {
            payments.forEach(payment -> {
                System.out.println("---------------");
                System.out.println(payment);
            });
        }
    }
}
