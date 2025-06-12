package com.codefathers.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.codefathers.model.dto.CreateOrderDTO;
import com.codefathers.model.dto.CreateOrderItemDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Order;
import com.codefathers.model.entity.Product;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.model.enums.OrderStatus;
import com.codefathers.repository.implementations.EmployeeRepositoryImpl;
import com.codefathers.repository.implementations.OrderRepositoryImpl;
import com.codefathers.repository.implementations.ProductRepositoryImpl;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.repository.implementations.StorageRepositoryImpl;
import com.codefathers.repository.interfaces.EmployeeRepository;
import com.codefathers.service.EmployeeService;
import com.codefathers.service.OrderService;
import com.codefathers.service.ProductService;
import com.codefathers.service.ShippingProviderService;
import com.codefathers.util.ValidatorUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
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
    private ProductService productService;
    private EmployeeService employeeService;
    private OrderService orderService;
    private Grid<Order> grid = new Grid<>(Order.class, false);
    private GridLazyDataView<Order> dataView;
    private Select<OrderStatus> orderStatusSelect;
    private OrderRepositoryImpl orderRepository;
    private OrderStatus currentStatusFilter = null;
    private TextField searchField = new TextField();
    private Select<String> statusFilter = new Select<>();
    private ComboBox<Employee> employeeComboBox;
    private Dialog dialog = new Dialog();
    private String currentSearchingTerm = "";
    private String currentStatus = "TODOS";

    public OrderView() {
        productService = new ProductService(productRepository, ValidatorUtil.getValidator());
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

        Button createButton = new Button("Criar Pedido", new Icon(VaadinIcon.PLUS));
        createButton.addClickListener(e -> openCreateOrderDialog());

        // Layout dos filtros (vendedor e status)
        HorizontalLayout filters = new HorizontalLayout(employeeComboBox, statusFilter);
        filters.setAlignItems(Alignment.CENTER);
        filters.setSpacing(true);

        // Layout principal da linha superior: createButton + espaço expansível +
        // filtros
        HorizontalLayout topLayout = new HorizontalLayout(createButton, filters);
        topLayout.setWidthFull();
        topLayout.setAlignItems(Alignment.CENTER);
        topLayout.expand(filters); // faz os filtros ficarem na direita

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

        grid.asSingleSelect().addValueChangeListener(event -> {
            Order selected = event.getValue();
            if (selected != null) {
                showOrderDialog(selected);
            }
        });
    }

    private void setupEmployeeComboBox() {
        employeeComboBox.setItemLabelGenerator(Employee::getFullName);
        employeeComboBox.setAllowCustomValue(false);
        employeeComboBox.setPageSize(10);

        employeeComboBox.setItems(query -> {
            String filter = query.getFilter().orElse("");
            return filterEmployeesByName(filter).stream()
                    .skip(query.getOffset())
                    .limit(query.getLimit());
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

        if (currentSearchingTerm.isEmpty())
            return true;

        String sellerName = order.getSeller() != null ? order.getSeller().getFullName().toLowerCase() : "";
        return sellerName.contains(currentSearchingTerm);
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

        VerticalLayout content = new VerticalLayout();
        content.add("ID: " + order.getId());
        content.add("Vendedor: " + order.getSeller().getFullName());
        content.add("Status: " + order.getOrderStatus());
        content.add("Total: R$" + order.getTotalAmount());

        Button close = new Button("Fechar", e -> dialog.close());
        content.add(close);

        dialog.add(content);
        dialog.open();
    }

    private void openCreateOrderDialog() {
        Dialog createDialog = new Dialog();
        createDialog.setHeaderTitle("Criar Novo Pedido");
        createDialog.setWidth("800px");
        createDialog.setHeight("600px");
        createDialog.setResizable(true);

        ComboBox<Employee> sellerComboBox = new ComboBox<>("Vendedor");
        ComboBox<ShippingProvider> shippingComboBox = new ComboBox<>("Transportadora");
        TextField descriptionField = new TextField("Descrição");

        Grid<OrderItemRow> itemsGrid = new Grid<>();
        List<OrderItemRow> orderItems = new ArrayList<>();

        ComboBox<Product> productComboBox = new ComboBox<>("Produto");
        IntegerField quantityField = new IntegerField("Quantidade");
        NumberField priceField = new NumberField("Price");
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

        HorizontalLayout addItemLayout = new HorizontalLayout(productComboBox, quantityField, addItemButton);
        addItemLayout.setAlignItems(Alignment.END);
        addItemLayout.setWidthFull();
        productComboBox.setWidth("300px");
        quantityField.setWidth("100px");

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

    private void setupCreateOrderButton() {
        Button createButton = new Button("Criar Pedido");
        createButton.setIcon(new Icon(VaadinIcon.PLUS));
        createButton.addClickListener(e -> openCreateOrderDialog());
        add(createButton);
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
}