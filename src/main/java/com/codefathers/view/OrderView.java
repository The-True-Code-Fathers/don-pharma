package com.codefathers.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
import com.codefathers.service.OrderService;
import com.codefathers.service.OrderItemService;
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

import static java.util.Arrays.stream;

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

        grid.addColumn(Order::getId).setHeader("ID do Pedido");
        grid.addColumn(order -> order.getSeller().getFullName()).setHeader("Nome do Vendedor").setAutoWidth(true);
        grid.addColumn(Order::getOrderStatus).setHeader("Status do Pedido").setAutoWidth(true);
        grid.addColumn(Order::getTotalAmount).setHeader("Valor Total (R$)").setAutoWidth(true);

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

    private void openEditDialog(Order order) {
        Dialog detailsDialog = new Dialog();
        detailsDialog.setHeaderTitle("Detalhes do Pedido");
        detailsDialog.setWidth("600px");
        detailsDialog.setHeight("500px");

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

        orderInfoLayout.add(idField, statusField, createdAtField);

        // Grid de itens com double-click listener
        Grid<OrderItem> itemsGrid = new Grid<>(OrderItem.class, false);
        itemsGrid.addColumn(item -> item.getProduct() != null ? item.getProduct().getName() : "N/A")
                .setHeader("Produto").setAutoWidth(true);
        itemsGrid.addColumn(item -> item.getProduct() != null ? item.getProduct().getSku() : "N/A")
                .setHeader("SKU").setAutoWidth(true);
        itemsGrid.addColumn(OrderItem::getQuantity)
                .setHeader("Quantidade").setAutoWidth(true);
        itemsGrid.addColumn(item -> item.getPrice() != null ? "R$ " + String.format("%.2f", item.getPrice()) : "R$ 0,00")
                .setHeader("Preço Unitário").setAutoWidth(true);
        itemsGrid.addColumn(item -> {
            if (item.getPrice() != null) {
                BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                return "R$ " + String.format("%.2f", total);
            }
            return "R$ 0,00";
        }).setHeader("Total").setAutoWidth(true);

        itemsGrid.addItemDoubleClickListener(event -> {
            OrderItem selectedItem = event.getItem();
            showOrderItemDetails(selectedItem);
        });

        if (order.getItems() != null) {
            itemsGrid.setItems(order.getItems());
        }
        itemsGrid.setHeight("200px");

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
                buttonLayout
        );

        detailsDialog.add(mainLayout);
        detailsDialog.open();
    }

    // Classe interna para formulário de item do pedido
    private static class OrderItemForm {
        ComboBox<Product> productField;
        NumberField quantityField;
        NumberField priceField;

        public OrderItemForm(List<Product> products) {
            this.productField = new ComboBox<>("Produto");
            this.productField.setItems(products);
            this.productField.setItemLabelGenerator(product -> product.getName() + " (" + product.getSku() + ")");
            this.productField.setPlaceholder("Selecione um produto");
            this.productField.setWidthFull();

            this.quantityField = new NumberField("Quantidade");
            this.quantityField.setMin(1);
            this.quantityField.setStep(1);
            this.quantityField.setValue(1.0);

            this.priceField = new NumberField("Preço");
            this.priceField.setMin(0.01);
            this.priceField.setStep(0.01);
            this.priceField.setValue(0.01);
        }
    }

    private void refreshGrid() {
        dataView.refreshAll();
        grid.getDataProvider().refreshAll();
    }

    private void setupEmployeeComboBox() {
        allEmployee = createTodosEmployee();

        employeeComboBox.setItemLabelGenerator(employee -> {
            if (employee == allEmployee) return "TODOS";
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
                        if (emp == allEmployee) return "todos".contains(filter.toLowerCase());
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

    private void showOrderDialog(Order order) {
    dialog.removeAll();
    dialog.setWidth("700px");
    dialog.setHeight("900px");
    
    VerticalLayout content = new VerticalLayout();
    content.setSpacing(true);
    content.setPadding(true);
    
    // Estilo para os labels
    String labelStyle = "font-weight: bold; margin-right: 10px; min-width: 120px; display: inline-block;";
    String valueStyle = "margin-left: 10px;";
    
    // Cabeçalho do pedido
    Div header = new Div();
    header.getElement().setProperty("innerHTML", 
        "<h3 style='margin-top: 0; color: var(--lumo-primary-text-color);'>Pedido #" + order.getId() + "</h3>");
    
    // Informações básicas
    Div sellerInfo = new Div();
    sellerInfo.getElement().setProperty("innerHTML", 
        "<span style='" + labelStyle + "'>Vendedor:</span>" + 
        "<span style='" + valueStyle + "'>" + order.getSeller().getFullName() + "</span>");
    
    Div statusInfo = new Div();
    String statusColor = order.getOrderStatus() == OrderStatus.CANCELLED ? "color: red;" : 
                        order.getOrderStatus() == OrderStatus.INVOICED ? "color: green;" : "";
    statusInfo.getElement().setProperty("innerHTML", 
        "<span style='" + labelStyle + "'>Status:</span>" + 
        "<span style='" + valueStyle + statusColor + "'>" + order.getOrderStatus() + "</span>");
    
    Div totalInfo = new Div();
    totalInfo.getElement().setProperty("innerHTML", 
        "<span style='" + labelStyle + "'>Valor Total:</span>" + 
        "<span style='" + valueStyle + "'>R$ " + String.format("%.2f", order.getTotalAmount()) + "</span>");
    
    // Seção de itens
    VerticalLayout itemsSection = new VerticalLayout();
    itemsSection.setSpacing(false);
    itemsSection.setPadding(false);
    
    H4 itemsTitle = new H4("Itens do Pedido");
    itemsTitle.getStyle().set("margin-bottom", "10px");
    
    if (order.getItems() != null && !order.getItems().isEmpty()) {
        // Tabela de itens
        Grid<OrderItem> itemsGrid = new Grid<>();
        itemsGrid.setItems(order.getItems());
        itemsGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        
        // Configuração responsiva da altura da grid
        if (order.getItems().size() <= 5) {
            itemsGrid.setHeight("auto");
            itemsGrid.setAllRowsVisible(true);
        } else {
            itemsGrid.setHeight("300px");
        }
        
        itemsGrid.addColumn(item -> item.getProduct().getName())
            .setHeader("Produto")
            .setAutoWidth(true);
        
        itemsGrid.addColumn(item -> item.getProduct().getSku())
            .setHeader("SKU")
            .setAutoWidth(true);
        
        itemsGrid.addColumn(OrderItem::getQuantity)
            .setHeader("Quantidade")
            .setAutoWidth(true);
        
        itemsGrid.addColumn(item -> "R$ " + String.format("%.2f", item.getPrice()))
            .setHeader("Preço Unit.")
            .setAutoWidth(true);
        
        itemsGrid.addColumn(item -> "R$ " + String.format("%.2f", 
                item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
            .setHeader("Total")
            .setAutoWidth(true);
        
        itemsSection.add(itemsTitle, itemsGrid);
    } else {
        itemsSection.add(itemsTitle, new Span("Nenhum item encontrado neste pedido"));
    }
    
    // Botão de fechar
    Button closeButton = new Button("Fechar", e -> dialog.close());
    closeButton.getStyle().set("margin-top", "20px");
    
    // Adicionando todos os componentes ao layout
    content.add(header, sellerInfo, statusInfo, totalInfo, itemsSection, closeButton);
    dialog.add(content);
    dialog.open();
}

    private void openCreateOrderDialog() {
        Dialog createDialog = new Dialog();
        createDialog.setHeaderTitle("Criar Novo Pedido");
        createDialog.setWidth("800px");
        createDialog.setHeight("800px");
        createDialog.setResizable(true);

        ComboBox<Employee> sellerComboBox = new ComboBox<>("Vendedor");
        ComboBox<ShippingProvider> shippingComboBox = new ComboBox<>("Transportadora");
        TextField descriptionField = new TextField("Descrição");

        Grid<OrderItemRow> itemsGrid = new Grid<>();
        List<OrderItemRow> orderItems = new ArrayList<>();

        ComboBox<Product> productComboBox = new ComboBox<>("Produto");
        IntegerField quantityField = new IntegerField("Quantidade");
        NumberField priceField = new NumberField("Preço");
        Button addItemButton = new Button("Adicionar Item");

        setupSellerComboBox(sellerComboBox);
        setupShippingComboBox(shippingComboBox);
        setupProductComboBox(productComboBox);
        setupItemsGrid(itemsGrid, orderItems);

        descriptionField.setWidthFull();
        descriptionField.setMaxLength(2048);

        quantityField.setValue(1);
        quantityField.setMin(1);
        quantityField.setStepButtonsVisible(true);

        priceField.setValue(1.0);
        priceField.setMin(1);
        priceField.setStepButtonsVisible(true);

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
        priceField.setWidth("100px");

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
        grid.addColumn(item -> "R$ " + item.getPrice()).setHeader("Preço Unit.").setAutoWidth(true);
        grid.addColumn(item -> "R$ " + item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .setHeader("Total").setAutoWidth(true);

        grid.addComponentColumn(item -> {
            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
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

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    private List<Employee> filterEmployeesByName(String filter) {
        String filterLower = filter == null ? "" : filter.toLowerCase();
        return employeeService.employeeList().stream()
                .filter(e -> e.getFullName() != null && e.getFullName().toLowerCase().contains(filterLower))
                .collect(Collectors.toList());
    }

    private void showOrderDetails(Order order) {
        Dialog detailsDialog = new Dialog();
        detailsDialog.setHeaderTitle("Detalhes do Pedido");
        detailsDialog.setWidth("600px");
        detailsDialog.setHeight("500px");

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

         TextField customerField = new TextField("Cliente");
         customerField.setValue(order.getSeller() != null ? order.getSeller().getFullName() : "N/A");
         customerField.setReadOnly(true);

        orderInfoLayout.add(idField, statusField, createdAtField); // Adicione outros campos aqui

        Grid<OrderItem> itemsGrid = new Grid<>(OrderItem.class, false);
        itemsGrid.addColumn(item -> item.getProduct() != null ? item.getProduct().getName() : "N/A").setHeader("Produto").setAutoWidth(true);
        itemsGrid.addColumn(OrderItem::getQuantity).setHeader("Quantidade").setAutoWidth(true);
        itemsGrid.addColumn(item -> item.getPrice() != null ? item.getPrice().toString() : "0.00").setHeader("Preço Unitário").setAutoWidth(true);
        itemsGrid.addColumn(item -> {
            if (item.getPrice() != null) {
                BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                return total.toString();
            }
            return "0.00";
        }).setHeader("Total").setAutoWidth(true);

        if (order.getItems() != null) {
            itemsGrid.setItems(order.getItems());
        }
        itemsGrid.setHeight("200px");

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
                buttonLayout
        );

        detailsDialog.add(mainLayout);
        detailsDialog.open();
    }

    private void showOrderItemDetails(OrderItem orderItem) {
        Dialog itemDialog = new Dialog();
        itemDialog.setHeaderTitle("Detalhes do Item");
        itemDialog.setWidth("400px");
        itemDialog.setHeight("300px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        TextField productNameField = new TextField("Produto");
        productNameField.setValue(orderItem.getProduct() != null ? orderItem.getProduct().getName() : "N/A");
        productNameField.setReadOnly(true);

        TextField skuField = new TextField("SKU");
        skuField.setValue(orderItem.getProduct() != null ? orderItem.getProduct().getSku() : "N/A");
        skuField.setReadOnly(true);

        TextField quantityField = new TextField("Quantidade");
        quantityField.setValue(String.valueOf(orderItem.getQuantity()));
        quantityField.setReadOnly(true);

        TextField priceField = new TextField("Preço Unitário");
        priceField.setValue(orderItem.getPrice() != null ? "R$ " + String.format("%.2f", orderItem.getPrice()) : "R$ 0,00");
        priceField.setReadOnly(true);

        TextField totalField = new TextField("Total");
        if (orderItem.getPrice() != null) {
            BigDecimal total = orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            totalField.setValue("R$ " + String.format("%.2f", total));
        } else {
            totalField.setValue("R$ 0,00");
        }
        totalField.setReadOnly(true);

        formLayout.add(productNameField, skuField, quantityField, priceField, totalField);

        Button closeButton = new Button("Fechar");
        closeButton.addClickListener(e -> itemDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(closeButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();

        layout.add(formLayout, buttonLayout);
        itemDialog.add(layout);
        itemDialog.open();
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