package com.codefathers.view;

import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.dto.UpdateProductDTO;
import com.codefathers.model.entity.Product;
import com.codefathers.repository.ProductRepositoryImpl;
import com.codefathers.service.ProductService;
import com.codefathers.util.ValidatorUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("products")
public class ProductsView extends VerticalLayout {

    private ProductService productService;

    private TextField sku = new TextField("SKU");
    private TextField name = new TextField("Name");
    private TextArea description = new TextArea("Description", "Optional");
    private BigDecimalField buyPrice = new BigDecimalField("Buy Price");
    private BigDecimalField sellPrice = new BigDecimalField("Sell Price");

    private Button saveButton = new Button("Save");
    private Button clearButton = new Button("Clear");

    private Grid<Product> grid = new Grid<>(Product.class, false);

    private Product currentProduct;

    public ProductsView() {
        // Instanciar repositório e service manualmente
        var productRepository = new ProductRepositoryImpl();

        this.productService = new ProductService(productRepository, ValidatorUtil.getValidator());

        setupForm();
        setupGrid();

        add(createFormLayout(), grid);
        updateGrid();
    }

    private void setupGrid() {
        grid.addColumn(Product::getSku).setHeader("SKU").setSortable(true).setAutoWidth(true);
        grid.addColumn(Product::getName).setHeader("Name").setSortable(true).setAutoWidth(true);
        grid.addColumn(Product::getDescription).setHeader("Description").setAutoWidth(true);
        grid.addColumn(Product::getBuyPrice).setHeader("Buy Price").setAutoWidth(true);
        grid.addColumn(Product::getSellPrice).setHeader("Sell Price").setAutoWidth(true);

        grid.asSingleSelect().addValueChangeListener(event -> {
            currentProduct = event.getValue();
            if (currentProduct != null) {
                populateForm(currentProduct);
            } else {
                clearForm();
            }
        });

        grid.setHeight("300px");
    }

    private void setupForm() {
        sku.setReadOnly(false);
        name.setReadOnly(false);
        saveButton.addClickListener(e -> saveProduct());
        clearButton.addClickListener(e -> clearForm());
    }

    private HorizontalLayout createFormLayout() {

        HorizontalLayout buttons = new HorizontalLayout(saveButton, clearButton);
        HorizontalLayout formLayout = new HorizontalLayout(sku, name, description, buyPrice, sellPrice, buttons);
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
                // Criar novo produto
                CreateProductDTO dto = CreateProductDTO.builder()
                        .sku(sku.getValue())
                        .name(name.getValue())
                        .description(description.getValue())
                        .buyPrice(buyPrice.getValue())
                        .sellPrice(sellPrice.getValue())
                        .build();

                productService.createProduct(dto);
                Notification.show("Product created");
            } else {
                // Atualizar produto existente
                UpdateProductDTO dto = UpdateProductDTO.builder()
                        .description(description.getValue())
                        .buyPrice(buyPrice.getValue())
                        .sellPrice(sellPrice.getValue())
                        .build();

                productService.updateProduct(currentProduct.getSku(), dto);
                Notification.show("Product updated");
            }
            updateGrid();
            clearForm();

        } catch (Exception ex) {
            Notification.show("Error: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            ex.printStackTrace();
        }
    }

    private void updateGrid() {
        grid.setItems(productService.findAllProducts());
    }
}


