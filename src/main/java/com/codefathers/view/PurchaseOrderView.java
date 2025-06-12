package com.codefathers.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.codefathers.model.dto.CreatePurchaseOrderDTO;
import com.codefathers.model.dto.CreatePurchaseOrderItemDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Product;
import com.codefathers.model.entity.PurchaseOrder;
import com.codefathers.model.entity.PurchaseOrderItem;
import com.codefathers.model.enums.PurchaseOrderStatus;
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
import com.vaadin.flow.component.dialog.Dialog;
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
    private final Button openDialogButton = new Button("Create Order");

    private final Grid<PurchaseOrder> grid = new Grid<>(PurchaseOrder.class, false);
    private final Grid<CreatePurchaseOrderItemDTO> itemGrid = new Grid<>(CreatePurchaseOrderItemDTO.class, false);

    private final ComboBox<Product> productComboBox = new ComboBox<>("Product");
    private final NumberField quantityField = new NumberField("Quantity");
    private final NumberField priceField = new NumberField("Price");
    private final ComboBox<Employee> purchaserComboBox = new ComboBox<>("Purchaser");
    private final Dialog editDialog = new Dialog();
    private final ComboBox<Product> editProductComboBox = new ComboBox<>("Product");
    private final NumberField editQuantityField = new NumberField("Quantity");
    private final ComboBox<PurchaseOrderStatus> statusComboBox = new ComboBox<>("Status");
    private PurchaseOrder currentOrderEditing = null;
    private final Dialog orderDialog = new Dialog();

    private final List<CreatePurchaseOrderItemDTO> items = new ArrayList<>();

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
        setupItemGrid();
        setupForm();
        setupOrderDialog();
        setupEditDialog();

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setWidthFull();
        topLayout.setAlignItems(Alignment.END);

        searchField.setWidth("300px");
        topLayout.add(openDialogButton, searchField);

        add(topLayout, itemGrid, grid, orderDialog, editDialog);

        refreshGrid();
    }

    private void setupGrid() {
        grid.addColumn(po -> po.getId().toString()).setHeader("ID").setSortable(true);
        grid.addColumn(po -> po.getPurchaserId().getFullName()).setHeader("Purchaser").setSortable(true);
        grid.addColumn(po -> po.getPurchaseTotalProductAmount()).setHeader("Total Amount").setSortable(true);
        grid.addColumn(po -> po.getPurchaseTotalPriceAmount()).setHeader("Total Price Amount").setSortable(true);
        grid.addColumn(po -> po.getCreatedAt().toString()).setHeader("Created At").setSortable(true);
        grid.addColumn(po -> po.getPurchaseOrderStatus().toString()).setHeader("Status").setSortable(true);
        grid.addComponentColumn(order -> {
            Button editButton = new Button("Edit", new Icon(VaadinIcon.EDIT));
            editButton.addClickListener(e -> openEditDialog(order));
            return editButton;
        }).setHeader("Actions");

        grid.setHeight("400px");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
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
        openDialogButton.addClickListener(e -> orderDialog.open()); // abre o diálogo
    }

    private void setupEditDialog() {
        editProductComboBox.setItems(productService.findAllProducts());
        editProductComboBox.setItemLabelGenerator(Product::getName);

        editQuantityField.setMin(1);
        editQuantityField.setStep(1);

        statusComboBox.setItems(PurchaseOrderStatus.values());

        Button updateButton = new Button("Update", e -> updateOrder());
        VerticalLayout layout = new VerticalLayout(
                editProductComboBox,
                editQuantityField,
                statusComboBox,
                updateButton);

        editDialog.setHeaderTitle("Edit Purchase Order");
        editDialog.add(layout);
    }

    private void openEditDialog(PurchaseOrder order) {
        editDialog.removeAll(); // Limpa conteúdo antigo;

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.setWidthFull();

        ComboBox<PurchaseOrderStatus> statusComboBox = new ComboBox<>("Status");
        statusComboBox.setItems(PurchaseOrderStatus.values());
        statusComboBox.setValue(order.getPurchaseOrderStatus());
        statusComboBox.setWidthFull();

        List<PurchaseOrderItemForm> itemForms = new ArrayList<>();
        for (PurchaseOrderItem item : order.getPurchaseItems()) {
            PurchaseOrderItemForm form = new PurchaseOrderItemForm(productService.findAllProducts());
            form.productField.setValue(item.getProduct());
            form.quantityField.setValue((double) item.getQuantity());
            form.priceField.setValue(item.getPrice().doubleValue());

            itemForms.add(form);

            FormLayout itemLayout = new FormLayout();
            itemLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));
            itemLayout.add(form.productField);
            itemLayout.add(form.quantityField);
            itemLayout.add(form.priceField);

            layout.add(itemLayout);
        }

        Button updateButton = new Button("Update", e -> {
            try {
                for (int i = 0; i < itemForms.size(); i++) {
                    PurchaseOrderItemForm form = itemForms.get(i);
                    PurchaseOrderItem item = order.getPurchaseItems().get(i);

                    item.setProduct(form.productField.getValue());
                    item.setQuantity(form.quantityField.getValue().intValue());
                    item.setPrice(BigDecimal.valueOf(form.priceField.getValue()));
                }

                purchaseOrderService.udpatePurchaseOrder(order);

                PurchaseOrderStatus selectedStatus = statusComboBox.getValue();
                PurchaseOrderStatus originalStatus = order.getPurchaseOrderStatus();

                if (selectedStatus == PurchaseOrderStatus.CANCELLED) {
                    purchaseOrderService.cancelPurchaseOrder(order);
                    Notification.show("Order cancelled.");
                } else if (selectedStatus == PurchaseOrderStatus.INVOICED) {
                    purchaseOrderService.finishPurchaseOrder(order);
                    Notification.show("Order finished.");
                }

                order.setPurchaseOrderStatus(originalStatus);
                refreshGrid();
                editDialog.close();
            } catch (Exception ex) {
                Notification.show("Error updating order: " + ex.getMessage());
            }
        });

        layout.add(statusComboBox, updateButton);
        editDialog.setHeaderTitle("Edit Purchase Order");
        editDialog.add(layout);
        editDialog.open();
    }

    private static class PurchaseOrderItemForm {
        ComboBox<Product> productField;
        NumberField quantityField;
        NumberField priceField;

        public PurchaseOrderItemForm(List<Product> products) {
            this.productField = new ComboBox<>("Product");
            this.productField.setItems(products);
            this.productField.setItemLabelGenerator(Product::getName);
            this.productField.setPlaceholder("Select a product");

            this.quantityField = new NumberField("Quantity");
            this.quantityField.setMin(1);
            this.quantityField.setStep(1);

            this.priceField = new NumberField("Price");
            this.priceField.setMin(0.01);
            this.priceField.setStep(0.01);
        }
    }

    private void updateOrder() {
        if (currentOrderEditing == null) {
            Notification.show("No order selected.");
            return;
        }

        PurchaseOrderStatus newStatus = statusComboBox.getValue();

        if (newStatus == null) {
            Notification.show("Status must be selected.");
            return;
        }

        try {
            currentOrderEditing.setPurchaseOrderStatus(newStatus);
            purchaseOrderService.udpatePurchaseOrder(currentOrderEditing);
            // purchaseOrderService.finishPurchaseOrder(currentOrderEditing);

            Notification.show("Order status updated successfully.");
            refreshGrid();
            editDialog.close();
        } catch (Exception e) {
            Notification.show("Error updating order: " + e.getMessage());
        }
    }

    private void setupOrderDialog() {
        FormLayout dialogFormLayout = new FormLayout();
        dialogFormLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        dialogFormLayout.add(productComboBox, quantityField, priceField, purchaserComboBox, addItemButton);

        Button confirmOrderButton = new Button("Confirm Order", e -> {
            createOrder();
            orderDialog.close();
        });

        VerticalLayout dialogContent = new VerticalLayout(dialogFormLayout, confirmOrderButton);
        dialogContent.setPadding(true);
        dialogContent.setSpacing(true);

        orderDialog.setHeaderTitle("Create Purchase Order");
        orderDialog.add(dialogContent);
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
        itemGrid.setItems(items);

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

            productComboBox.clear();
            quantityField.setValue(1.0);
            priceField.setValue(0.0);
            purchaserComboBox.clear();
            itemGrid.setItems(items);

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

        String id = order.getId().toString().toLowerCase();
        String purchaserName = order.getPurchaserId().getFullName().toLowerCase();

        return id.contains(currentSearchTerm) || purchaserName.contains(currentSearchTerm);
    }

}