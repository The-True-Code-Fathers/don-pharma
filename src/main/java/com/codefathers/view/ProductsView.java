package com.codefathers.view;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.dto.UpdateProductDTO;
import com.codefathers.model.entity.Product;
import com.codefathers.repository.implementations.ProductRepositoryImpl;
import com.codefathers.service.ProductService;
import com.codefathers.util.ValidatorUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;

import java.util.List;

@Route("products")
public class ProductsView extends VerticalLayout {

    private ProductService productService;

    // Form fields - CREATE NEW INSTANCES FOR EACH DIALOG
    private TextField createSku = new TextField("SKU");
    private TextField createName = new TextField("Name");
    private TextArea createDescription = new TextArea("Description", "Optional");
    private BigDecimalField createBuyPrice = new BigDecimalField("Buy Price");
    private BigDecimalField createSellPrice = new BigDecimalField("Sell Price");

    private TextField updateSku = new TextField("SKU");
    private TextField updateName = new TextField("Name");
    private TextArea updateDescription = new TextArea("Description", "Optional");
    private BigDecimalField updateBuyPrice = new BigDecimalField("Buy Price");
    private BigDecimalField updateSellPrice = new BigDecimalField("Sell Price");

    private TextField searchField = new TextField();
    private Select<Integer> pageSizeSelect = new Select<>();

    // Buttons for create dialog
    private Button createSaveButton = new Button("Save");
    private Button createClearButton = new Button("Clear");
    private Button createCloseButton = new Button("Close");

    // Buttons for update dialog
    private Button updateSaveButton = new Button("Update");
    private Button updateClearButton = new Button("Clear");
    private Button updateCloseButton = new Button("Close");
    private Button setInactiveButton = new Button("Set Inactive");

    private Button dialogButtonCreateProduct = new Button("Create Product");

    private Grid<Product> grid = new Grid<>(Product.class, false);
    private GridLazyDataView<Product> dataView;
    private Column<Product> statusColumn;

    private com.vaadin.flow.component.checkbox.Checkbox showInactiveCheckbox =
            new com.vaadin.flow.component.checkbox.Checkbox("Show inactive products");

    private Dialog createDialog = new Dialog();
    private Dialog updateDialog = new Dialog();
    private Product currentProduct;

    // Cache para evitar consultas desnecessárias
    private String currentSearchTerm = "";
    private Boolean currentShowInactive = false;

    public ProductsView() {
        // Instanciar repositório e service manualmente
        var productRepository = new ProductRepositoryImpl();
        this.productService = new ProductService(productRepository, ValidatorUtil.getValidator());

        setupSearchField();
        setupGrid();
        setupCreateDialog();
        setupUpdateDialog();
        setupEventListeners();

        HorizontalLayout leftLayout = new HorizontalLayout(dialogButtonCreateProduct, searchField);
        leftLayout.setAlignItems(Alignment.CENTER);
        leftLayout.setSpacing(true);

        HorizontalLayout rightLayout = new HorizontalLayout(showInactiveCheckbox);
        rightLayout.setAlignItems(Alignment.CENTER);

        HorizontalLayout headerLayout = new HorizontalLayout(leftLayout, rightLayout);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setWidth("80%");

        add(headerLayout, grid);
        setupLazyDataProvider();
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
        createDialog.getElement().getStyle().set("height", "350px");

        // Setup form fields
        createSku.setWidth("350px");
        createName.setWidth("350px");
        createDescription.setWidth("350px");
        createBuyPrice.setWidth("350px");
        createSellPrice.setWidth("350px");

        // Create form layout
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.add(createSku, createName, createDescription, createBuyPrice, createSellPrice);
        formLayout.setSpacing(true);
        formLayout.setPadding(true);

        // Create buttons layout
        HorizontalLayout buttonsLayout = new HorizontalLayout(createSaveButton, createClearButton, createCloseButton);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.EVENLY);

        // Main layout
        VerticalLayout mainLayout = new VerticalLayout(formLayout, buttonsLayout);
        mainLayout.setSpacing(true);
        mainLayout.setPadding(true);

