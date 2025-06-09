package com.codefathers.view;

import com.codefathers.model.dto.CreateShippingProviderDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.ShippingProviderRepository;
import com.codefathers.repository.ShippingProviderRepositoryImpl;
import com.codefathers.service.ShippingProviderService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;;

public class ShippingProviderView {
    private final ShippingProviderService shippingProviderService;
    private final Scanner scanner = new Scanner(System.in);

    public ShippingProviderView(ShippingProviderService shippingProviderService) {
        this.shippingProviderService = shippingProviderService;
    }

    public static void main(String[] args) {
        ShippingProviderRepository shippingProviderRepository = new ShippingProviderRepositoryImpl();
        ShippingProviderService shippingProviderService = new ShippingProviderService(shippingProviderRepository);
        ShippingProviderView shippingProviderView = new ShippingProviderView(shippingProviderService);
        shippingProviderView.menu();
    }

    public void menu() {
        while (true) {
            System.out.println("\n=== Sistema Transportadora ===");
            System.out.println("1 - Listar transportadoras");
            System.out.println("2 - Cadastrar transportadora");
            System.out.println("3 - Buscar transportadora por ID");
            System.out.println("4 - Remover transportadora por ID");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");

            String option = scanner.nextLine();

            switch (option) {
                case "1" -> listAll();
                case "2" -> register();
                case "3" -> searchById();
                case "4" -> removeById();
                case "0" -> {
                    System.out.println("Saindo...");
                    return;
                }
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    private void listAll() {
        List<ShippingProvider> providers = shippingProviderService.listAllShippingProviders();
        if (providers.isEmpty()) {
            System.out.println("Nenhuma transportadora cadastrada.");
            return;
        }
        providers.forEach(sp -> {
            System.out.printf("ID: %s, Nome: %s, CNPJ: %s, Capacidade diária: %s\n",
                    sp.getId(), sp.getName(), sp.getCnpj(), sp.getDailyCapacity());
        });
    }

    private void register() {
        try {
            System.out.print("Nome: ");
            String name = scanner.nextLine();

            System.out.print("CNPJ: ");
            String cnpj = scanner.nextLine();

            System.out.print("Preço base (decimal): ");
            BigDecimal basePrice = new BigDecimal(scanner.nextLine());

            System.out.print("Capacidade diária (decimal): ");
            BigDecimal dailyCapacity = new BigDecimal(scanner.nextLine());

            List<ShippingArea> areas = new ArrayList<>();
            System.out.print("Quantas áreas de atendimento deseja cadastrar? ");
            int areasCount = Integer.parseInt(scanner.nextLine());
            for (int i = 0; i < areasCount; i++) {
                System.out.printf("Descrição área %d: ", i + 1);
                String desc = scanner.nextLine();

                System.out.printf("Estados atendidos (ex: SP, RJ): ");
                String states = scanner.nextLine();

                ShippingArea area = new ShippingArea();
                area.setDescription(desc);
                area.setStates(states);
                areas.add(area);
            }

            CreateShippingProviderDTO dto = CreateShippingProviderDTO.builder()
                    .name(name)
                    .cnpj(cnpj)
                    .basePrice(basePrice)
                    .dailyCapacity(dailyCapacity)
                    .shippingAreas(areas)
                    .build();

            shippingProviderService.registerShippingProvider(dto);
            System.out.println("Transportadora cadastrada com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar transportadora: " + e.getMessage());
        }
    }

    private void searchById() {
        try {
            System.out.print("Informe o ID da transportadora: ");
            UUID id = UUID.fromString(scanner.nextLine());
            ShippingProvider sp = shippingProviderService.searchShippingProviderPerId(id);
            if (sp != null) {
                System.out.println("Transportadora encontrada:");
                System.out.println(sp);
            } else {
                System.out.println("Transportadora não encontrada.");
            }
        } catch (Exception e) {
            System.out.println("ID inválido.");
        }
    }

    private void removeById() {
        try {
            System.out.print("Informe o ID da transportadora para remover: ");
            UUID id = UUID.fromString(scanner.nextLine());
            ShippingProvider removed = shippingProviderService.removeShippingProvider(id);
            if (removed != null) {
                System.out.println("Transportadora removida com sucesso.");
            } else {
                System.out.println("Transportadora não encontrada para remoção.");
            }
        } catch (Exception e) {
            System.out.println("ID inválido.");
        }
    }
}