// package com.codefathers.view;

// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;
// import java.util.Scanner;
// import java.util.UUID;

// import com.codefathers.model.dto.CreateEmployeeDTO;
// import com.codefathers.model.dto.CreateOrderDTO;
// import com.codefathers.model.dto.CreatePaymentDTO;
// import com.codefathers.model.dto.CreateProductDTO;
// import com.codefathers.model.dto.CreatePurchaseOrderDTO;
// import com.codefathers.model.dto.CreateShippingAreaDTO;
// import com.codefathers.model.dto.CreateShippingOrderDTO;
// import com.codefathers.model.dto.CreateShippingProviderDTO;
// import com.codefathers.model.dto.CreateStorageDTO;
// import com.codefathers.model.dto.UpdateProductDTO;
// import com.codefathers.model.entity.Employee;
// import com.codefathers.model.entity.Order;
// import com.codefathers.model.entity.Payment;
// import com.codefathers.model.entity.Product;
// import com.codefathers.model.entity.ShippingArea;
// import com.codefathers.model.entity.ShippingOrder;
// import com.codefathers.model.entity.ShippingProvider;
// import com.codefathers.model.enums.EmployeeGender;
// import com.codefathers.model.enums.EmployeeRole;
// import com.codefathers.model.enums.ShippingServiceStatus;
// import com.codefathers.repository.implementations.EmployeeRepositoryImpl;
// import com.codefathers.repository.implementations.OrderRepositoryImpl;
// import com.codefathers.repository.implementations.PaymentRepositoryImpl;
// import com.codefathers.repository.implementations.ProductRepositoryImpl;
// import com.codefathers.repository.implementations.PurchaseOrderRepositoryImpl;
// import com.codefathers.repository.implementations.ShippingAreaRepositoryImpl;
// import com.codefathers.repository.implementations.ShippingOrderRepositoryImpl;
// import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
// import com.codefathers.repository.implementations.StorageRepositoryImpl;
// import com.codefathers.repository.interfaces.EmployeeRepository;
// import com.codefathers.repository.interfaces.OrderRepository;
// import com.codefathers.repository.interfaces.PaymentRepository;
// import com.codefathers.repository.interfaces.ProductRepository;
// import com.codefathers.repository.interfaces.PurchaseOrderRepository;
// import com.codefathers.repository.interfaces.ShippingAreaRepository;
// import com.codefathers.repository.interfaces.ShippingOrderRepository;
// import com.codefathers.repository.interfaces.ShippingProviderRepository;
// import com.codefathers.repository.interfaces.StorageRepository;
// import com.codefathers.service.EmployeeService;
// import com.codefathers.service.OrderService;
// import com.codefathers.service.PaymentService;
// import com.codefathers.service.ProductService;
// import com.codefathers.service.PurchaseOrderService;
// import com.codefathers.service.ShippingAreaService;
// import com.codefathers.service.ShippingOrderService;
// import com.codefathers.service.ShippingProviderService;
// import com.codefathers.service.StorageService;
// import com.codefathers.util.ValidatorUtil;


// public class Main {

//     public static void shippingOrderFunctions() {
//         Scanner scanner = new Scanner(System.in);
//         ShippingOrderRepository shippingOrderRepository = new ShippingOrderRepositoryImpl();
//         ShippingProviderRepository shippingProviderRepository = new ShippingProviderRepositoryImpl();
//         ShippingOrderService shippingOrderService = new ShippingOrderService(shippingOrderRepository,
//                 shippingProviderRepository, ValidatorUtil.getValidator());

//         while (true) {
//             System.out.println("\n== Shipping Order Menu ==");
//             System.out.println("1 - Criar ordem de entrega");
//             System.out.println("2 - Buscar ordem por ID");
//             System.out.println("3 - Listar todas as ordens");
//             System.out.println("4 - Remover ordem por ID");
//             System.out.println("0 - Sair");
//             System.out.print("Escolha: ");

//             String choice = scanner.nextLine();

//             try {
//                 switch (choice) {
//                     case "1":
//                         System.out.print("ID do Shipping Provider: ");
//                         UUID providerId = UUID.fromString(scanner.nextLine());
//                         ShippingProvider shippingProvider = shippingProviderRepository
//                                 .searchShippingProviderPerId(providerId);

//                         System.out.print("Estado de destino: ");
//                         String state = scanner.nextLine();