        createDialog.add(mainLayout);
    }

    private void setupUpdateDialog() {
        updateDialog.setHeaderTitle("Update Product");
        updateDialog.setDraggable(true);
        updateDialog.getElement().getStyle().set("width", "300px");
        updateDialog.getElement().getStyle().set("height", "250px");

        // Setup form fields
        updateSku.setWidth("350px");
        updateName.setWidth("350px");
        updateDescription.setWidth("350px");
        updateBuyPrice.setWidth("350px");
        updateSellPrice.setWidth("350px");

        // Create form layout
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.add(updateSku, updateName, updateDescription, updateBuyPrice, updateSellPrice);
        formLayout.setSpacing(true);
        formLayout.setPadding(true);

        // Create buttons layout
        HorizontalLayout buttonsLayout = new HorizontalLayout(updateSaveButton, updateClearButton, updateCloseButton, setInactiveButton);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.EVENLY);

        // Main layout
        VerticalLayout mainLayout = new VerticalLayout(formLayout, buttonsLayout);
        mainLayout.setSpacing(true);
        mainLayout.setPadding(true);

        updateDialog.add(mainLayout);
    }

    private void setupEventListeners() {
        // Create dialog events
        dialogButtonCreateProduct.addClickListener(e -> {
            clearCreateForm();
            createDialog.open();
        });

        createSaveButton.addClickListener(e -> saveNewProduct());
        createClearButton.addClickListener(e -> clearCreateForm());
        createCloseButton.addClickListener(e -> createDialog.close());

        // Update dialog events
        updateSaveButton.addClickListener(e -> updateExistingProduct());
        updateClearButton.addClickListener(e -> clearUpdateForm());
        updateCloseButton.addClickListener(e -> updateDialog.close());
        setInactiveButton.addClickListener(e -> toggleProductActive());
    }

    private void setupGrid() {
        grid.addColumn(Product::getSku).setHeader("SKU").setSortable(true).setAutoWidth(true);
        grid.addColumn(Product::getName).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(Product::getDescription).setHeader("Description").setAutoWidth(true);
        grid.addColumn(Product::getBuyPrice).setHeader("Buy Price").setAutoWidth(true);
        grid.addColumn(Product::getSellPrice).setHeader("Sell Price").setAutoWidth(true);

        statusColumn = grid.addColumn(product -> product.isActive() ? "Active" : "Inactive")
                .setHeader("Status")
                .setAutoWidth(true);
        statusColumn.setVisible(false);

        grid.addItemClickListener(event -> {
            if (event.getClickCount() == 2) {
                currentProduct = event.getItem();
                populateUpdateForm(currentProduct);
                updateDialog.open();
            }
        });

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setPageSize(10);
        grid.setWidth("80%");
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

        boolean matchesSku = matchesTerm(product.getSku(), searchTermLower);
        boolean matchesName = matchesTerm(product.getName(), searchTermLower);
        boolean matchesDescription = matchesTerm(product.getDescription(), searchTermLower);

        boolean matchesBuyPrice = product.getBuyPrice() != null &&
                product.getBuyPrice().toString().toLowerCase().contains(searchTermLower);
        boolean matchesSellPrice = product.getSellPrice() != null &&
                product.getSellPrice().toString().toLowerCase().contains(searchTermLower);

        return matchesSku || matchesName || matchesDescription || matchesBuyPrice || matchesSellPrice;
    }

    private void populateUpdateForm(Product product) {
        updateSku.setValue(product.getSku());
        updateSku.setReadOnly(true);
        updateName.setValue(product.getName());
        updateName.setReadOnly(true);
        updateDescription.setValue(product.getDescription() != null ? product.getDescription() : "");
        updateBuyPrice.setValue(product.getBuyPrice());
        updateSellPrice.setValue(product.getSellPrice());

        // Update button text based on product status
        setInactiveButton.setText(product.isActive() ? "Set Inactive" : "Set Active");
    }

    private void clearCreateForm() {
        createSku.clear();
        createSku.setReadOnly(false);
        createName.clear();
        createName.setReadOnly(false);
        createDescription.clear();
        createBuyPrice.clear();
        createSellPrice.clear();
    }

    private void clearUpdateForm() {
        updateSku.clear();
        updateSku.setReadOnly(false);
        updateName.clear();
        updateName.setReadOnly(false);
        updateDescription.clear();
        updateBuyPrice.clear();
        updateSellPrice.clear();
        currentProduct = null;
    }

    private void saveNewProduct() {
        try {
            CreateProductDTO dto = CreateProductDTO.builder()
                    .sku(createSku.getValue())
                    .name(createName.getValue())
                    .description(createDescription.getValue())
                    .buyPrice(createBuyPrice.getValue())
                    .sellPrice(createSellPrice.getValue())
                    .active(true) // New products are active by default
                    .build();

            productService.createProduct(dto);
            Notification.show("Product created successfully!");

            dataView.refreshAll();
            clearCreateForm();
            createDialog.close();

        } catch (Exception ex) {
            Notification.show("Error creating product: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            ex.printStackTrace();
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
                    .buyPrice(updateBuyPrice.getValue())
                    .sellPrice(updateSellPrice.getValue())
                    .active(currentProduct.isActive())
                    .build();

            productService.updateProduct(currentProduct.getSku(), dto);
            Notification.show("Product updated successfully!");

            dataView.refreshAll();
            clearUpdateForm();
            updateDialog.close();

        } catch (Exception ex) {
            Notification.show("Error updating product: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            ex.printStackTrace();
        }
    }

    private void toggleProductActive() {
        if (currentProduct != null) {
            currentProduct.setActive(!currentProduct.isActive());
            setInactiveButton.setText(currentProduct.isActive() ? "Set Inactive" : "Set Active");
            updateExistingProduct();
        }
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value != null && value.toLowerCase().contains(searchTerm);
    }
}