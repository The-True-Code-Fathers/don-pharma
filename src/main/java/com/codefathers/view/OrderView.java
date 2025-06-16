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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
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
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Order")
@Route("order")
public class OrderView extends VerticalLayout {
    private static final UUID ALL_EMPLOYEES_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private ShippingProviderService shippingProviderService;
    ProductRepositoryImpl productRepository = new ProductRepositoryImpl();
    ShippingProviderRepositoryImpl shippingProviderRepository = new ShippingProviderRepositoryImpl();
    OrderItemRepositoryImpl orderItemRepository = new OrderItemRepositoryImpl();
    private ProductService productService;
    private EmployeeService employeeService;
    private OrderService orderService;
    private Grid<Order> grid = new Grid<>(Order.class, false);
    private GridLazyDataView<Order> dataView;
    private OrderRepositoryImpl orderRepository;
    private TextField searchField = new TextField();
    private Select<String> statusFilter = new Select<>();
    private Dialog dialog = new Dialog();
    private Dialog editDialog = new Dialog();
    private String currentSearchingTerm = "";
    private String currentStatus = "ALL";
    private Order currentOrderEditing = null;
    private Employee currentEmployeeFilter = null;
    private ComboBox<Product> productComboBox;
    private IntegerField quantityField;
    private NumberField priceField;
    private List<OrderItemRow> orderItems;
    private Grid<OrderItemRow> itemsGrid;

    public OrderView() {
        productService = new ProductService(productRepository, ValidatorUtil.getValidator());
        shippingProviderService = new ShippingProviderService(shippingProviderRepository);
        EmployeeRepository employeeRepository = new EmployeeRepositoryImpl();
        employeeService = new EmployeeService(employeeRepository, ValidatorUtil.getValidator());
        var orderRepository = new OrderRepositoryImpl();
        var storageRepository = new StorageRepositoryImpl();
        this.orderService = new OrderService(orderRepository, employeeRepository, storageRepository,
                ValidatorUtil.getValidator());

        setSizeFull();
        initializeFormComponents();

        searchStatusFilter();
        setupGrid();
        setupDialog();
        setupEditDialog();

        Button createButton = new Button("Create Order", new Icon(VaadinIcon.PLUS));
        createButton.addClickListener(e -> openCreateOrderDialog());

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setWidthFull();
        topLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        topLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        topLayout.setSpacing(true);
        topLayout.setPadding(false);

        topLayout.add(createButton, searchField, statusFilter);

        searchField.setWidth("300px");
        statusFilter.setWidth("150px");

        add(topLayout, grid);
        setFlexGrow(1, grid);
        setupEmployeeSearchField();
        setupLazyDataProvider();
    }

    private void setupGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.addColumn(Order::getId)
                .setHeader("Order ID")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(order -> order.getSeller() != null ? order.getSeller().getFullName() : "N/A")
                .setHeader("Seller")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(order -> {
            if (order.getCreatedAt() != null) {
                return order.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
            return "N/A";
        })
                .setHeader("Created At")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(order -> {
            String description = order.getDescription();
            if (description != null && !description.trim().isEmpty()) {
                return description.length() > 50 ? description.substring(0, 47) + "..." : description;
            }
            return "No description";
        })
                .setHeader("Description")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(order -> {
            BigDecimal totalAmount = order.getTotalAmount();
            if (totalAmount != null) {
                return "R$ " + String.format("%.2f", totalAmount);
            }
            return "R$ 0,00";
        })
                .setHeader("Total Amount")
                .setSortable(true)
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
                .setHeader("Qty. Items")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(order -> {
            if (order.getShippingProvider() != null) {
                return order.getShippingProvider().getName();
            }
            return "N/A";
        })
                .setHeader("Shipping Provider")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(Order::getOrderStatus)
                .setHeader("Status")
                .setSortable(true)
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addItemDoubleClickListener(event -> {
            Order selectedOrder = event.getItem();
            if (selectedOrder != null) {
                showOrderDetails(selectedOrder);
            }
        });
    }

    private void setupEditDialog() {
        editDialog.setHeaderTitle("Edit Order");
        editDialog.setResizable(true);
        editDialog.setDraggable(true);
        editDialog.setWidth("800px");
        editDialog.setHeight("600px");
    }