//                         System.out.print("Cidade de destino: ");
//                         String city = scanner.nextLine();

//                         System.out.print("Peso (kg): ");
//                         BigDecimal weight = new BigDecimal(scanner.nextLine());

//                         System.out.println("Status (PENDENTE, EM_TRANSPORTE, ENTREGUE, ATRASADO): ");
//                         ShippingServiceStatus status = ShippingServiceStatus.valueOf(scanner.nextLine().toUpperCase());

//                         System.out.print("Dias estimados para entrega: ");
//                         int estimatedDays = Integer.parseInt(scanner.nextLine());

//                         System.out.print("Data de envio (yyyy-mm-dd): ");
//                         LocalDate shipmentDate = LocalDate.parse(scanner.nextLine());

//                         System.out.print("Data de entrega (yyyy-mm-dd): ");
//                         LocalDate deliveryDate = LocalDate.parse(scanner.nextLine());

//                         System.out.print("Custo do frete: ");
//                         BigDecimal cost = new BigDecimal(scanner.nextLine());

//                         CreateShippingOrderDTO createShippingOrderDTO = CreateShippingOrderDTO.builder()
//                                 .shippingProvider(shippingProvider)
//                                 .destinationState(state)
//                                 .destinationCity(city)
//                                 .weight(weight)
//                                 .status(status)
//                                 .estimatedDeliveryDays(estimatedDays)
//                                 .deliveryDate(deliveryDate)
//                                 .shipmentDate(shipmentDate)
//                                 .shippingCost(cost)
//                                 .build();

//                         shippingOrderService.createOrder(createShippingOrderDTO);
//                         System.out.println("Ordem criada com sucesso.");
//                         break;

//                     case "2":
//                         System.out.print("ID da ordem: ");
//                         UUID orderId = UUID.fromString(scanner.nextLine());
//                         ShippingOrder order = shippingOrderService.searchShippingOrder(orderId);
//                         System.out.println(order);
//                         break;

//                     case "3":
//                         var orders = shippingOrderService.listAllShippingOrders();
//                         orders.forEach(System.out::println);
//                         break;

//                     case "4":
//                         System.out.print("ID da ordem para remover: ");
//                         UUID removeId = UUID.fromString(scanner.nextLine());
//                         var removed = shippingOrderService.removeShippingOrder(removeId);
//                         if (removed != null) {
//                             System.out.println("Ordem removida com sucesso.");
//                         } else {
//                             System.out.println("Ordem não encontrada.");
//                         }
//                         break;

//                     case "0":
//                         System.out.println("Saindo do menu de Shipping Orders.");
//                         return;

//                     default:
//                         System.out.println("Opção inválida.");
//                 }
//             } catch (Exception e) {
//                 System.out.println("Erro: " + e.getMessage());
//             }
//         }
//     }

//     public static void shippingProviderFunctions() {

//         Scanner scanner = new Scanner(System.in);
//         ShippingProviderRepository shippingProviderRepository = new ShippingProviderRepositoryImpl();
//         ShippingProviderService shippingProviderService = new ShippingProviderService(shippingProviderRepository);

//         while (true) {
//             System.out.println("\n== Shipping Provider Menu ==");
//             System.out.println("1 - Criar Provider");
//             System.out.println("2 - Buscar Provider por ID");
//             System.out.println("3 - Listar todos Providers");
//             System.out.println("4 - Remover Provider por ID");
//             System.out.println("0 - Sair");
//             System.out.print("Escolha: ");

//             String choice = scanner.nextLine();

//             try {
//                 switch (choice) {
//                     case "1":
//                         System.out.print("Nome: ");
//                         String name = scanner.nextLine();

//                         System.out.print("CNPJ: ");
//                         String cnpj = scanner.nextLine();

//                         System.out.print("Preço base: ");
//                         BigDecimal basePrice = new BigDecimal(scanner.nextLine());

//                         System.out.print("Capacidade diária: ");
//                         BigDecimal dailyCapacity = new BigDecimal(scanner.nextLine());

//                         CreateShippingProviderDTO createShippingProviderDTO = CreateShippingProviderDTO.builder()
//                                 .name(name)
//                                 .cnpj(cnpj)
//                                 .basePrice(basePrice)
//                                 .dailyCapacity(dailyCapacity)
//                                 .build();

