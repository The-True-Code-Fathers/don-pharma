package com.codefathers.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.codefathers.exceptions.BusinessRuleException;
import com.codefathers.model.dto.CreateOrderDTO;
import com.codefathers.model.dto.CreateOrderItemDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Order;
import com.codefathers.model.entity.OrderItem;
import com.codefathers.model.entity.Product;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.model.enums.OrderStatus;
import com.codefathers.repository.implementations.EmployeeRepositoryImpl;
import com.codefathers.repository.implementations.OrderItemRepositoryImpl;
import com.codefathers.repository.implementations.OrderRepositoryImpl;
import com.codefathers.repository.implementations.ProductRepositoryImpl;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.repository.implementations.StorageRepositoryImpl;
import com.codefathers.repository.interfaces.EmployeeRepository;
import com.codefathers.service.EmployeeService;
import com.codefathers.service.OrderItemService;
import com.codefathers.service.OrderService;
import com.codefathers.service.ProductService;
import com.codefathers.service.ShippingProviderService;
import com.codefathers.util.ValidatorUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.Route;

import jakarta.validation.Validator;

@Route("order")
public class OrderView extends VerticalLayout {
    private ShippingProviderService shippingProviderService;
    ProductRepositoryImpl productRepository = new ProductRepositoryImpl();
    ShippingProviderRepositoryImpl shippingProviderRepository = new ShippingProviderRepositoryImpl();
    OrderItemRepositoryImpl orderItemRepository = new OrderItemRepositoryImpl();
    private ProductService productService;
    private EmployeeService employeeService;
    private OrderService orderService;
    private OrderItemService orderItemService;
    private Grid<Order> grid = new Grid<>(Order.class, false);
    private GridLazyDataView<Order> dataView;
    private Select<OrderStatus> orderStatusSelect;
    private OrderRepositoryImpl orderRepository;
    private OrderStatus currentStatusFilter = null;
    private TextField searchField = new TextField();
    private Select<String> statusFilter = new Select<>();
    private ComboBox<Employee> employeeComboBox;
    private Dialog dialog = new Dialog();
    private Dialog editDialog = new Dialog();
    private String currentSearchingTerm = "";
    private String currentStatus = "TODOS";
    private Order currentOrderEditing = null;
    private Employee currentEmployeeFilter = null;
    private Employee allEmployee;

    public OrderView() {
        productService = new ProductService(productRepository, ValidatorUtil.getValidator());
        orderItemService = new OrderItemService(orderItemRepository, orderRepository);
        shippingProviderService = new ShippingProviderService(shippingProviderRepository);
        EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();
        employeeService = new EmployeeService(employeeRepository, ValidatorUtil.getValidator());
        var orderRepository = new OrderRepositoryImpl();
        var storageRepository = new StorageRepositoryImpl();
        Validator validator = ValidatorUtil.getValidator();
        this.orderService = new OrderService(orderRepository, employeeRepository, storageRepository, validator);

        employeeComboBox = new ComboBox<>();
        setupEmployeeComboBox();
        searchStatusFilter();
        setupGrid();
        setupDialog();
        setupEditDialog();

        Button createButton = new Button("Criar Pedido", new Icon(VaadinIcon.PLUS));
        createButton.addClickListener(e -> openCreateOrderDialog());

        HorizontalLayout filters = new HorizontalLayout(employeeComboBox, statusFilter);
        filters.setAlignItems(Alignment.CENTER);
        filters.setSpacing(true);

        HorizontalLayout topLayout = new HorizontalLayout(createButton, filters);
        topLayout.setWidthFull();
        topLayout.setAlignItems(Alignment.CENTER);
        topLayout.expand(filters);

        add(topLayout, grid);

        setupLazyDataProvider();
    }

