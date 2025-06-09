package com.codefathers.view;

import com.codefathers.model.dto.CreateShippingAreaDTO;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.ShippingAreaRepository;
import com.codefathers.repository.ShippingAreaRepositoryImpl;
import com.codefathers.service.ShippingAreaService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;


import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class ShippingAreaView {
    private final ShippingAreaService shippingAreaService;
    private final Scanner scanner = new Scanner(System.in);

    public ShippingAreaView(ShippingAreaService shippingAreaService) {
        this.shippingAreaService = shippingAreaService;
    }

    public static void main(String[] args) {

        ShippingAreaRepository repository = new ShippingAreaRepositoryImpl();
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        ShippingAreaService service = new ShippingAreaService(repository,validator);
        ShippingAreaView view = new ShippingAreaView(service);
        view.menu();
    }

    public void menu(){
        int option;
        do {
            System.out.println("\n--- Menu de Áreas de Entrega ---");
            System.out.println("1 - Cadastrar Área de Entrega");
            System.out.println("2 - Listar Áreas de Entrega");
            System.out.println("3 - Remover por Id");
            System.out.println("4 - Buscar por Id");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");
            option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1 -> cadastrarAreaEntrega();
                case 2 -> listarAreasEntrega();
                case 3 -> removerPorId();
                case 4 -> buscarPorId();
                case 0 -> System.out.println("Saindo...");
                default -> System.out.println("Opção inválida.");
            }
        } while (option != 0);
    }

    private void listarAreasEntrega() {
        try {
            List<?> areas = shippingAreaService.findAllShippingAreas();
            if (areas.isEmpty()) {
                System.out.println("Nenhuma área de entrega cadastrada.");
            } else {
                System.out.println("\n--- Áreas de Entrega Cadastradas ---");
                areas.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar áreas: " + e.getMessage());
        }
    }

    private void cadastrarAreaEntrega(){
        try {
            System.out.print("Nome do provedor de entrega: ");
            String providerName = scanner.nextLine();

            ShippingProvider provider = new ShippingProvider();
            provider.setName(providerName);

            System.out.print("Descrição (opcional): ");
            String description = scanner.nextLine();

            System.out.print("Estados atendidos (ex: SP, RJ, MG): ");
            String states = scanner.nextLine();

            CreateShippingAreaDTO dto = new CreateShippingAreaDTO();
            dto.setShippingProvider(provider);
            dto.setDescription(description);
            dto.setStates(states);

            shippingAreaService.saveShippingArea(dto);
            System.out.println("Área de entrega cadastrada com sucesso!");
        } catch (ConstraintViolationException e) {
            e.getConstraintViolations().forEach(v -> System.out.println("Erro: " + v.getMessage()));
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    private void buscarPorId() {
        try {
            System.out.print("Informe o ID da área de entrega: ");
            String input = scanner.nextLine();
            UUID id = UUID.fromString(input);
            var area = shippingAreaService.findShippingAreaById(id);
            System.out.println("Área encontrada: " + area);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void removerPorId() {
        try {
            System.out.print("Informe o ID da área de entrega para remover: ");
            String input = scanner.nextLine();
            UUID id = UUID.fromString(input);
            shippingAreaService.deleteShippingAreaById(id);
            System.out.println("Área de entrega removida com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }


}