//                         shippingProviderService.registerShippingProvider(createShippingProviderDTO);
//                         System.out.println("Provider criado com sucesso!");
//                         break;

//                     case "2":
//                         System.out.print("ID do Provider: ");
//                         UUID id = UUID.fromString(scanner.nextLine());
//                         var provider = shippingProviderService.searchShippingProviderPerId(id);
//                         if (provider != null) {
//                             System.out.println("Provider encontrado: " + provider);
//                         } else {
//                             System.out.println("Provider não encontrado.");
//                         }
//                         break;

//                     case "3":
//                         var list = shippingProviderService.listAllShippingProviders();
//                         if (list.isEmpty()) {
//                             System.out.println("Nenhum provider cadastrado.");
//                         } else {
//                             list.forEach(p -> System.out.println(p));
//                         }
//                         break;

//                     case "4":
//                         System.out.print("ID do Provider para remover: ");
//                         UUID removeId = UUID.fromString(scanner.nextLine());
//                         var removed = shippingProviderService.removeShippingProvider(removeId);
//                         if (removed != null) {
//                             System.out.println("Provider removido com sucesso.");
//                         } else {
//                             System.out.println("Provider não encontrado para remoção.");
//                         }
//                         break;

//                     case "0":
//                         System.out.println("Saindo do menu de Providers.");
//                         return;

//                     default:
//                         System.out.println("Opção inválida. Tente novamente.");
//                 }
//             } catch (Exception e) {
//                 System.out.println("Erro: " + e.getMessage());
//             }
//         }
//     }

//     public static void shippingAreaFunctions() {
//         Scanner scanner = new Scanner(System.in);
//         ShippingAreaRepository shippingAreaRepository = new ShippingAreaRepositoryImpl();
//         ShippingAreaService shippingAreaService = new ShippingAreaService(shippingAreaRepository, ValidatorUtil.getValidator());
//         ShippingProviderRepository shippingProviderRepository = new ShippingProviderRepositoryImpl();

//         while (true) {
//             System.out.println("\n== Shipping Area Menu ==");
//             System.out.println("1 - Criar área");
//             System.out.println("2 - Buscar área por ID");
//             System.out.println("3 - Listar todas as áreas");
//             System.out.println("4 - Remover área por ID");
//             System.out.println("0 - Sair");
//             System.out.print("Escolha: ");

//             String choice = scanner.nextLine();

//             try {
//                 switch (choice) {
//                     case "1":
//                         System.out.print("Descrição: ");
//                         String desc = scanner.nextLine();

//                         System.out.print("ID do Shipping Provider: ");
//                         UUID providerId = UUID.fromString(scanner.nextLine());

//                         System.out.print("Estados atendidos (separados por vírgula): ");
//                         String[] estados = scanner.nextLine().split(",");

//                         var shippingProvider = shippingProviderRepository.searchShippingProviderPerId(providerId);

//                         CreateShippingAreaDTO createShippingAreaDTO = CreateShippingAreaDTO.builder()
//                                 .description(desc)
//                                 .shippingProvider(shippingProvider)
//                                 .states(Arrays.stream(estados).map(String::trim).toArray(String[]::new))
//                                 .build();

//                         shippingAreaService.saveShippingArea(createShippingAreaDTO);
//                         System.out.println("Área criada com sucesso.");
//                         break;

//                     case "2":
//                         System.out.print("ID da área: ");
//                         UUID id = UUID.fromString(scanner.nextLine());
//                         ShippingArea area = shippingAreaService.findShippingAreaById(id);
//                         System.out.println("Área encontrada: " + area);
//                         break;

//                     case "3":
//                         var list = shippingAreaService.findAllShippingAreas();
//                         list.forEach(System.out::println);
//                         break;

//                     case "4":
//                         System.out.print("ID da área para remover: ");
//                         UUID removeId = UUID.fromString(scanner.nextLine());
//                         shippingAreaService.deleteShippingAreaById(removeId);
//                         System.out.println("Área removida com sucesso.");
//                         break;

//                     case "0":
//                         System.out.println("Saindo do menu de Shipping Areas.");
//                         return;

//                     default:
//                         System.out.println("Opção inválida.");
//                 }
//             } catch (Exception e) {
//                 System.out.println("Erro: " + e.getMessage());
//             }
//         }
//     }