    private void setupGrid() {
        grid.setHeight("400px");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.addColumn(Order::getId)
                .setHeader("ID do Pedido")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(order -> order.getSeller() != null ? order.getSeller().getFullName() : "N/A")
                .setHeader("Vendedor")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(order -> {
            if (order.getCreatedAt() != null) {
                return order.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
            return "N/A";
        })
                .setHeader("Criado em")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(order -> {
            String description = order.getDescription();
            if (description != null && !description.trim().isEmpty()) {
                // Limita a descrição a 50 caracteres para não ocupar muito espaço
                return description.length() > 50 ? description.substring(0, 47) + "..." : description;
            }
            return "Sem descrição";
        })
                .setHeader("Descrição")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(order -> {
            BigDecimal totalAmount = order.getTotalAmount();
            if (totalAmount != null) {
                return "R$ " + String.format("%.2f", totalAmount);
            }
            return "R$ 0,00";
        })
                .setHeader("Valor Total")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(order -> {
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                int totalQuantity = order.getItems().stream()
                        .mapToInt(OrderItem::getQuantity)
                        .sum();
                return String.valueOf(totalQuantity);
            }
            return "0";
        })
                .setHeader("Qtd. Itens")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(order -> {
            if (order.getShippingProvider() != null) {
                return order.getShippingProvider().getName();
            }
            return "N/A";
        })
                .setHeader("Transportadora")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(Order::getOrderStatus)
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addItemDoubleClickListener(event -> {
            Order selectedOrder = event.getItem();
            showOrderDetails(selectedOrder);
        });
    }

    private void setupEditDialog() {
        editDialog.setHeaderTitle("Editar Pedido");
        editDialog.setResizable(true);
        editDialog.setDraggable(true);
        editDialog.setWidth("800px");
        editDialog.setHeight("600px");
    }

    // Substitua o método openEditDialog existente por este código atualizado:

    private void openEditDialog(Order order) {
        editDialog.removeAll(); // Limpa o conteúdo anterior

        this.currentOrderEditing = order; // Define o pedido atual sendo editado

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        // Formulário de edição do pedido
        FormLayout orderEditLayout = new FormLayout();
        orderEditLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        TextField idField = new TextField("ID do Pedido");
        idField.setValue(order.getId().toString());
        idField.setReadOnly(true);

        Select<OrderStatus> statusSelect = new Select<>();
        statusSelect.setLabel("Status");
        statusSelect.setItems(OrderStatus.values());
        statusSelect.setValue(order.getOrderStatus());

        // Só permite editar para OPEN se não estiver CANCELLED ou INVOICED
        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.INVOICED) {
            statusSelect.setEnabled(false);
        }

        ComboBox<Employee> sellerComboBox = new ComboBox<>("Vendedor");
        setupSellerComboBox(sellerComboBox);
        sellerComboBox.setValue(order.getSeller());

        TextField descriptionField = new TextField("Descrição");
        descriptionField.setValue(order.getDescription() != null ? order.getDescription() : "");
        descriptionField.setWidthFull();

        orderEditLayout.add(idField, statusSelect, sellerComboBox, descriptionField);

        // Grid de itens (somente visualização - botão de remover removido)
        Grid<OrderItem> itemsGrid = new Grid<>(OrderItem.class, false);
        itemsGrid.addColumn(item -> item.getProduct() != null ? item.getProduct().getName() : "N/A")
                .setHeader("Produto").setAutoWidth(true);
        itemsGrid.addColumn(item -> item.getProduct() != null ? item.getProduct().getSku() : "N/A")
                .setHeader("SKU").setAutoWidth(true);
        itemsGrid.addColumn(OrderItem::getQuantity)
                .setHeader("Quantidade").setAutoWidth(true);
        itemsGrid
                .addColumn(item -> item.getPrice() != null ? "R$ " + String.format("%.2f", item.getPrice()) : "R$ 0,00")
                .setHeader("Preço Unitário").setAutoWidth(true);
        itemsGrid.addColumn(item -> {
            if (item.getPrice() != null) {
                BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                return "R$ " + String.format("%.2f", total);
            }
            return "R$ 0,00";
        }).setHeader("Total").setAutoWidth(true);

        // Removido o botão de remover item conforme solicitado

