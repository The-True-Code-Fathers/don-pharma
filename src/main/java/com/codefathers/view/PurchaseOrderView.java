package com.codefathers.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.codefathers.model.dto.CreatePurchaseOrderDTO;
import com.codefathers.model.dto.CreatePurchaseOrderItemDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Product;
import com.codefathers.model.entity.PurchaseOrder;
import com.codefathers.repository.implementations.EmployeeRepositoryImpl;
import com.codefathers.repository.implementations.ProductRepositoryImpl;
import com.codefathers.repository.implementations.PurchaseOrderRepositoryImpl;
import com.codefathers.repository.implementations.StorageRepositoryImpl;
import com.codefathers.service.EmployeeService;
import com.codefathers.service.ProductService;
import com.codefathers.service.PurchaseOrderService;
import com.codefathers.util.ValidatorUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

@Route("purchaseOrder")
public class PurchaseOrderView extends VerticalLayout {

    private final PurchaseOrderService purchaseOrderService;
    private final ProductService productService;
    private final EmployeeService employeeService;

    private final TextField searchField = new TextField();
    private final Button addItemButton = new Button("Add Item");
    private final Button createOrderButton = new Button("Create Order");

    private final Grid<PurchaseOrder> grid = new Grid<>(PurchaseOrder.class, false);
    private final Grid<CreatePurchaseOrderItemDTO> itemGrid = new Grid<>(CreatePurchaseOrderItemDTO.class, false); // Novo
                                                                                                                   // grid

    private ComboBox<Product> productComboBox = new ComboBox<>("Product");
    private NumberField quantityField = new NumberField("Quantity");
    private NumberField priceField = new NumberField("Price");
    private ComboBox<Employee> purchaserComboBox = new ComboBox<>("Purchaser");

    private List<CreatePurchaseOrderItemDTO> items = new ArrayList<>();

    private String currentSearchTerm = "";

    public PurchaseOrderView() {
        var purchaseOrderRepository = new PurchaseOrderRepositoryImpl();
        var employeeRepository = new EmployeeRepositoryImpl();
        var storageRepository = new StorageRepositoryImpl();
        var productRepository = new ProductRepositoryImpl();
        this.purchaseOrderService = new PurchaseOrderService(purchaseOrderRepository, employeeRepository,
                storageRepository, ValidatorUtil.getValidator());
        this.employeeService = new EmployeeService(employeeRepository, ValidatorUtil.getValidator());
        this.productService = new ProductService(productRepository, ValidatorUtil.getValidator());

        setupSearchField();
        setupGrid();
        setupForm();
        setupItemGrid(); // configura o grid de itens adicionados

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2));
        formLayout.add(productComboBox, quantityField, priceField, purchaserComboBox, addItemButton, createOrderButton);

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setWidthFull();
        topLayout.setAlignItems(Alignment.END);

        searchField.setWidth("300px");
        topLayout.add(formLayout, searchField);
        topLayout.expand(formLayout);

        add(topLayout, itemGrid, grid); // adiciona itemGrid no layout visual

        refreshGrid();
    }

    private void setupSearchField() {
        searchField.setPlaceholder("Search purchase orders...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setClearButtonVisible(true);

        searchField.addValueChangeListener(e -> {
            currentSearchTerm = e.getValue().trim().toLowerCase();
            refreshGrid();
        });
    }

    private void setupGrid() {
        grid.addColumn(po -> po.getId().toString()).setHeader("ID").setSortable(true);
        grid.addColumn(po -> po.getPurchaserId().getFullName()).setHeader("Purchaser").setSortable(true);
        grid.addColumn(po -> po.getPurchaseTotalAmount()).setHeader("Total Amount").setSortable(true);
        grid.addColumn(po -> po.getCreatedAt().toString()).setHeader("Created At").setSortable(true);

        grid.setHeight("400px");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
    }

    private void setupItemGrid() {
        itemGrid.addColumn(item -> item.getProduct().getName()).setHeader("Product");
        itemGrid.addColumn(CreatePurchaseOrderItemDTO::getQuantity).setHeader("Quantity");
        itemGrid.addColumn(item -> item.getPrice().toString()).setHeader("Price");

        itemGrid.setHeight("200px");
        itemGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
    }

    private void setupForm() {
        productComboBox.setItems(productService.findAllProducts());
        productComboBox.setItemLabelGenerator(Product::getName);
        productComboBox.setPlaceholder("Select a product");

        quantityField.setMin(1);
        quantityField.setStep(1);
        quantityField.setValue(1.0);

        priceField.setMin(0);
        priceField.setStep(0.01);
        priceField.setValue(0.0);

        purchaserComboBox.setItems(employeeService.employeeList());
        purchaserComboBox.setItemLabelGenerator(Employee::getFullName);
        purchaserComboBox.setPlaceholder("Select purchaser");

        addItemButton.addClickListener(e -> addItem());
        createOrderButton.addClickListener(e -> createOrder());
    }

    private void addItem() {
        Product selectedProduct = productComboBox.getValue();
        Double quantityValue = quantityField.getValue();
        Double priceValue = priceField.getValue();

        if (selectedProduct == null) {
            Notification.show("Please select a product.");
            return;
        }
        if (quantityValue == null || quantityValue < 1) {
            Notification.show("Quantity must be at least 1.");
            return;
        }
        if (priceValue == null || priceValue <= 0) {
            Notification.show("Price must be greater than zero.");
            return;
        }

        CreatePurchaseOrderItemDTO itemDTO = CreatePurchaseOrderItemDTO.builder()
                .product(selectedProduct)
                .quantity(quantityValue.intValue())
                .price(BigDecimal.valueOf(priceValue))
                .build();

        items.add(itemDTO);
        itemGrid.setItems(items); // atualiza o grid com os novos itens

        Notification.show("Item added successfully.");

        productComboBox.clear();
        quantityField.setValue(1.0);
        priceField.setValue(0.0);
    }

    private void createOrder() {
        Employee purchaser = purchaserComboBox.getValue();

        if (purchaser == null) {
            Notification.show("Please select a purchaser.");
            return;
        }

        if (items.isEmpty()) {
            Notification.show("Add at least one item before creating an order.");
            return;
        }

        CreatePurchaseOrderDTO dto = CreatePurchaseOrderDTO.builder()
                .purchaserId(purchaser.getId())
                .item(items)
                .build();

        try {
            purchaseOrderService.createPurchaseOrder(dto);
            Notification.show("Purchase order created successfully.");
            items.clear();

            // Limpa campos e grids
            productComboBox.clear();
            quantityField.setValue(1.0);
            priceField.setValue(0.0);
            purchaserComboBox.clear();
            itemGrid.setItems(items); // limpa a grid dos itens adicionados

            refreshGrid();
        } catch (Exception ex) {
            Notification.show("Error creating purchase order: " + ex.getMessage());
        }
    }

    private void refreshGrid() {
        List<PurchaseOrder> orders = purchaseOrderService.getAllPurchaseOrders();
        List<PurchaseOrder> filtered = orders.stream()
                .filter(this::matchesFilter)
                .toList();
        grid.setItems(filtered);
    }

    private boolean matchesFilter(PurchaseOrder order) {
        if (currentSearchTerm.isEmpty())
            return true;
        return order.getId().toString().toLowerCase().contains(currentSearchTerm);
    }
}