//     public static void storageFunctions() {
//         Scanner scanner = new Scanner(System.in);

//         StorageRepository storageRepository = new StorageRepositoryImpl();
//         ProductRepository productRepository = new ProductRepositoryImpl();
//         StorageService storageService = new StorageService(storageRepository, productRepository);

//         while (true) {
//             System.out.println("\n--- STORAGE MENU ---");
//             System.out.println("1 - Adicionar produto ao estoque");
//             System.out.println("2 - Ver quantidade em estoque por SKU");
//             System.out.println("3 - Listar todos os estoques");
//             System.out.println("0 - Voltar ao menu principal");
//             System.out.print("Opção: ");
//             String opcao = scanner.nextLine();

//             try {
//                 switch (opcao) {
//                     case "1" -> {
//                         System.out.print("SKU do produto: ");
//                         String sku = scanner.nextLine();

//                         System.out.print("Quantidade a adicionar: ");
//                         int quantity = Integer.parseInt(scanner.nextLine());

//                         storageService.createStorage(sku, quantity);
//                         System.out.println("Produto adicionado ao estoque com sucesso.");
//                     }
//                     case "2" -> {
//                         System.out.print("SKU do produto: ");
//                         String sku = scanner.nextLine();

//                         int quantity = storageService.getQuantityStock(sku);
//                         System.out.println("Quantidade em estoque: " + quantity);
//                     }
//                     case "3" -> {
//                         List<CreateStorageDTO> storages = storageService.getAllStorages();
//                         if (storages.isEmpty()) {
//                             System.out.println("Nenhum item no estoque.");
//                         } else {
//                             storages.forEach(
//                                     storage -> System.out.println("Quantidade: " + storage.getProductQuantity()));
//                         }
//                     }
//                     case "0" -> {
//                         return;
//                     }
//                     default -> System.out.println("Opção inválida.");
//                 }
//             } catch (Exception e) {
//                 System.out.println("Erro: " + e.getMessage());
//             }
//         }
//     }

//     public static void productFunctions() {
//         Scanner scanner = new Scanner(System.in);

//         ProductRepository productRepository = new ProductRepositoryImpl();
//         ProductService productService = new ProductService(productRepository, ValidatorUtil.getValidator());

//         while (true) {
//             System.out.println("\n--- PRODUCT MENU ---");
//             System.out.println("1 - Criar produto");
//             System.out.println("2 - Atualizar produto por SKU");
//             System.out.println("3 - Buscar produto por SKU");
//             System.out.println("4 - Listar todos os produtos");
//             System.out.println("0 - Voltar ao menu principal");
//             System.out.print("Opção: ");
//             String opcao = scanner.nextLine();

//             try {
//                 switch (opcao) {
//                     case "1" -> {
//                         System.out.print("SKU: ");
//                         String sku = scanner.nextLine();

//                         System.out.print("Nome: ");
//                         String name = scanner.nextLine();

//                         System.out.print("Descrição: ");
//                         String description = scanner.nextLine();

//                         System.out.print("Preço de compra: ");
//                         BigDecimal buyPrice = new BigDecimal(scanner.nextLine());

//                         System.out.print("Preço de venda: ");
//                         BigDecimal sellPrice = new BigDecimal(scanner.nextLine());

//                         CreateProductDTO createProductDTO = CreateProductDTO.builder()
//                                 .sku(sku)
//                                 .name(name)
//                                 .description(description)
//                                 .buyPrice(buyPrice)
//                                 .sellPrice(sellPrice)
//                                 .build();

//                         productService.createProduct(createProductDTO);
//                         System.out.println("Produto criado com sucesso!");
//                     }
//                     case "2" -> {
//                         System.out.print("SKU do produto a atualizar: ");
//                         String sku = scanner.nextLine();

//                         System.out.print("Nova descrição: ");
//                         String description = scanner.nextLine();

//                         System.out.print("Novo preço de compra: ");
//                         BigDecimal buyPrice = new BigDecimal(scanner.nextLine());

//                         System.out.print("Novo preço de venda: ");
//                         BigDecimal sellPrice = new BigDecimal(scanner.nextLine());

//                         UpdateProductDTO updateProductDTO = UpdateProductDTO.builder()
//                                 .description(description)
//                                 .buyPrice(buyPrice)
//                                 .sellPrice(sellPrice)
//                                 .build();

