package com.codefathers.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.dto.UpdateProductDTO;
import com.codefathers.model.entity.OrderItem;
import com.codefathers.model.entity.Product;
import com.codefathers.model.entity.PurchaseOrderItem;
import com.codefathers.model.enums.MeasurementUnit;
import com.codefathers.repository.implementations.OrderItemRepositoryImpl;
import com.codefathers.repository.implementations.OrderRepositoryImpl;
import com.codefathers.repository.implementations.ProductRepositoryImpl;
import com.codefathers.repository.implementations.PurchaseOrderItemRepositoryImpl;
import com.codefathers.repository.implementations.PurchaseOrderRepositoryImpl;
import com.codefathers.service.OrderItemService;
import com.codefathers.service.ProductService;
import com.codefathers.service.PurchaseOrderItemService;
import com.codefathers.service.PurchaseOrderService;
import com.codefathers.util.AverageProductPriceUtil;
import com.codefathers.util.ValidatorUtil;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;

import java.util.List;

@PageTitle("Products")
@Route("products")
public class ProductsView extends VerticalLayout {

    private ProductService productService;
    private OrderItemService orderItemService;
    private PurchaseOrderItemService purchaseOrderItemService;
    private PurchaseOrderService purchaseOrderService;
    private TextField createSku = new TextField("SKU", "A123");
    private TextField createName = new TextField("Name");
    private TextArea createDescription = new TextArea("Description", "Optional");

    private TextField updateSku = new TextField("SKU");
    private TextField updateName = new TextField("Name");
    private TextArea updateDescription = new TextArea("Description", "Optional");

    private TextField searchField = new TextField();
    private Select<Integer> pageSizeSelect = new Select<>();

    private ComboBox<MeasurementUnit> createUnit = new ComboBox<>("Unit");
    private ComboBox<MeasurementUnit> updateUnit = new ComboBox<>("Unit");

    private Button createSaveButton = new Button("Save");
    private Button createClearButton = new Button("Clear");
    private Button createCloseButton = new Button("Close");

    private Button updateSaveButton = new Button("Update");
    private Button updateClearButton = new Button("Clear");
    private Button updateCloseButton = new Button("Close");
    private Button setInactiveButton = new Button("Inactivate Product");

    private Button dialogButtonCreateProduct = new Button("Create Product");

    private Grid<Product> grid = new Grid<>(Product.class, false);
    private GridLazyDataView<Product> dataView;
    private Column<Product> statusColumn;

    private com.vaadin.flow.component.checkbox.Checkbox showInactiveCheckbox =
            new com.vaadin.flow.component.checkbox.Checkbox("Show inactive products");

    private Dialog createDialog = new Dialog();
    private Dialog updateDialog = new Dialog();
    private Product currentProduct;

    private String currentSearchTerm = "";
    private Boolean currentShowInactive = false;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ProductsView() {
        var productRepository = new ProductRepositoryImpl();
        var orderItemRepository = new OrderItemRepositoryImpl();
        var orderRepository = new OrderRepositoryImpl();
        var purchaseOrderItemRepository = new PurchaseOrderItemRepositoryImpl();
        var purchaseOrderRepository = new PurchaseOrderRepositoryImpl();
        this.productService = new ProductService(productRepository, ValidatorUtil.getValidator());
        this.orderItemService = new OrderItemService(orderItemRepository, orderRepository);
        this.purchaseOrderItemService = new PurchaseOrderItemService(purchaseOrderItemRepository, purchaseOrderRepository, ValidatorUtil.getValidator());
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        setupSearchField();
        setupGrid();
        setupCreateDialog();
        setupUpdateDialog();
        setupEventListeners();
        setupCreateComboBox(createUnit);
        setupUpdateComboBox(updateUnit);

        HorizontalLayout leftLayout = new HorizontalLayout(dialogButtonCreateProduct, searchField);
        leftLayout.setAlignItems(Alignment.CENTER);
        leftLayout.setSpacing(true);

        HorizontalLayout rightLayout = new HorizontalLayout(showInactiveCheckbox);
        rightLayout.setAlignItems(Alignment.CENTER);

        HorizontalLayout headerLayout = new HorizontalLayout(leftLayout, rightLayout);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setWidth("100%");

        add(headerLayout, grid);
        setupLazyDataProvider();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        refreshGrid();
    }

