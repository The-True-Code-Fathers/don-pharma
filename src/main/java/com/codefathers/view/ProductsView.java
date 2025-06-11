package com.codefathers.view;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.dto.UpdateProductDTO;
import com.codefathers.model.entity.Product;
import com.codefathers.repository.ProductRepositoryImpl;
import com.codefathers.service.ProductService;
import com.codefathers.util.ValidationUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route("products")
public class ProductsView extends VerticalLayout {

    private ProductService productService;

    private TextField sku = new TextField("SKU");
    private TextField name = new TextField("Name");
    private TextArea description = new TextArea("Description", "Optional");
    private BigDecimalField buyPrice = new BigDecimalField("Buy Price");
    private BigDecimalField sellPrice = new BigDecimalField("Sell Price");

    private TextField searchField = new TextField();
    private Select<Integer> pageSizeSelect = new Select<>();

    private Button saveButton = new Button("Save");
    private Button clearButton = new Button("Clear");
    private Button setInactive = new Button("Set Inactive");
    private Button dialogButtonCreateProduct = new Button("Create");
    private Button dialogButtonUpdateProduct = new Button("Update");
    private Button closeDialog = new Button("Close");

    private Grid<Product> grid = new Grid<>(Product.class, false);
    private GridLazyDataView<Product> dataView;

    private com.vaadin.flow.component.checkbox.Checkbox showInactiveCheckbox =
            new com.vaadin.flow.component.checkbox.Checkbox("Show inactive products");

    private Dialog dialog = new Dialog();
    private Product currentProduct;

    // Cache para evitar consultas desnecessárias
    private String currentSearchTerm = "";
    private Boolean currentShowInactive = false;

    public ProductsView() {
        // Instanciar repositório e service manualmente
        var productRepository = new ProductRepositoryImpl();
        this.productService = new ProductService(productRepository, ValidationUtil.getValidator());

        setupSearchField();
//        setupPageSizeSelect();
        setupForm();
        setupGrid();
        setupDialog();
        setupLazyDataProvider();

        HorizontalLayout searchLayout = new HorizontalLayout(searchField, showInactiveCheckbox);
        searchLayout.setAlignItems(Alignment.CENTER);

        HorizontalLayout controlsLayout = new HorizontalLayout(dialogButtonCreateProduct, dialogButtonUpdateProduct);
        controlsLayout.setAlignItems(Alignment.CENTER);


        HorizontalLayout headerLayout = new HorizontalLayout(controlsLayout, searchLayout);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.EVENLY);

        add(headerLayout, grid);