//                         productService.updateProduct(sku, updateProductDTO);
//                         System.out.println("Produto atualizado com sucesso!");
//                     }
//                     case "3" -> {
//                         System.out.print("SKU do produto: ");
//                         String sku = scanner.nextLine();

//                         Product product = productService.findProductBySKU(sku);
//                         if (product != null) {
//                             System.out.println("Produto encontrado: " + product);
//                         } else {
//                             System.out.println("Produto não encontrado.");
//                         }
//                     }
//                     case "4" -> {
//                         List<Product> products = productService.findAllProducts();
//                         if (products.isEmpty()) {
//                             System.out.println("Nenhum produto cadastrado.");
//                         } else {
//                             products.forEach(System.out::println);
//                         }
//                     }
//                     case "0" -> {
//                         return;
//                     }
//                     default -> System.out.println("Opção inválida.");
//                 }
//             } catch (Exception e) {
//                 System.out.println("Erro: " + e.getMessage());
//             }
//         }
//     }

//     public static void paymentFunctions() {
//         Scanner scanner = new Scanner(System.in);

//         PaymentRepository paymentRepository = new PaymentRepositoryImpl();
//         EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();
//         PaymentService paymentService = new PaymentService(paymentRepository, employeeRepository);

//         while (true) {
//             System.out.println("\n--- PAYMENT MENU ---");
//             System.out.println("1 - Criar pagamento");
//             System.out.println("2 - Listar todos os pagamentos");
//             System.out.println("3 - Buscar pagamento por ID");
//             System.out.println("4 - Buscar pagamentos de um funcionário");
//             System.out.println("5 - Calcular salário líquido de um pagamento");
//             System.out.println("0 - Voltar ao menu principal");
//             System.out.print("Opção: ");
//             String opcao = scanner.nextLine();

//             try {
//                 switch (opcao) {
//                     case "1" -> {
//                         System.out.print("ID do funcionário: ");
//                         UUID employeeId = UUID.fromString(scanner.nextLine());
//                         Employee employee = paymentService.findEmployeeById(employeeId);

//                         System.out.print("Salário bruto: ");
//                         BigDecimal grossIncome = new BigDecimal(scanner.nextLine());

//                         System.out.print("Impostos: ");
//                         BigDecimal taxes = new BigDecimal(scanner.nextLine());

//                         System.out.print("Vale-refeição: ");
//                         BigDecimal mealVoucher = new BigDecimal(scanner.nextLine());

//                         System.out.print("Vale-alimentação: ");
//                         BigDecimal foodVoucher = new BigDecimal(scanner.nextLine());

//                         System.out.print("Plano de saúde: ");
//                         BigDecimal health = new BigDecimal(scanner.nextLine());

//                         System.out.print("Plano odontológico: ");
//                         BigDecimal dental = new BigDecimal(scanner.nextLine());

//                         System.out.print("Participação nos lucros: ");
//                         BigDecimal profitSharing = new BigDecimal(scanner.nextLine());

//                         CreatePaymentDTO dto = new CreatePaymentDTO(employee, taxes, grossIncome,
//                                 mealVoucher, foodVoucher, health, dental, profitSharing);

//                         Payment payment = paymentService.createPayment(dto);

//                         if (payment != null) {
//                             System.out.println("Pagamento criado com sucesso!");
//                             System.out.println(payment);
//                         }
//                     }
//                     case "2" -> {
//                         List<Payment> payments = paymentService.getAllPayments();
//                         if (payments.isEmpty()) {
//                             System.out.println("Nenhum pagamento registrado.");
//                         } else {
//                             payments.forEach(System.out::println);
//                         }
//                     }
//                     case "3" -> {
//                         System.out.print("ID do pagamento: ");
//                         UUID id = UUID.fromString(scanner.nextLine());
//                         Optional<Payment> payment = paymentService.findById(id);
//                         payment.ifPresentOrElse(
//                                 System.out::println,
//                                 () -> System.out.println("Pagamento não encontrado."));
//                     }
//                     case "4" -> {
//                         System.out.print("ID do funcionário: ");
//                         UUID empId = UUID.fromString(scanner.nextLine());
//                         Employee employee = paymentService.findEmployeeById(empId);
//                         List<Payment> employeePayments = paymentService.findPaymentsByEmployee(employee);
//                         if (employeePayments.isEmpty()) {
//                             System.out.println("Nenhum pagamento encontrado para o funcionário.");
//                         } else {
//                             employeePayments.forEach(System.out::println);
//                         }
//                     }
//                     case "5" -> {
//                         System.out.print("ID do pagamento: ");
//                         UUID paymentId = UUID.fromString(scanner.nextLine());
//                         Optional<Payment> payment = paymentService.findById(paymentId);
//                         if (payment.isPresent()) {
//                             BigDecimal net = paymentService.calculateNetIncome(payment.get());
//                             System.out.println("Salário líquido: R$ " + net);
//                         } else {
//                             System.out.println("Pagamento não encontrado.");
//                         }
//                     }
//                     case "0" -> {
//                         return;
//                     }
//                     default -> System.out.println("Opção inválida.");
//                 }
//             } catch (Exception e) {
//                 System.out.println("Erro: " + e.getMessage());
//             }
//         }
//     }