    private void openEditDialog(Order order) {
        editDialog.removeAll();

        this.currentOrderEditing = order;

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        FormLayout orderEditLayout = new FormLayout();
        orderEditLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        TextField idField = new TextField("Order ID");
        idField.setValue(order.getId().toString());
        idField.setReadOnly(true);

        Select<OrderStatus> statusSelect = new Select<>();
        statusSelect.setLabel("Status");
        statusSelect.setItems(OrderStatus.values());
        statusSelect.setValue(order.getOrderStatus());

        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.INVOICED) {
            statusSelect.setEnabled(false);
        }

        ComboBox<Employee> sellerComboBox = new ComboBox<>("Seller");
        setupSellerComboBox(sellerComboBox);
        sellerComboBox.setValue(order.getSeller());

        TextField descriptionField = new TextField("Description");
        descriptionField.setValue(order.getDescription() != null ? order.getDescription() : "");
        descriptionField.setWidthFull();

        orderEditLayout.add(idField, statusSelect, sellerComboBox, descriptionField);

        Grid<OrderItem> itemsGrid = new Grid<>(OrderItem.class, false);
        itemsGrid.addColumn(item -> item.getProduct() != null ? item.getProduct().getName() : "N/A")
                .setHeader("Product").setAutoWidth(true);
        itemsGrid.addColumn(item -> item.getProduct() != null ? item.getProduct().getSku() : "N/A")
                .setHeader("SKU").setAutoWidth(true);
        itemsGrid.addColumn(OrderItem::getQuantity)
                .setHeader("Quantity").setAutoWidth(true);
        itemsGrid
                .addColumn(item -> item.getPrice() != null ? "R$ " + String.format("%.2f", item.getPrice()) : "R$ 0,00")
                .setHeader("Unit Price").setAutoWidth(true);
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
        itemsGrid.setHeight("250px");

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getItems() != null) {
            totalAmount = order.getItems().stream()
                    .filter(item -> item.getPrice() != null)
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        buttonLayout.setWidthFull();

        Button saveButton = new Button("Save", new Icon(VaadinIcon.CHECK));
        saveButton.addClickListener(e -> saveOrderEdit(statusSelect, sellerComboBox, descriptionField));

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(e -> editDialog.close());

        buttonLayout.add(saveButton, cancelButton);

        mainLayout.add(
                new com.vaadin.flow.component.html.H4("Edit Order"),
                orderEditLayout,
                new com.vaadin.flow.component.html.H4("Order Items"),
                itemsGrid,
                buttonLayout);

        editDialog.add(mainLayout);
        editDialog.open();
    }

    private void saveOrderEdit(Select<OrderStatus> statusSelect, ComboBox<Employee> sellerComboBox,
            TextField descriptionField) {
        try {
            if (currentOrderEditing == null) {
                Notification.show("Error: No order selected for editing", 5000, Notification.Position.MIDDLE);
                return;
            }

            if (sellerComboBox.getValue() == null) {
                Notification.show("Select a seller", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (statusSelect.getValue() == null) {
                Notification.show("Select a status", 3000, Notification.Position.MIDDLE);
                return;
            }

            orderService.updateOrder(
                    currentOrderEditing.getId(),
                    statusSelect.getValue(),
                    sellerComboBox.getValue(),
                    descriptionField.getValue());

            Notification.show("Order updated successfully!", 3000, Notification.Position.MIDDLE);

            refreshGrid();
            editDialog.close();

            currentOrderEditing = null;

        } catch (BusinessRuleException ex) {
            Notification.show("Business Rule Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (IllegalArgumentException ex) {
            Notification.show("Error: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (Exception ex) {
            System.err.println("Error saving order: " + ex.getMessage());
            ex.printStackTrace();
            Notification.show("Unexpected error saving: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void refreshGrid() {
        dataView.refreshAll();
    }

    public void setupEmployeeSearchField() {
        searchField.setPlaceholder("Search by ID, Seller, or Product...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setClearButtonVisible(true);

        searchField.addValueChangeListener(e -> {
            currentSearchingTerm = e.getValue() != null ? e.getValue() : "";
            dataView.refreshAll();
        });
    }

    private void searchStatusFilter() {
        List<String> statusItems = List.of("ALL", "OPEN", "CANCELLED", "INVOICED");

        statusFilter.setItems(statusItems);
        statusFilter.setValue("ALL");
        statusFilter.setEmptySelectionAllowed(false);
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
                            .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt())) // Ordenação padrão
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
        if (!currentStatus.equals("ALL") && !order.getOrderStatus().name().equalsIgnoreCase(currentStatus)) {
            return false;
        }

        if (currentEmployeeFilter != null && !currentEmployeeFilter.getId().equals(ALL_EMPLOYEES_ID)) {
            if (order.getSeller() == null || !order.getSeller().getId().equals(currentEmployeeFilter.getId())) {
                return false;
            }
        }

        if (!currentSearchingTerm.isEmpty()) {
            String lowerCaseTerm = currentSearchingTerm.toLowerCase();

            boolean idMatches = order.getId().toString().toLowerCase().contains(lowerCaseTerm);

            boolean sellerMatches = order.getSeller() != null &&
                    order.getSeller().getFullName() != null &&
                    order.getSeller().getFullName().toLowerCase().contains(lowerCaseTerm);

            boolean productMatches = order.getItems() != null &&
                    order.getItems().stream()
                            .anyMatch(item -> item.getProduct() != null &&
                                    item.getProduct().getName() != null &&
                                    item.getProduct().getName().toLowerCase().contains(lowerCaseTerm));

            if (!(idMatches || sellerMatches || productMatches)) {
                return false;
            }
        }

        return true;
    }

    private void setupDialog() {
        dialog.setHeaderTitle("Order details");
        dialog.setResizable(true);
        dialog.setDraggable(true);
        dialog.setWidth("400px");
        dialog.setHeight("200px");
    }

    private void openCreateOrderDialog() {
        Dialog createDialog = new Dialog();
        createDialog.setHeaderTitle("Create new order");
        createDialog.setWidth("900px"); // Aumentado para acomodar melhor o grid
        createDialog.setHeight("800px");
        createDialog.setResizable(true);

        ComboBox<Employee> sellerComboBox = new ComboBox<>("Seller");
        ComboBox<ShippingProvider> shippingComboBox = new ComboBox<>("Shipping provider");
        TextField descriptionField = new TextField("Description");

        itemsGrid = new Grid<>();
        orderItems = new ArrayList<>();

        ComboBox<Product> productComboBox = new ComboBox<>("Product");
        IntegerField quantityField = new IntegerField("Quantity");

        NumberField priceField = new NumberField("Price");
        priceField.setPrefixComponent(new Span("R$"));
        priceField.setStepButtonsVisible(false);

        Button addItemButton = new Button("Add item");

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

        setupItemsGridImproved(itemsGrid, orderItems);

        addItemButton.addClickListener(e -> {
            Product selectedProduct = productComboBox.getValue();
            Integer quantity = quantityField.getValue();
            Double priceDouble = priceField.getValue();
            if (priceDouble == null || priceDouble <= 0) {
                Notification.show("Inform a valid price", 3000, Notification.Position.MIDDLE);
                return;
            }
            BigDecimal price = BigDecimal.valueOf(priceDouble);

            if (selectedProduct == null) {
                Notification.show("Select a product", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (quantity == null || quantity < 1) {
                Notification.show("Inform a valid quantity", 3000, Notification.Position.MIDDLE);
                return;
            }

            boolean exists = orderItems.stream()
                    .anyMatch(item -> item.getProduct().getSku().equals(selectedProduct.getSku()));

            if (exists) {
                Notification.show("Product already added to the order.", 3000, Notification.Position.MIDDLE);
                return;
            }

            OrderItemRow newItem = new OrderItemRow(selectedProduct, quantity, price);
            orderItems.add(newItem);
            itemsGrid.setItems(orderItems);

            productComboBox.clear();
            quantityField.setValue(1);
            priceField.setValue(1.0);

            Notification.show("Item added!", 2000, Notification.Position.MIDDLE);
        });

        Button saveButton = new Button("Create order", e -> {
            try {
                if (sellerComboBox.getValue() == null) {
                    Notification.show("Select an seller", 3000, Notification.Position.MIDDLE);
                    return;
                }

                if (shippingComboBox.getValue() == null) {
                    Notification.show("Select a shipping provider", 3000, Notification.Position.MIDDLE);
                    return;
                }

                if (orderItems.isEmpty()) {
                    Notification.show("Add at least one item", 3000, Notification.Position.MIDDLE);
                    return;
                }

                CreateOrderDTO createOrderDTO = buildCreateOrderDTO(
                        sellerComboBox.getValue(),
                        shippingComboBox.getValue(),
                        descriptionField.getValue(),
                        orderItems);

                orderService.createOrder(createOrderDTO);

                Notification.show("Succeed creating order!", 3000, Notification.Position.MIDDLE);
                dataView.refreshAll();
                createDialog.close();

            } catch (Exception ex) {
                System.out.println("Error creating order: " + ex.getMessage());
                ex.printStackTrace();
                Notification.show("Error creating order: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("Cancel", e -> createDialog.close());

        HorizontalLayout addItemLayout = new HorizontalLayout(productComboBox, quantityField, priceField,
                addItemButton);
        addItemLayout.setAlignItems(Alignment.END);
        addItemLayout.setWidthFull();
        productComboBox.setWidth("300px");
        quantityField.setWidth("100px");
        priceField.setWidth("120px");

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.add(
                sellerComboBox,
                shippingComboBox,
                descriptionField,
                new com.vaadin.flow.component.html.H4("Order items"),
                addItemLayout,
                itemsGrid,
                new HorizontalLayout(saveButton, cancelButton));
        mainLayout.setSpacing(true);
        mainLayout.setPadding(true);

        createDialog.add(mainLayout);
        createDialog.open();
    }

    private void setupItemsGridImproved(Grid<OrderItemRow> grid, List<OrderItemRow> items) {
        grid.setItems(items);
        grid.setHeight("250px");
        grid.setWidthFull();

        grid.addColumn(item -> {
            String productName = item.getProduct().getName();
            return productName.length() > 25 ? productName.substring(0, 22) + "..." : productName;
        })
                .setHeader("Products")
                .setWidth("130px")
                .setFlexGrow(0);

        grid.addColumn(item -> item.getProduct().getSku())
                .setHeader("SKU")
                .setWidth("150px")
                .setFlexGrow(0);

        grid.addColumn(OrderItemRow::getQuantity)
                .setHeader("Qtd")
                .setWidth("70px")
                .setFlexGrow(1);

        grid.addColumn(item -> "R$ " + String.format("%.2f", item.getPrice()))
                .setHeader("Unit price.")
                .setWidth("120px")
                .setFlexGrow(0);

        grid.addColumn(
                item -> "R$ " + String.format("%.2f", item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .setHeader("Total")
                .setWidth("130px")
                .setFlexGrow(0);

        grid.addComponentColumn(item -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            actions.setAlignItems(Alignment.CENTER);

            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
            removeButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR);
            removeButton.addClickListener(e -> {
                items.remove(item);
                grid.getDataProvider().refreshAll();
                Notification.show("Removed items.", 2000, Notification.Position.MIDDLE);
            });

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> {
                editItem(item);
                editButton.getElement().setAttribute("title", "Edit item");
            });

            actions.add(editButton, removeButton);
            return actions;
        })
                .setHeader("Actions")
                .setWidth("120px")
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
        sellerComboBox.setPlaceholder("Select a seller");
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

        grid.addColumn(item -> item.getProduct().getName()).setHeader("Product").setAutoWidth(true);
        grid.addColumn(item -> item.getProduct().getSku()).setHeader("SKU").setAutoWidth(true);
        grid.addColumn(OrderItemRow::getQuantity).setHeader("Quantity").setAutoWidth(true);
        grid.addColumn(item -> "R$ " + String.format("%.2f", item.getPrice())).setHeader("Unit Price")
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
                Notification.show("Item removed", 2000, Notification.Position.MIDDLE);
            });
            return removeButton;
        }).setHeader("Actions").setAutoWidth(true);
    }

    private void setupShippingComboBox(ComboBox<ShippingProvider> shippingComboBox) {
        shippingComboBox.setItemLabelGenerator(provider -> provider.getName());
        shippingComboBox.setAllowCustomValue(false);
        shippingComboBox.setWidthFull();
        shippingComboBox.setPlaceholder("Select a shipping provider");

        List<ShippingProvider> providers = shippingProviderService.listAllShippingProviders();
        shippingComboBox.setItems(providers);
    }

    private void setupProductComboBox(ComboBox<Product> productComboBox) {
        productComboBox.setItemLabelGenerator(product -> product.getName() + " (" + product.getSku() + ")");
        productComboBox.setAllowCustomValue(false);
        productComboBox.setPlaceholder("Select a product");

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
        detailsDialog.setHeaderTitle("Order Details");
        detailsDialog.setWidth("600px");
        detailsDialog.setHeight("550px");
        detailsDialog.setHeight("550px");

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        FormLayout orderInfoLayout = new FormLayout();
        orderInfoLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        TextField idField = new TextField("Order ID");
        idField.setValue(order.getId().toString());
        idField.setReadOnly(true);

        TextField statusField = new TextField("Status");
        statusField.setValue(order.getOrderStatus().toString());
        statusField.setReadOnly(true);

        TextField createdAtField = new TextField("Created At");
        createdAtField.setValue(order.getCreatedAt() != null
                ? order.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "N/A");
        createdAtField.setReadOnly(true);

        TextField customerField = new TextField("Seller");
        customerField.setValue(order.getSeller() != null ? order.getSeller().getFullName() : "N/A");
        customerField.setReadOnly(true);

        orderInfoLayout.add(idField, statusField, createdAtField, customerField);

        Grid<OrderItem> itemsGrid = new Grid<>(OrderItem.class, false);
        itemsGrid.addColumn(item -> item.getProduct() != null ? item.getProduct().getName() : "N/A")
                .setHeader("Product").setAutoWidth(true);
        itemsGrid.addColumn(OrderItem::getQuantity).setHeader("Quantity").setAutoWidth(true);
        itemsGrid
                .addColumn(item -> item.getPrice() != null ? "R$ " + String.format("%.2f", item.getPrice()) : "R$ 0,00")
                .setHeader("Unit Price").setAutoWidth(true);
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

        Button editButton = new Button("Edit", new Icon(VaadinIcon.EDIT));

        boolean canEdit = order.getOrderStatus() != OrderStatus.CANCELLED &&
                order.getOrderStatus() != OrderStatus.INVOICED;

        if (canEdit) {
            editButton.addClickListener(e -> {
                detailsDialog.close();
                openEditDialog(order);
            });
        } else {
            editButton.setEnabled(false);
            String statusText = order.getOrderStatus() == OrderStatus.CANCELLED ? "cancelled" : "invoiced";
            editButton.addClickListener(e -> {
                Notification.show("⚠️ This order cannot be edited as it has been " + statusText + ".",
                        4000, Notification.Position.MIDDLE);
            });
        }

        Button closeButton = new Button("Close");
        closeButton.addClickListener(e -> detailsDialog.close());

        buttonLayout.add(editButton, closeButton);

        mainLayout.add(
                new com.vaadin.flow.component.html.H4("Order Information"),
                orderInfoLayout,
                new com.vaadin.flow.component.html.H4("Items"),
                itemsGrid,
                buttonLayout);

        detailsDialog.add(mainLayout);
        detailsDialog.open();
    }

    private void editItem(OrderItemRow item) {
        productComboBox.setValue(item.getProduct());
        quantityField.setValue(item.getQuantity());
        priceField.setValue(item.getPrice().doubleValue());

        orderItems.remove(item);
        itemsGrid.getDataProvider().refreshAll();

        Notification.show("Item loaded for editing. Modify and click 'Add Item' to update.",
                3000, Notification.Position.MIDDLE);
    }

    private void initializeFormComponents() {
        productComboBox = new ComboBox<>("Product");
        setupProductComboBox(productComboBox);

        quantityField = new IntegerField("Quantity");
        quantityField.setValue(1);
        quantityField.setMin(1);
        quantityField.setStepButtonsVisible(true);

        priceField = new NumberField("Price");
        priceField.setPrefixComponent(new Span("R$"));
        priceField.setStepButtonsVisible(false);
        priceField.setValue(1.0);
        priceField.setMin(0.01);
        priceField.setStep(0.01);

        orderItems = new ArrayList<>();
    }
}