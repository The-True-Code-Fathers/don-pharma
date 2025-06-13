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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
        setupItemGrid();
        setupGrid();
        setupForm();
        setupOrderDialog();
        setupEditDialog();

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setWidthFull();
        topLayout.setAlignItems(Alignment.END);

        searchField.setWidth("300px");
        topLayout.add(openDialogButton, searchField);

        add(topLayout, grid, orderDialog, editDialog);

        refreshGrid();
    }

    private void setupGrid() {
        grid.addColumn(po -> po.getId().toString()).setHeader("ID").setSortable(true);
        grid.addColumn(po -> po.getPurchaserId().getFullName()).setHeader("Purchaser").setSortable(true);
        grid.addColumn(po -> po.getPurchaseTotalProductAmount()).setHeader("Total Amount").setSortable(true);
        grid.addColumn(po -> po.getPurchaseTotalPriceAmount()).setHeader("Total Price Amount").setSortable(true);
        grid.addColumn(po -> po.getCreatedAt().toString()).setHeader("Created At").setSortable(true);
        grid.addColumn(po -> po.getPurchaseOrderStatus().toString()).setHeader("Status").setSortable(true);
        grid.setHeight("400px");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        grid.addItemDoubleClickListener(event -> {
            PurchaseOrder selectedOrder = event.getItem();
            showOrderDetails(selectedOrder);
        });
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
        openDialogButton.addClickListener(e -> orderDialog.open());
    }

    private void setupEditDialog() {
        editDialog.setHeaderTitle("Edit Purchase Order");
    }

    private void openEditDialog(PurchaseOrder order) {
        editDialog.removeAll();

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
                // Atualiza os itens do pedido
                for (int i = 0; i < itemForms.size(); i++) {
                    PurchaseOrderItemForm form = itemForms.get(i);
                    PurchaseOrderItem item = order.getPurchaseItems().get(i);

                    item.setProduct(form.productField.getValue());
                    item.setQuantity(form.quantityField.getValue().intValue());
                    item.setPrice(BigDecimal.valueOf(form.priceField.getValue()));
                }

                // Atualiza o pedido (recalcula totais)
                purchaseOrderService.udpatePurchaseOrder(order);

                // Processa mudança de status
                PurchaseOrderStatus selectedStatus = statusComboBox.getValue();
                if (selectedStatus != order.getPurchaseOrderStatus()) {
                    if (selectedStatus == PurchaseOrderStatus.CANCELLED) {
                        purchaseOrderService.cancelPurchaseOrder(order);
                        Notification.show("Order cancelled successfully.");
                    } else if (selectedStatus == PurchaseOrderStatus.INVOICED) {
                        purchaseOrderService.finishPurchaseOrder(order);
                        Notification.show("Order finished successfully.");
                    } else {
                        // Para outros status, apenas atualiza
                        order.setPurchaseOrderStatus(selectedStatus);
                        purchaseOrderService.update(order);
                        Notification.show("Order updated successfully.");
                    }
                } else {
                    Notification.show("Order updated successfully.");
                }

                refreshGrid();
                editDialog.close();
            } catch (Exception ex) {
                Notification.show("Error updating order: " + ex.getMessage());
            }
        });

        layout.add(statusComboBox, updateButton);
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

    private void setupOrderDialog() {
        FormLayout dialogFormLayout = new FormLayout();
        dialogFormLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        dialogFormLayout.add(productComboBox, quantityField, priceField, purchaserComboBox, addItemButton);

        Button confirmOrderButton = new Button("Confirm Order", e -> {
            createOrder();
        });

        Button closeButton = new Button("Close");
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(e -> {
            clearOrderForm();
            orderDialog.close();
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(confirmOrderButton, closeButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        buttonLayout.setSpacing(true);

        VerticalLayout dialogContent = new VerticalLayout(dialogFormLayout, itemGrid, buttonLayout);
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

        // Limpa os campos do item
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
            
            clearOrderForm();
            orderDialog.close();
            refreshGrid();
        } catch (Exception ex) {
            Notification.show("Error creating purchase order: " + ex.getMessage());
        }
    }

    private void clearOrderForm() {
        items.clear();
        productComboBox.clear();
        quantityField.setValue(1.0);
        priceField.setValue(0.0);
        purchaserComboBox.clear();
        itemGrid.setItems(items);
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

    private void setupItemGrid() {
        itemGrid.addColumn(item -> item.getProduct().getName()).setHeader("Product").setAutoWidth(true);
        itemGrid.addColumn(CreatePurchaseOrderItemDTO::getQuantity).setHeader("Quantity").setAutoWidth(true);
        itemGrid.addColumn(item -> item.getPrice().toString()).setHeader("Price").setAutoWidth(true);
        itemGrid.setHeight("200px");
    }

    private void showOrderDetails(PurchaseOrder order) {
        Dialog detailsDialog = new Dialog();
        detailsDialog.setHeaderTitle("Purchase Order Details");
        detailsDialog.setWidth("600px");
        detailsDialog.setHeight("500px");

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        FormLayout orderInfoLayout = new FormLayout();
        orderInfoLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        TextField idField = new TextField("Order ID");
        idField.setValue(order.getId().toString());
        idField.setReadOnly(true);

        TextField purchaserField = new TextField("Purchaser");
        purchaserField.setValue(order.getPurchaserId().getFullName());
        purchaserField.setReadOnly(true);

        TextField statusField = new TextField("Status");
        statusField.setValue(order.getPurchaseOrderStatus().toString());
        statusField.setReadOnly(true);

        TextField createdAtField = new TextField("Created At");
        createdAtField.setValue(order.getCreatedAt().toString());
        createdAtField.setReadOnly(true);

        TextField totalAmountField = new TextField("Total Amount");
        totalAmountField.setValue(String.valueOf(order.getPurchaseTotalProductAmount()));
        totalAmountField.setReadOnly(true);

        TextField totalPriceField = new TextField("Total Price");
        totalPriceField.setValue(order.getPurchaseTotalPriceAmount().toString());
        totalPriceField.setReadOnly(true);

        orderInfoLayout.add(idField, purchaserField, statusField, createdAtField, totalAmountField, totalPriceField);

        Grid<PurchaseOrderItem> itemsGrid = new Grid<>(PurchaseOrderItem.class, false);
        itemsGrid.addColumn(item -> item.getProduct().getName()).setHeader("Product").setAutoWidth(true);
        itemsGrid.addColumn(PurchaseOrderItem::getQuantity).setHeader("Quantity").setAutoWidth(true);
        itemsGrid.addColumn(item -> item.getPrice().toString()).setHeader("Unit Price").setAutoWidth(true);
        itemsGrid.addColumn(item -> {
            BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return total.toString();
        }).setHeader("Total").setAutoWidth(true);

        itemsGrid.setItems(order.getPurchaseItems());
        itemsGrid.setHeight("200px");

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();

        Button editButton = new Button("Edit", new Icon(VaadinIcon.EDIT));
        editButton.addClickListener(e -> {
            detailsDialog.close();
            openEditDialog(order);
        });

        // Desabilita edição para pedidos cancelados ou finalizados
        if (order.getPurchaseOrderStatus() == PurchaseOrderStatus.CANCELLED ||
            order.getPurchaseOrderStatus() == PurchaseOrderStatus.INVOICED) {
            editButton.setEnabled(false);
        }

        Button closeButton = new Button("Close");
        closeButton.addClickListener(e -> detailsDialog.close());

        buttonLayout.add(editButton, closeButton);

        mainLayout.add(
                new com.vaadin.flow.component.html.H4("Order Information"),
                orderInfoLayout,
                new com.vaadin.flow.component.html.H4("Items"),
                itemsGrid,
                buttonLayout
        );
        detailsDialog.add(mainLayout);
        detailsDialog.open();
    }
}