//     public static void employeeFunctions() {
//         EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();
//         EmployeeService employeeService = new EmployeeService(employeeRepository, ValidatorUtil.getValidator());
//         Scanner scanner = new Scanner(System.in);

//         while (true) {
//             System.out.println("\n--- EMPLOYEE MENU ---");
//             System.out.println("1 - Criar funcionário");
//             System.out.println("2 - Listar todos os funcionários");
//             System.out.println("3 - Buscar funcionário por ID");
//             System.out.println("4 - Deletar funcionário por ID");
//             System.out.println("0 - Voltar ao menu principal");
//             System.out.print("Opção: ");

//             String opcao = scanner.nextLine();

//             try {
//                 switch (opcao) {
//                     case "1" -> {
//                         System.out.print("Nome completo: ");
//                         String nome = scanner.nextLine();

//                         System.out.print("Cargo: ");
//                         String roleInput = scanner.nextLine().toUpperCase();
//                         EmployeeRole role = EmployeeRole.valueOf(roleInput);

//                         // Exibir opções de gênero
//                         System.out.println("Gêneros disponíveis:");
//                         for (EmployeeGender gender : EmployeeGender.values()) {
//                             System.out.println("- " + gender);
//                         }
//                         System.out.print("Gênero: ");
//                         String genderInput = scanner.nextLine().toUpperCase();
//                         EmployeeGender gender = EmployeeGender.valueOf(genderInput);

//                         System.out.print("Data de nascimento (AAAA-MM-DD): ");
//                         String birthDateInput = scanner.nextLine();
//                         LocalDate birthDate = LocalDate.parse(birthDateInput);

//                         CreateEmployeeDTO createEmployeeDTO = new CreateEmployeeDTO(nome, birthDate, gender, role);
//                         employeeService.createEmployee(createEmployeeDTO);
//                         System.out.println("Funcionário criado com sucesso!");
//                     }
//                     case "2" -> {
//                         List<Employee> employees = employeeService.employeeList();
//                         if (employees.isEmpty()) {
//                             System.out.println("Nenhum funcionário encontrado.");
//                         } else {
//                             employees.forEach(System.out::println);
//                         }
//                     }
//                     case "3" -> {
//                         System.out.print("ID do funcionário: ");
//                         String idInput = scanner.nextLine();
//                         UUID id = UUID.fromString(idInput);
//                         employeeService.findEmployeeById(id);
//                     }
//                     case "4" -> {
//                         System.out.print("ID do funcionário para deletar: ");
//                         String idInput = scanner.nextLine();
//                         UUID id = UUID.fromString(idInput);
//                         employeeService.deleteEmployeeByID(id);
//                         System.out.println("Funcionário deletado (se existia).");
//                     }
//                     case "0" -> {
//                         return;
//                     }
//                     default -> System.out.println("Opção inválida.");
//                 }
//             } catch (Exception e) {
//                 System.out.println("Erro: " + e.getMessage());
//             }
//         }
//     }

//     public static void orderFunctions() {
//         StorageRepository storageRepository = new StorageRepositoryImpl();
//         ProductRepository productRepository = new ProductRepositoryImpl();
//         ShippingProviderRepository shippingProviderRepository = new ShippingProviderRepositoryImpl();
//         OrderRepository orderRepository = new OrderRepositoryImpl();
//         EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();
//         OrderService orderService = new OrderService(orderRepository, employeeRepository, storageRepository,
//                 ValidatorUtil.getValidator());
//         Scanner scanner = new Scanner(System.in);