        // Listener para o checkbox de produtos inativos
        showInactiveCheckbox.addValueChangeListener(e -> {
            currentShowInactive = e.getValue();
            dataView.refreshAll(); // 🔁 Recria o data provider
        });
        searchField.addValueChangeListener(e -> {
            currentSearchTerm = e.getValue().trim();
            dataView.refreshAll(); // 🔁 Recria o data provider
        });
        pageSizeSelect.addValueChangeListener(e -> {
            grid.setPageSize(e.getValue());
            dataView.refreshAll();
        });
    }

    private void setupSearchField() {
        searchField.setWidth("400px");
        searchField.setPlaceholder("Search by SKU, name...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setClearButtonVisible(true);

        // Adicionar delay para evitar muitas consultas
        searchField.addValueChangeListener(e -> {
            currentSearchTerm = e.getValue().trim();
            setupLazyDataProvider();
        });
    }

    private void setupDialog() {
        dialog.setHeaderTitle("Create or Update Product");
        dialog.setResizable(true);
        dialog.setDraggable(true);
        dialog.getElement().getStyle().set("width", "300px");
        dialog.getElement().getStyle().set("height", "200px");
        dialog.add(createFormLayout());
    }

    private void setupGrid() {
        grid.addColumn(Product::getSku).setHeader("SKU").setSortable(true).setAutoWidth(true);
        grid.addColumn(Product::getName).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(Product::getDescription).setHeader("Description").setAutoWidth(true);
        grid.addColumn(Product::getBuyPrice).setHeader("Buy Price").setAutoWidth(true);
        grid.addColumn(Product::getSellPrice).setHeader("Sell Price").setAutoWidth(true);
//        grid.addColumn(Product::isActive).setHeader("Active").setAutoWidth(true);

        grid.asSingleSelect().addValueChangeListener(event -> {
            currentProduct = event.getValue();
            if (currentProduct != null) {
                populateForm(currentProduct);
            } else {
                clearForm();
            }
        });

        grid.setAllRowsVisible(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
    }

    private List<Product> allProducts;

    private void setupLazyDataProvider() {

        allProducts = productService.findAllProducts();

        // Create the data provider with filtering and pagination logic
        CallbackDataProvider<Product, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    // Apply filtering on the full list first
                    Stream<Product> filteredStream = allProducts.stream()
                            .filter(this::matchesCurrentFilters); // Apply the current filters

                    // Paginate the filtered stream (skip and limit) within the context of the full dataset
                    return filteredStream
                            .skip(query.getOffset())  // Skip items based on the current page offset
                            .limit(query.getLimit())  // Limit the items according to the page size
                            .collect(Collectors.toList()) // Collect to a list so Vaadin can handle it
                            .stream(); // Convert the list back to a stream for the data provider
                },
                query -> {
                    // Count only the filtered items for pagination purposes
                    return (int) allProducts.stream()
                            .filter(this::matchesCurrentFilters) // Apply the same filters for counting
                            .count();
                }
        );

        dataView = grid.setItems(dataProvider);
        grid.getDataProvider().refreshAll();
    }


    private boolean matchesCurrentFilters(Product product) {
        // Filtro para produtos inativos
        if (!showInactiveCheckbox.getValue() && !product.isActive()) {
            return false;
        }

        // Filtro de busca
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

    private void setupForm() {
        sku.setWidth("350px");
        name.setWidth("350px");
        description.setWidth("350px");
        buyPrice.setWidth("350px");
        sellPrice.setWidth("350px");

        sku.setReadOnly(false);
        name.setReadOnly(false);

        dialogButtonCreateProduct.addClickListener(e -> {
            clearForm();
            dialog.open();
        });

        dialogButtonUpdateProduct.addClickListener(e -> {
            if (currentProduct != null) {
                dialog.open();
                populateForm(currentProduct);
            }
        });

        closeDialog.addClickListener(e -> dialog.close());
        setInactive.addClickListener(e -> {
            currentProduct.setActive(false);
            saveProduct();
        });
        saveButton.addClickListener(e -> saveProduct());
        clearButton.addClickListener(e -> clearForm());
    }

    private HorizontalLayout createFormLayout() {
        HorizontalLayout buttonsCreate = new HorizontalLayout(saveButton, clearButton, closeDialog);
        HorizontalLayout buttonsUpdate = new HorizontalLayout(saveButton, clearButton, closeDialog, setInactive);
        VerticalLayout formLayout = new VerticalLayout(sku, name, description, buyPrice, sellPrice, buttonsCreate, buttonsUpdate);
        formLayout.setWidth("400px");

        return new HorizontalLayout(formLayout);
    }

    private void populateForm(Product product) {
        sku.setValue(product.getSku());
        sku.setReadOnly(true);
        name.setValue(product.getName());
        name.setReadOnly(true);
        description.setValue(product.getDescription() != null ? product.getDescription() : "");
        buyPrice.setValue(product.getBuyPrice());
        sellPrice.setValue(product.getSellPrice());
    }

    private void clearForm() {
        currentProduct = null;
        sku.clear();
        sku.setReadOnly(false);
        name.clear();
        name.setReadOnly(false);
        description.clear();
        buyPrice.clear();
        sellPrice.clear();
        grid.asSingleSelect().clear();
    }

    private void saveProduct() {
        try {
            if (currentProduct == null) {
                CreateProductDTO dto = CreateProductDTO.builder()
                        .sku(sku.getValue())
                        .name(name.getValue())
                        .description(description.getValue())
                        .buyPrice(buyPrice.getValue())
                        .sellPrice(sellPrice.getValue())
                        .active(true)
                        .build();

                productService.createProduct(dto);
                Notification.show("Product created");
            } else {
                UpdateProductDTO dto = UpdateProductDTO.builder()
                        .description(description.getValue())
                        .buyPrice(buyPrice.getValue())
                        .sellPrice(sellPrice.getValue())
                        .build();

                productService.updateProduct(currentProduct.getSku(), dto);
                Notification.show("Product updated");
            }

            // Refresh dos dados após salvar
            setupLazyDataProvider();
            clearForm();
            dialog.close();

        } catch (Exception ex) {
            Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            ex.printStackTrace();
        }
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value != null && value.toLowerCase().contains(searchTerm);
    }
}