    private void refreshGrid() {
        if (dataView != null) {
            dataView.refreshAll();
        }
        Notification.show("Grid updated", 2000, Notification.Position.BOTTOM_END);
    }

    private void setupSearchField() {
        searchField.setWidth("400px");
        searchField.setPlaceholder("Search by SKU, name...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setClearButtonVisible(true);

        searchField.addValueChangeListener(e -> {
            currentSearchTerm = e.getValue().trim();
            dataView.refreshAll();
        });
    }

    private void setupCreateDialog() {
        createDialog.setHeaderTitle("Create Product");
        createDialog.setDraggable(true);
        createDialog.getElement().getStyle().set("width", "400px");

        createSku.setWidthFull();
        createName.setWidthFull();
        createUnit.setWidthFull();
        createDescription.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout(createSku, createName, createUnit, createDescription);
        formLayout.setSpacing(true);
        formLayout.setPadding(false);

        HorizontalLayout buttonsLayout = new HorizontalLayout(createSaveButton, createClearButton, createCloseButton);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonsLayout.setWidthFull();

        createDialog.add(new VerticalLayout(formLayout, buttonsLayout));
    }

    private void setupUpdateDialog() {
        updateDialog.setHeaderTitle("Update Product");
        updateDialog.setDraggable(true);
        updateDialog.getElement().getStyle().set("width", "400px");

        updateSku.setWidthFull();
        updateName.setWidthFull();
        updateUnit.setWidthFull();
        updateDescription.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout(updateSku, updateName, updateUnit, updateDescription);
        formLayout.setSpacing(true);
        formLayout.setPadding(false);

        HorizontalLayout buttonsLayout = new HorizontalLayout(updateSaveButton, updateClearButton, updateCloseButton, setInactiveButton);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonsLayout.setWidthFull();

        updateDialog.add(new VerticalLayout(formLayout, buttonsLayout));
    }

    private void setupEventListeners() {
        dialogButtonCreateProduct.addClickListener(e -> {
            clearCreateForm();
            createDialog.open();
        });

        createSaveButton.addClickListener(e -> saveNewProduct());
        createClearButton.addClickListener(e -> clearCreateForm());
        createCloseButton.addClickListener(e -> createDialog.close());

        updateSaveButton.addClickListener(e -> updateExistingProduct());
        updateClearButton.addClickListener(e -> clearUpdateForm());
        updateCloseButton.addClickListener(e -> updateDialog.close());
        setInactiveButton.addClickListener(e -> toggleProductActive());

    }

    private void setupGrid() {
        grid.addColumn(Product::getSku).setHeader("SKU").setSortable(true).setFlexGrow(1);
        grid.addColumn(Product::getName).setHeader("Name").setSortable(true).setFlexGrow(2);
        grid.addColumn(Product::getMeasurementUnit).setHeader("UM").setSortable(true).setFlexGrow(0).setWidth("80px");
        grid.addColumn(Product::getDescription).setHeader("Description").setFlexGrow(3);

        grid.addColumn(product -> {
            return product.getCreatedAt() != null ? product.getCreatedAt().format(dateFormatter) : "";
        }).setHeader("Created At").setSortable(true).setFlexGrow(1);

        grid.addColumn(this::calculateWeightedAverageBuyPrice)
            .setHeader("Avg Buy Price")
            .setFlexGrow(1).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);

        grid.addColumn(this::calculateWeightedAverageSellPrice)
            .setHeader("Avg Sell Price")
            .setFlexGrow(1).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);

        statusColumn = grid.addColumn(product -> product.isActive() ? "Active" : "Inactive")
                .setHeader("Status")
                .setFlexGrow(0).setWidth("100px");
        statusColumn.setVisible(false);