//         while (true) {
//             System.out.println("\n--- ORDER MENU ---");
//             System.out.println("1 - Criar pedido");
//             System.out.println("2 - Buscar pedido por ID");
//             System.out.println("3 - Cancelar pedido");
//             System.out.println("0 - Voltar ao menu principal");
//             System.out.print("Opção: ");

//             String opcao = scanner.nextLine();

//             try {
//                 switch (opcao) {
//                     case "1" -> {
//                         System.out.print("ID do vendedor (UUID): ");
//                         UUID sellerId = UUID.fromString(scanner.nextLine());

//                         System.out.print("Descrição do pedido (opcional): ");
//                         String description = scanner.nextLine();

//                         // Captura dos itens do pedido
//                         List<CreateOrderDTO.CreateOrderItemDTO> items = new ArrayList<>();
//                         while (true) {
//                             System.out.print("Adicionar item? (s/n): ");
//                             String resp = scanner.nextLine().trim().toLowerCase();
//                             if (!resp.equals("s"))
//                                 break;

//                             // Aqui você pode adaptar para buscar o produto no sistema pelo ID ou nome
//                             System.out.print("Sku do Produto:");
//                             String productSku = scanner.nextLine();
//                             // Supondo que você tenha um método para buscar o produto pelo ID
//                             Product product = productRepository.findBySKU(productSku);
//                             if (product == null) {
//                                 System.out.println("Produto não encontrado, tente novamente.");
//                                 continue;
//                             }

//                             System.out.print("Quantidade: ");
//                             int quantity = Integer.parseInt(scanner.nextLine());

//                             CreateOrderDTO.CreateOrderItemDTO itemDTO = CreateOrderDTO.CreateOrderItemDTO.builder()
//                                     .product(product)
//                                     .quantity(quantity)
//                                     .build();

//                             items.add(itemDTO);
//                         }

//                         // Selecionar ShippingProvider
//                         System.out.println("Transportadoras disponíveis:");
//                         List<ShippingProvider> providers = shippingProviderRepository.listAllShippingProviders();
//                         for (int i = 0; i < providers.size(); i++) {
//                             System.out.printf("%d - %s%n", i + 1, providers.get(i).getName());
//                         }
//                         System.out.print("Escolha a transportadora (número): ");
//                         int providerIndex = Integer.parseInt(scanner.nextLine()) - 1;
//                         if (providerIndex < 0 || providerIndex >= providers.size()) {
//                             System.out.println("Transportadora inválida.");
//                             continue;
//                         }
//                         ShippingProvider shippingProvider = providers.get(providerIndex);

//                         CreateOrderDTO createOrderDTO = CreateOrderDTO.builder()
//                                 .sellerId(sellerId)
//                                 .description(description)
//                                 .item(items)
//                                 .shippingProvider(shippingProvider)
//                                 .build();

//                         orderService.createOrder(createOrderDTO);
//                         System.out.println("Pedido criado com sucesso!");
//                     }
//                     case "2" -> {
//                         System.out.print("ID do pedido: ");
//                         UUID orderId = UUID.fromString(scanner.nextLine());

//                         Order order = orderService.findOrderById(orderId);
//                         if (order == null) {
//                             System.out.println("Pedido não encontrado.");
//                         } else {
//                             System.out.println(order);
//                         }
//                     }
//                     case "3" -> {
//                         System.out.print("ID do pedido para cancelar: ");
//                         UUID orderId = UUID.fromString(scanner.nextLine());

//                         orderService.cancelOrder(orderId);
//                         System.out.println("Pedido cancelado (se existia).");
//                     }
//                     case "0" -> {
//                         return;
//                     }
//                     default -> System.out.println("Opção inválida.");
//                 }
//             } catch (Exception e) {
//                 System.out.println("Erro: " + e.getMessage());
//             }
//         }
//     }

//     public static void purchaseOrderFunctions() {
//         StorageRepository storageRepository = new StorageRepositoryImpl();
//         ProductRepository productRepository = new ProductRepositoryImpl();
//         EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();
//         PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepositoryImpl();
//         PurchaseOrderService purchaseOrderService = new PurchaseOrderService(purchaseOrderRepository,
//                 employeeRepository, storageRepository, ValidatorUtil.getValidator());
//         Scanner scanner = new Scanner(System.in);