        if (order.getItems() != null) {
            itemsGrid.setItems(order.getItems());
        }
        itemsGrid.setHeight("250px");

        // Calcular e mostrar o total
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getItems() != null) {
            totalAmount = order.getItems().stream()
                    .filter(item -> item.getPrice() != null)
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Botões de ação
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();

        Button saveButton = new Button("Salvar", new Icon(VaadinIcon.CHECK));
        saveButton.addClickListener(e -> saveOrderEdit(statusSelect, sellerComboBox, descriptionField));

        Button cancelButton = new Button("Cancelar");
        cancelButton.addClickListener(e -> editDialog.close());

        buttonLayout.add(saveButton, cancelButton);

        mainLayout.add(
                new com.vaadin.flow.component.html.H4("Editar Pedido"),
                orderEditLayout,
                new com.vaadin.flow.component.html.H4("Itens do Pedido"),
                itemsGrid,
                buttonLayout);

        editDialog.add(mainLayout);
        editDialog.open();
    }

    // Novo método para salvar a edição do pedido
    private void saveOrderEdit(Select<OrderStatus> statusSelect, ComboBox<Employee> sellerComboBox,
            TextField descriptionField) {
        try {
            // Validar se há um pedido sendo editado
            if (currentOrderEditing == null) {
                Notification.show("Erro: Nenhum pedido selecionado para edição", 5000, Notification.Position.MIDDLE);
                return;
            }

            // Validar campos obrigatórios
            if (sellerComboBox.getValue() == null) {
                Notification.show("Selecione um vendedor", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (statusSelect.getValue() == null) {
                Notification.show("Selecione um status", 3000, Notification.Position.MIDDLE);
                return;
            }

            // Usar o método updateOrder do OrderService
            orderService.updateOrder(
                    currentOrderEditing.getId(),
                    statusSelect.getValue(),
                    sellerComboBox.getValue(),
                    descriptionField.getValue());

            Notification.show("Pedido atualizado com sucesso!", 3000, Notification.Position.MIDDLE);

            // Atualizar a grid e fechar o dialog
            refreshGrid();
            editDialog.close();

            // Limpar a referência do pedido atual
            currentOrderEditing = null;

        } catch (BusinessRuleException ex) {
            Notification.show("Erro de regra de negócio: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (IllegalArgumentException ex) {
            Notification.show("Erro: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (Exception ex) {
            System.err.println("Erro ao salvar pedido: " + ex.getMessage());
            ex.printStackTrace();
            Notification.show("Erro inesperado ao salvar: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void refreshGrid() {
        dataView.refreshAll();
        grid.getDataProvider().refreshAll();
    }

    private void setupEmployeeComboBox() {
        allEmployee = createTodosEmployee();

        employeeComboBox.setItemLabelGenerator(employee -> {
            if (employee == allEmployee)
                return "TODOS";
            return employee.getFullName();
        });
        employeeComboBox.setAllowCustomValue(false);
        employeeComboBox.setPageSize(10);

        List<Employee> allEmployees = new ArrayList<>();
        allEmployees.add(allEmployee); // Adiciona o item "TODOS"

        List<Employee> sellers = employeeService.employeeList().stream()
                .filter(emp -> emp.getRole() == EmployeeRole.SALES || emp.getRole() == EmployeeRole.LOCAL_MANAGER)
                .collect(Collectors.toList());

        allEmployees.addAll(sellers);

        employeeComboBox.setItems(query -> {
            String filter = query.getFilter().orElse("");
            return allEmployees.stream()
                    .filter(emp -> {
                        if (emp == allEmployee)
                            return "todos".contains(filter.toLowerCase());
                        return emp.getFullName().toLowerCase().contains(filter.toLowerCase());
                    })
                    .skip(query.getOffset())
                    .limit(query.getLimit());
        });

        employeeComboBox.setValue(allEmployee);
        currentEmployeeFilter = allEmployee;

        employeeComboBox.addValueChangeListener(e -> {
            currentEmployeeFilter = e.getValue();
            dataView.refreshAll();
        });
    }

    private void searchStatusFilter() {
        List<String> statusItems = List.of("TODOS", "OPEN", "CANCELLED", "INVOICED");

        statusFilter.setItems(statusItems);
        statusFilter.setValue("TODOS");
        statusFilter.setEmptySelectionAllowed(false); // desativa seleção vazia
        statusFilter.setPlaceholder("Selecione um status");

        statusFilter.addValueChangeListener(e -> {
            currentStatus = e.getValue();
            dataView.refreshAll();
        });
    }

    private void setupLazyDataProvider() {
        CallbackDataProvider<Order, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    List<Order> allOrders = orderService.findAll();
                    System.out.println("Total de pedidos encontrados: " + allOrders.size());
                    return allOrders.stream()
                            .filter(this::matchesFilters)
                            .skip(query.getOffset())
                            .limit(query.getLimit());
                },
                query -> {
                    List<Order> allOrders = orderService.findAll();
                    return (int) allOrders.stream().filter(this::matchesFilters).count();
                });

        dataView = grid.setItems(dataProvider);
    }

    private boolean matchesFilters(Order order) {
        if (!currentStatus.equals("TODOS") && !order.getOrderStatus().name().equalsIgnoreCase(currentStatus)) {
            return false;
        }

        if (currentEmployeeFilter != null && currentEmployeeFilter != allEmployee) {
            if (order.getSeller() == null || !order.getSeller().getId().equals(currentEmployeeFilter.getId())) {
                return false;
            }
        }

        if (!currentSearchingTerm.isEmpty()) {
            String sellerName = order.getSeller() != null ? order.getSeller().getFullName().toLowerCase() : "";
            return sellerName.contains(currentSearchingTerm.toLowerCase());
        }

        return true;
    }

    private void setupDialog() {
        dialog.setHeaderTitle("Detalhes do Pedido");
        dialog.setResizable(true);
        dialog.setDraggable(true);
        dialog.setWidth("400px");
        dialog.setHeight("200px");
    }

private void openCreateOrderDialog() {
    Dialog createDialog = new Dialog();
    createDialog.setHeaderTitle("Criar Novo Pedido");
    createDialog.setWidth("900px"); // Aumentado para acomodar melhor o grid
    createDialog.setHeight("800px");
    createDialog.setResizable(true);

    ComboBox<Employee> sellerComboBox = new ComboBox<>("Vendedor");
    ComboBox<ShippingProvider> shippingComboBox = new ComboBox<>("Transportadora");
    TextField descriptionField = new TextField("Descrição");

    Grid<OrderItemRow> itemsGrid = new Grid<>();
    List<OrderItemRow> orderItems = new ArrayList<>();

    ComboBox<Product> productComboBox = new ComboBox<>("Produto");
    IntegerField quantityField = new IntegerField("Quantidade");
    
    // Campo de preço melhorado com prefixo R$ e sem step buttons
    NumberField priceField = new NumberField("Preço");
    priceField.setPrefixComponent(new Span("R$"));
    priceField.setStepButtonsVisible(false); // Remove os botões de incremento/decremento
    
    Button addItemButton = new Button("Adicionar Item");

    setupSellerComboBox(sellerComboBox);
    setupShippingComboBox(shippingComboBox);
    setupProductComboBox(productComboBox);

    descriptionField.setWidthFull();
    descriptionField.setMaxLength(2048);

    quantityField.setValue(1);
    quantityField.setMin(1);
    quantityField.setStepButtonsVisible(true);

    priceField.setValue(1.0);
    priceField.setMin(0.01);
    priceField.setStep(0.01);


    setupItemsGridImproved(itemsGrid, orderItems); // Passa o callback

    addItemButton.addClickListener(e -> {
        Product selectedProduct = productComboBox.getValue();
        Integer quantity = quantityField.getValue();
        Double priceDouble = priceField.getValue();
        if (priceDouble == null || priceDouble <= 0) {
            Notification.show("Informe um preço válido", 3000, Notification.Position.MIDDLE);
            return;
        }
        BigDecimal price = BigDecimal.valueOf(priceDouble);

        if (selectedProduct == null) {
            Notification.show("Selecione um produto", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (quantity == null || quantity < 1) {
            Notification.show("Informe uma quantidade válida", 3000, Notification.Position.MIDDLE);
            return;
        }

        boolean exists = orderItems.stream()
                .anyMatch(item -> item.getProduct().getSku().equals(selectedProduct.getSku()));

        if (exists) {
            Notification.show("Produto já adicionado ao pedido", 3000, Notification.Position.MIDDLE);
            return;
        }

        OrderItemRow newItem = new OrderItemRow(selectedProduct, quantity, price);
        orderItems.add(newItem);
        itemsGrid.getDataProvider().refreshAll();

        productComboBox.clear();
        quantityField.setValue(1);
        priceField.setValue(1.0);

        Notification.show("Item adicionado!", 2000, Notification.Position.MIDDLE);
    });

    Button saveButton = new Button("Criar Pedido", e -> {
        try {
            if (sellerComboBox.getValue() == null) {
                Notification.show("Selecione um vendedor", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (shippingComboBox.getValue() == null) {
                Notification.show("Selecione uma transportadora", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (orderItems.isEmpty()) {
                Notification.show("Adicione pelo menos um item ao pedido", 3000, Notification.Position.MIDDLE);
                return;
            }

            CreateOrderDTO createOrderDTO = buildCreateOrderDTO(
                    sellerComboBox.getValue(),
                    shippingComboBox.getValue(),
                    descriptionField.getValue(),
                    orderItems);

            orderService.createOrder(createOrderDTO);

            Notification.show("Pedido criado com sucesso!", 3000, Notification.Position.MIDDLE);
            dataView.refreshAll();
            grid.getDataProvider().refreshAll();
            createDialog.close();

        } catch (Exception ex) {
            System.out.println("Erro ao criar pedido: " + ex.getMessage());
            ex.printStackTrace();
            Notification.show("Erro ao criar pedido: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    });

    Button cancelButton = new Button("Cancelar", e -> createDialog.close());

    HorizontalLayout addItemLayout = new HorizontalLayout(productComboBox, quantityField, priceField,
            addItemButton);
    addItemLayout.setAlignItems(Alignment.END);
    addItemLayout.setWidthFull();
    productComboBox.setWidth("300px");
    quantityField.setWidth("100px");
    priceField.setWidth("120px"); // Aumentado um pouco para acomodar o prefixo R$

    VerticalLayout mainLayout = new VerticalLayout();
    mainLayout.add(
            sellerComboBox,
            shippingComboBox,
            descriptionField,
            new com.vaadin.flow.component.html.H4("Itens do Pedido"),
            addItemLayout,
            itemsGrid,
            new HorizontalLayout(saveButton, cancelButton));
    mainLayout.setSpacing(true);
    mainLayout.setPadding(true);

    createDialog.add(mainLayout);
    createDialog.open();
}

// Método melhorado para o grid de itens
private void setupItemsGridImproved(Grid<OrderItemRow> grid, List<OrderItemRow> items) {
    grid.setItems(items);
    grid.setHeight("250px");
    grid.setWidthFull();

    // Produto - largura fixa controlada para não ocupar muito espaço
    grid.addColumn(item -> {
        String productName = item.getProduct().getName();
        // Trunca o nome do produto se for muito longo
        return productName.length() > 25 ? productName.substring(0, 22) + "..." : productName;
    })
        .setHeader("Produto")
        .setWidth("200px")
        .setFlexGrow(0);

    // SKU - largura otimizada
    grid.addColumn(item -> item.getProduct().getSku())
        .setHeader("SKU")
        .setWidth("150px")
        .setFlexGrow(0);

    // Quantidade - largura otimizada
    grid.addColumn(OrderItemRow::getQuantity)
        .setHeader("Qtd")
        .setWidth("70px")
        .setFlexGrow(0);

    // Preço unitário - largura adequada para valores monetários
    grid.addColumn(item -> "R$ " + String.format("%.2f", item.getPrice()))
        .setHeader("Preço Unit.")
        .setWidth("120px")
        .setFlexGrow(0);

    // Total - largura adequada para valores monetários (SEMPRE VISÍVEL)
    grid.addColumn(item -> "R$ " + String.format("%.2f", item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
        .setHeader("Total")
        .setWidth("130px")
        .setFlexGrow(0);

    // Ações - largura mínima para o botão
    grid.addComponentColumn(item -> {
        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
        removeButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR);
        removeButton.addClickListener(e -> {
            items.remove(item);
            grid.getDataProvider().refreshAll();
            Notification.show("Item removido", 2000, Notification.Position.MIDDLE);
        });
        return removeButton;
    })
    .setHeader("Ações")
    .setWidth("80px")
    .setFlexGrow(0);
}


    private void setupSellerComboBox(ComboBox<Employee> sellerComboBox) {
        sellerComboBox.setItemLabelGenerator(Employee::getFullName);
        sellerComboBox.setAllowCustomValue(false);
        sellerComboBox.setWidthFull();

        List<Employee> sellers = employeeService.employeeList().stream()
                .filter(emp -> emp.getRole() == EmployeeRole.SALES || emp.getRole() == EmployeeRole.LOCAL_MANAGER)
                .collect(Collectors.toList());

        sellerComboBox.setItems(sellers);
        sellerComboBox.setPlaceholder("Selecione um vendedor");
    }

    private CreateOrderDTO buildCreateOrderDTO(Employee seller, ShippingProvider shippingProvider,
            String description, List<OrderItemRow> items) {

        List<CreateOrderItemDTO> itemDTOs = items.stream()
                .map(item -> CreateOrderItemDTO.builder()
                        .product(item.getProduct())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return CreateOrderDTO.builder()
                .sellerId(seller.getId())
                .description(description)
                .shippingProvider(shippingProvider)
                .item(itemDTOs)
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }

    private void setupItemsGrid(Grid<OrderItemRow> grid, List<OrderItemRow> items) {
        grid.setItems(items);
        grid.setHeight("200px");

        grid.addColumn(item -> item.getProduct().getName()).setHeader("Produto").setAutoWidth(true);
        grid.addColumn(item -> item.getProduct().getSku()).setHeader("SKU").setAutoWidth(true);
        grid.addColumn(OrderItemRow::getQuantity).setHeader("Quantidade").setAutoWidth(true);
        grid.addColumn(item -> "R$ " + String.format("%.2f", item.getPrice())).setHeader("Preço Unit.")
                .setAutoWidth(true);
        grid.addColumn(
                item -> "R$ " + String.format("%.2f", item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .setHeader("Total").setAutoWidth(true);

        grid.addComponentColumn(item -> {
            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
            removeButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR);
            removeButton.addClickListener(e -> {
                items.remove(item);
                grid.getDataProvider().refreshAll();
                Notification.show("Item removido", 2000, Notification.Position.MIDDLE);
            });
            return removeButton;
        }).setHeader("Ações").setAutoWidth(true);
    }

    private void setupShippingComboBox(ComboBox<ShippingProvider> shippingComboBox) {
        shippingComboBox.setItemLabelGenerator(provider -> provider.getName());
        shippingComboBox.setAllowCustomValue(false);
        shippingComboBox.setWidthFull();
        shippingComboBox.setPlaceholder("Selecione uma transportadora");

        List<ShippingProvider> providers = shippingProviderService.listAllShippingProviders();
        shippingComboBox.setItems(providers);
    }

    private void setupProductComboBox(ComboBox<Product> productComboBox) {
        productComboBox.setItemLabelGenerator(product -> product.getName() + " (" + product.getSku() + ")");
        productComboBox.setAllowCustomValue(false);
        productComboBox.setPlaceholder("Selecione um produto");

        List<Product> products = productService.findAllProducts();
        productComboBox.setItems(products);
    }

    private static class OrderItemRow {
        private Product product;
        private int quantity;
        private BigDecimal price;

        public OrderItemRow(Product product, int quantity, BigDecimal price) {
            this.product = product;
            this.quantity = quantity;
            this.price = price;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

    }

    private void showOrderDetails(Order order) {
        Dialog detailsDialog = new Dialog();
        detailsDialog.setHeaderTitle("Detalhes do Pedido");
        detailsDialog.setWidth("600px");
        detailsDialog.setHeight("550px"); // Aumentado para acomodar o total

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        FormLayout orderInfoLayout = new FormLayout();
        orderInfoLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        TextField idField = new TextField("ID do Pedido");
        idField.setValue(order.getId().toString());
        idField.setReadOnly(true);

        TextField statusField = new TextField("Status");
        statusField.setValue(order.getOrderStatus().toString());
        statusField.setReadOnly(true);

        TextField createdAtField = new TextField("Criado em");
        createdAtField.setValue(order.getCreatedAt() != null ? order.getCreatedAt().toString() : "N/A");
        createdAtField.setReadOnly(true);

        TextField customerField = new TextField("Vendedor");
        customerField.setValue(order.getSeller() != null ? order.getSeller().getFullName() : "N/A");
        customerField.setReadOnly(true);

        orderInfoLayout.add(idField, statusField, createdAtField, customerField);

        Grid<OrderItem> itemsGrid = new Grid<>(OrderItem.class, false);
        itemsGrid.addColumn(item -> item.getProduct() != null ? item.getProduct().getName() : "N/A")
                .setHeader("Produto").setAutoWidth(true);
        itemsGrid.addColumn(OrderItem::getQuantity).setHeader("Quantidade").setAutoWidth(true);
        itemsGrid
                .addColumn(item -> item.getPrice() != null ? "R$ " + String.format("%.2f", item.getPrice()) : "R$ 0,00")
                .setHeader("Preço Unitário").setAutoWidth(true);
        itemsGrid.addColumn(item -> {
            if (item.getPrice() != null) {
                BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                return "R$ " + String.format("%.2f", total);
            }
            return "R$ 0,00";
        }).setHeader("Total").setAutoWidth(true);

        if (order.getItems() != null) {
            itemsGrid.setItems(order.getItems());
        }
        itemsGrid.setHeight("200px");

        // Calcular e mostrar o total
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getItems() != null) {
            totalAmount = order.getItems().stream()
                    .filter(item -> item.getPrice() != null)
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();

        Button editButton = new Button("Editar", new Icon(VaadinIcon.EDIT));

        boolean canEdit = order.getOrderStatus() != OrderStatus.CANCELLED &&
                order.getOrderStatus() != OrderStatus.INVOICED;

        if (canEdit) {
            editButton.addClickListener(e -> {
                detailsDialog.close();
                openEditDialog(order);
            });
        } else {
            editButton.setEnabled(false);
            String statusText = order.getOrderStatus() == OrderStatus.CANCELLED ? "cancelado" : "faturado";
            editButton.addClickListener(e -> {
                Notification.show("⚠️ Este pedido não pode ser editado pois foi " + statusText + ".",
                        4000, Notification.Position.MIDDLE);
            });
        }

        Button closeButton = new Button("Fechar");
        closeButton.addClickListener(e -> detailsDialog.close());

        buttonLayout.add(editButton, closeButton);

        mainLayout.add(
                new com.vaadin.flow.component.html.H4("Informações do Pedido"),
                orderInfoLayout,
                new com.vaadin.flow.component.html.H4("Itens"),
                itemsGrid,
                buttonLayout);

        detailsDialog.add(mainLayout);
        detailsDialog.open();
    }

    private Employee createTodosEmployee() {
        Employee todos = new Employee();
        todos.setId(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        try {
            todos.setFullName("TODOS");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return todos;
    }
}