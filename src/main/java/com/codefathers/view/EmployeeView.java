package com.codefathers.view;

import com.codefathers.model.dto.CreateEmployeeDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.enums.EmployeeGender;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.repository.EmployeeRepository;
import com.codefathers.repository.EmployeeRepositoryImpl;
import com.codefathers.service.EmployeeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class EmployeeView {
    private final EmployeeService employeeService;

    public EmployeeView(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public static void main(String[] args) {
        EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();
        EmployeeService employeeService = new EmployeeService(employeeRepository);
        EmployeeView view = new EmployeeView(employeeService);
        view.menu();
    }

    public void menu() {
        Scanner sc = new Scanner(System.in);
        int option;

        do {
            System.out.printf("- Menu de Funcionario -\n");
            System.out.printf("1 - Cadastrar Funcionario \n");
            System.out.printf("2 - Listar Funcionarios \n");
            System.out.printf("3 - Buscar Funcionario pelo Id \n");
            System.out.println("4 - Remover Funcionario pelo Id ");
            System.out.println("0 - Sair.\n");
            option = sc.nextInt();
            switch (option) {
                case 1:
                    System.out.println("--- Cadastrar Funcionario ---");
                    cadastrarFuncionario();
                    break;
                case 2:
                    System.out.print("--- Lista de Funcionarios ---");
                    listarFuncionarios();
                    break;
                case 3:
                    System.out.println("-- Usuários buscados pelo ID --");
                    acharFuncionarioPeloID();
                    break;
                case 4:
                    System.out.println("-- Removendo usuário pelo ID --");
                    removerFuncionarioPeloID();
                    break;
                case 0:
                    System.out.printf("Saindo...");
                    break;
                default:
                    System.out.printf("Opção imvalida, tente novamente!");
                    break;
            }

        } while (option != 0);
        sc.close();
    }

    public void cadastrarFuncionario() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.print("Nome para cadastro: ");
            String name = scanner.nextLine();

            System.out.print("Cargo (opções: LOCAL_MANAGER, SAC, HR, FINANCIAL, SALES, STORAGE, SHIPPING): ");
            String roleInput = scanner.nextLine().toUpperCase();
            EmployeeRole role = EmployeeRole.valueOf(roleInput);

            System.out.println("Data de nascimento (YYYY-MM-DD): ");
            String birth = scanner.nextLine();
            LocalDate birthDate = LocalDate.parse(birth);

            System.out.println("Gênero (MALE/FEMALE/NON-BINARY/UNDECLARED): ");
            String genderInput = scanner.nextLine().toUpperCase();
            EmployeeGender gender = EmployeeGender.valueOf(genderInput);

            CreateEmployeeDTO createEmployeeDTO = CreateEmployeeDTO.
                    builder().
                    fullName(name).
                    birthDate(birthDate).
                    gender(gender).
                    role(role).
                    build();
            employeeService.createEmployee(createEmployeeDTO);
        } catch (Exception e) {
            e.getMessage();
        }
        scanner.close();
    }

    public void listarFuncionarios() {
        List<Employee> employeeListService = employeeService.employeeList();

        employeeService.employeeList().forEach(System.out::println);
    }

    public void acharFuncionarioPeloID() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("Informe um id para buscarmos: ");
            String id = scanner.nextLine();
            UUID uuid = UUID.fromString(id);
            employeeService.findEmployeeById(uuid);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public void removerFuncionarioPeloID() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Informe o id do funcionario para removermos: ");
            String id = scanner.nextLine();
            UUID uuid = UUID.fromString(id);
            employeeService.deleteEmployeeByID(uuid);
        } catch (Exception e) {
            e.getMessage();
        }
        scanner.close();
    }
}