package com.codefathers.view;

import com.codefathers.model.dto.CreateShippingOrderDTO;
import com.codefathers.model.entity.ShippingOrder;
import com.codefathers.model.enums.ShippingServiceStatus;
import com.codefathers.repository.ShippingOrderRepository;
import com.codefathers.repository.ShippingOrderRepositoryImpl;
import com.codefathers.repository.ShippingProviderRepository;
import com.codefathers.repository.ShippingProviderRepositoryImpl;
import com.codefathers.service.ShippingOrderService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class ShippingOrderView {
    private final ShippingOrderService shippingOrderService;
    private final Scanner scanner = new Scanner(System.in);

    public ShippingOrderView(ShippingOrderService shippingOrderService) {
        this.shippingOrderService = shippingOrderService;
    }

    public static void main(String[] args) {
        ShippingOrderRepository orderRepository = new ShippingOrderRepositoryImpl();
        ShippingProviderRepository providerRepository = new ShippingProviderRepositoryImpl();
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        ShippingOrderService service = new ShippingOrderService(orderRepository, providerRepository, validator);
        ShippingOrderView view = new ShippingOrderView(service);

        view.menu();
    }

    public void menu() {
        int option;
        do {
            System.out.println("\n--- Menu de Pedidos de Entrega ---");
            System.out.println("1 - Cadastrar Pedido de Entrega");
            System.out.println("2 - Listar Pedidos de Entrega");
            System.out.println("3 - Remover Pedido por Id");
            System.out.println("4 - Buscar Pedido por Id");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");
            option = scanner.nextInt();
            scanner.nextLine(); // consumir enter

            switch (option) {
                case 1 -> cadastrarPedidoEntrega();
                case 2 -> listarPedidosEntrega();
                case 3 -> removerPedidoPorId();
                case 4 -> buscarPedidoPorId();
                case 0 -> System.out.println("Saindo...");
                default -> System.out.println("Opção inválida.");
            }
        } while (option != 0);
    }
    private void listarPedidosEntrega() {
        try {
            List<ShippingOrder> orders = shippingOrderService.listAllShippingOrders();
            if (orders.isEmpty()) {
                System.out.println("Nenhum pedido de entrega cadastrado.");
            } else {
                System.out.println("\n--- Pedidos de Entrega ---");
                orders.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar pedidos: " + e.getMessage());
        }
    }
    private void cadastrarPedidoEntrega() {
        try {
            System.out.print("ID do provedor de entrega: ");
            UUID providerId = UUID.fromString(scanner.nextLine());

            System.out.print("Estado de destino (ex: SP): ");
            String destinationState = scanner.nextLine();

            System.out.print("Cidade de destino: ");
            String destinationCity = scanner.nextLine();

            System.out.print("Peso da encomenda (kg): ");
            BigDecimal weight = new BigDecimal(scanner.nextLine());

            System.out.print("Status (PENDING, SHIPPED, DELIVERED): ");
            ShippingServiceStatus status = ShippingServiceStatus.valueOf(scanner.nextLine().toUpperCase());

            System.out.print("Dias estimados para entrega (opcional): ");
            String estDaysInput = scanner.nextLine();
            Integer estimatedDeliveryDays = estDaysInput.isBlank() ? null : Integer.valueOf(estDaysInput);

            System.out.print("Data de envio (yyyy-MM-dd) (opcional): ");
            String shipmentDateInput = scanner.nextLine();
            LocalDate shipmentDate = shipmentDateInput.isBlank() ? null : LocalDate.parse(shipmentDateInput);

            System.out.print("Data prevista de entrega (yyyy-MM-dd) (opcional): ");
            String deliveryDateInput = scanner.nextLine();
            LocalDate deliveryDate = deliveryDateInput.isBlank() ? null : LocalDate.parse(deliveryDateInput);

            System.out.print("Custo do frete (opcional): ");
            String costInput = scanner.nextLine();
            BigDecimal shippingCost = costInput.isBlank() ? null : new BigDecimal(costInput);

            CreateShippingOrderDTO dto = CreateShippingOrderDTO.builder()
                    .shippingProviderId(providerId)
                    .destinationState(destinationState)
                    .destinationCity(destinationCity)
                    .weight(weight)
                    .status(status)
                    .estimatedDeliveryDays(estimatedDeliveryDays)
                    .shipmentDate(shipmentDate)
                    .deliveryDate(deliveryDate)
                    .shippingCost(shippingCost)
                    .build();

            shippingOrderService.createOrder(dto);
            System.out.println("Pedido de entrega cadastrado com sucesso!");
        } catch (ConstraintViolationException e) {
            e.getConstraintViolations().forEach(v -> System.out.println("Erro: " + v.getMessage()));
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }
    private void buscarPedidoPorId() {
        try {
            System.out.print("Informe o ID do pedido: ");
            UUID id = UUID.fromString(scanner.nextLine());
            ShippingOrder order = shippingOrderService.searchShippingOrder(id);
            if (order == null) {
                System.out.println("Pedido não encontrado.");
            } else {
                System.out.println("Pedido encontrado: " + order);
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void removerPedidoPorId() {
        try {
            System.out.print("Informe o ID do pedido para remover: ");
            UUID id = UUID.fromString(scanner.nextLine());
            ShippingOrder removed = shippingOrderService.removeShippingOrder(id);
            if (removed != null) {
                System.out.println("Pedido removido com sucesso.");
            } else {
                System.out.println("Pedido não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