//         while (true) {
//             System.out.println("\n--- PURCHASE ORDER MENU ---");
//             System.out.println("1 - Criar pedido de compra");
//             System.out.println("2 - Buscar pedido de compra por ID");
//             System.out.println("0 - Voltar ao menu principal");
//             System.out.print("Opção: ");

//             String opcao = scanner.nextLine();

//             try {
//                 switch (opcao) {
//                     case "1" -> {
//                         System.out.print("ID do comprador (UUID - deve ser STORAGE ou LOCAL_MANAGER): ");
//                         UUID purchaserId = UUID.fromString(scanner.nextLine());

//                         // Captura dos itens do pedido de compra
//                         List<CreatePurchaseOrderDTO.CreatePurchaseOrderItemDTO> items = new ArrayList<>();
//                         while (true) {
//                             System.out.print("Adicionar item? (s/n): ");
//                             String resp = scanner.nextLine().trim().toLowerCase();
//                             if (!resp.equals("s"))
//                                 break;

//                             System.out.print("SKU do Produto: ");
//                             String productSku = scanner.nextLine();
//                             Product product = productRepository.findBySKU(productSku);
//                             if (product == null) {
//                                 System.out.println("Produto não encontrado, tente novamente.");
//                                 continue;
//                             }

//                             System.out.print("Quantidade: ");
//                             int quantity = Integer.parseInt(scanner.nextLine());

//                             System.out.print("Preço unitário: ");
//                             BigDecimal price = new BigDecimal(scanner.nextLine());

//                             CreatePurchaseOrderDTO.CreatePurchaseOrderItemDTO itemDTO = CreatePurchaseOrderDTO.CreatePurchaseOrderItemDTO
//                                     .builder()
//                                     .product(product)
//                                     .quantity(quantity)
//                                     .price(price)
//                                     .build();

//                             items.add(itemDTO);
//                         }

//                         CreatePurchaseOrderDTO createPurchaseOrderDTO = CreatePurchaseOrderDTO.builder()
//                                 .purchaserId(purchaserId)
//                                 .item(items)
//                                 .build();

//                         purchaseOrderService.createPurchaseOrder(createPurchaseOrderDTO);
//                         System.out.println("Pedido de compra criado com sucesso!");
//                     }
//                     case "2" -> {
//                         System.out.print("ID do pedido de compra: ");
//                         UUID orderId = UUID.fromString(scanner.nextLine());

//                         // PurchaseOrder order = purchaseOrderService.findPurchaseOrderById(orderId);
//                         // if (order == null) {
//                         //     System.out.println("Pedido de compra não encontrado.");
//                         // } else {
//                         //     System.out.println(order);
//                         // }
//                     }
//                     case "0" -> {
//                         return;
//                     }
//                     default -> System.out.println("Opção inválida.");
//                 }
//             } catch (Exception e) {
//                 System.out.println("Erro: " + e.getMessage());
//             }
//         }
//     }

//     public static void main(String[] args) {

//         Scanner scanner = new Scanner(System.in);

//         while (true) {
//             System.out.println("EMPLOYEE: 1");
//             System.out.println("ORDER: 2");
//             System.out.println("PAYMENT: 3");
//             System.out.println("PRODUCT: 4");
//             System.out.println("SHIPPING AREA: 5");
//             System.out.println("SHIPPING PROVIDER: 6");
//             System.out.println("STORAGE: 7");
//             System.out.println("SHIPPING ORDER: 8");
//             System.out.println("PURCHASE ORDER: 9");
//             System.out.println("ENCERRAR: 0");

//             String opcao = scanner.nextLine();

//             switch (opcao) {
//                 case "1" -> employeeFunctions();
//                 case "2" -> orderFunctions();
//                 case "3" -> paymentFunctions();
//                 case "4" -> productFunctions();
//                 case "5" -> shippingAreaFunctions();
//                 case "6" -> shippingProviderFunctions();
//                 case "7" -> storageFunctions();
//                 case "8" -> shippingOrderFunctions();
//                 case "9" -> purchaseOrderFunctions();
//                 case "0" -> {
//                     System.out.println("Encerrando...");
//                     return;
//                 }
//             }

//         }
//     }
// }