        grid.addItemClickListener(event -> {
            if (event.getClickCount() == 2) {
                currentProduct = event.getItem();
                populateUpdateForm(currentProduct);
                updateDialog.open();
            }
        });

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setPageSize(20);
        grid.setWidth("100%");
        grid.setHeightFull();
    }

    private void setupCreateComboBox(ComboBox<MeasurementUnit> createUnit) {
        createUnit.setAllowCustomValue(false);
        createUnit.setItems(MeasurementUnit.values());
        createUnit.setPlaceholder("Unit");
    }

    private void setupUpdateComboBox(ComboBox<MeasurementUnit> updateUnit) {
        updateUnit.setAllowCustomValue(false);
        updateUnit.setItems(MeasurementUnit.values());
        updateUnit.setPlaceholder("Unit");
    }

    private void setupLazyDataProvider() {
        CallbackDataProvider<Product, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();
                    List<Product> allProducts = productService.findAllProducts();
                    return allProducts.stream()
                            .filter(this::matchesCurrentFilters)
                            .skip(offset)
                            .limit(limit);
                },
                query -> {
                    List<Product> allProducts = productService.findAllProducts();
                    return (int) allProducts.stream()
                            .filter(this::matchesCurrentFilters)
                            .count();
                }
        );

        dataView = grid.setItems(dataProvider);

        showInactiveCheckbox.addValueChangeListener(e -> {
            currentShowInactive = e.getValue();
            statusColumn.setVisible(e.getValue());
            dataView.refreshAll();
        });
    }

    private boolean matchesCurrentFilters(Product product) {
        if (!showInactiveCheckbox.getValue() && !product.isActive()) {
            return false;
        }
        if (currentSearchTerm.isEmpty()) {
            return true;
        }
        String searchTermLower = currentSearchTerm.toLowerCase();
        return matchesTerm(product.getSku(), searchTermLower)
            || matchesTerm(product.getName(), searchTermLower)
            || matchesTerm(product.getDescription(), searchTermLower);
    }

    private String calculateWeightedAverageBuyPrice(Product product) {
        return AverageProductPriceUtil.calculateWeightedAverageBuyPrice(product);
        }

    private String calculateWeightedAverageSellPrice(Product product) {
        return AverageProductPriceUtil.calculateWeightedAverageSellPrice(product);
    }

    private void populateUpdateForm(Product product) {
        currentProduct = product;
        updateSku.setValue(product.getSku());
        updateSku.setReadOnly(true);
        updateName.setValue(product.getName());
        updateName.setReadOnly(true);
        updateUnit.setValue(product.getMeasurementUnit());
        updateDescription.setValue(product.getDescription() != null ? product.getDescription() : "");
        setInactiveButton.setText(product.isActive() ? "Set Inactive" : "Set Active");
    }

    private void clearCreateForm() {
        createSku.clear();
        createName.clear();
        createUnit.clear();
        createDescription.clear();
    }

    private void clearUpdateForm() {
        updateSku.clear();
        updateSku.setReadOnly(false);
        updateName.clear();
        updateName.setReadOnly(false);
        updateUnit.clear();
        updateDescription.clear();
        currentProduct = null;
    }

    private void saveNewProduct() {
        try {
            CreateProductDTO dto = CreateProductDTO.builder()
                    .sku(createSku.getValue())
                    .name(createName.getValue())
                    .description(createDescription.getValue())
                    .active(true)
                    .measurementUnit(createUnit.getValue())
                    .build();

            productService.createProduct(dto);
            Notification.show("Product created successfully!");

            refreshGrid();
            clearCreateForm();
            createDialog.close();
        } catch (Exception ex) {
            Notification.show("Error creating product: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void updateExistingProduct() {
        try {
            if (currentProduct == null) {
                Notification.show("No product selected for update");
                return;
            }
            UpdateProductDTO dto = UpdateProductDTO.builder()
                    .description(updateDescription.getValue())
                    .measurementUnit(updateUnit.getValue())
                    .active(currentProduct.isActive())
                    .build();

            productService.updateProduct(currentProduct.getSku(), dto);
            Notification.show("Product updated successfully!");

            refreshGrid();
            clearUpdateForm();
            updateDialog.close();
        } catch (Exception ex) {
            Notification.show("Error updating product: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void toggleProductActive() {
        if (currentProduct != null) {
            boolean newStatus = !currentProduct.isActive();
            currentProduct.setActive(newStatus);
            updateExistingProduct();
        }
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value != null && !value.isBlank() && value.toLowerCase().contains(searchTerm);
    }
}