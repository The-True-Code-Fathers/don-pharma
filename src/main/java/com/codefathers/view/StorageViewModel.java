package com.codefathers.view;

import java.util.Scanner;

import com.codefathers.repository.ProductRepositoryImpl;
import com.codefathers.repository.StorageRepositoryImpl;
import com.codefathers.service.StorageService;
import com.codefathers.util.HibernateUtil;

public class StorageViewModel {

    private final StorageService storageService;
    private final Scanner scanner;

    public StorageViewModel(StorageService storageService) {
        this.storageService = storageService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            System.out.println("\n========= MENU ESTOQUE =========");
            System.out.println("1 - Adicionar produto ao estoque");
            System.out.println("2 - Consultar quantidade em estoque");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");
            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1" -> adicionarProduto();
                case "2" -> consultarEstoque();
                case "0" -> {
                    System.out.println("Saindo...");
                    return;
                }
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    private void adicionarProduto() {
        try {
            System.out.print("Informe o SKU do produto: ");
            String sku = scanner.nextLine();

            System.out.print("Informe a quantidade a adicionar: ");
            int quantidade = Integer.parseInt(scanner.nextLine());

            storageService.createStorage(sku, quantidade);
            
            System.out.println("Produto adicionado com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao adicionar produto: " + e.getMessage());
        }
    }

    private void consultarEstoque() {
        try {
            System.out.print("Informe o SKU do produto: ");
            String sku = scanner.nextLine();
            int quantidade = storageService.getQuantityStock(sku);
            System.out.println("Quantidade em estoque: " + quantidade);
        } catch (Exception e) {
            System.out.println("Erro ao consultar estoque: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        StorageRepositoryImpl storageRepository = new StorageRepositoryImpl();
        ProductRepositoryImpl productRepository = new ProductRepositoryImpl();
        StorageService storageService = new StorageService(storageRepository, productRepository);
        StorageViewModel viewModel = new StorageViewModel(storageService);

        viewModel.start();

        HibernateUtil.shutdown();
    }

}
