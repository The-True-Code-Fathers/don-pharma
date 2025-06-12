package com.codefathers.view;

import java.util.List;

import com.codefathers.model.entity.Product;
import com.codefathers.model.entity.Storage;
import com.codefathers.repository.implementations.ProductRepositoryImpl;
import com.codefathers.repository.implementations.StorageRepositoryImpl;
import com.codefathers.service.StorageService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

@Route("storage")
public class StorageView extends VerticalLayout {

    private final StorageService storageService;

    private final TextField searchField = new TextField();
    private final Button openDialogButton = new Button("Add to Storage");

    private final Grid<Storage> grid = new Grid<>(Storage.class, false);

    private String currentSearchTerm = "";

    public StorageView() {
        var productRepository = new ProductRepositoryImpl();
        var storageRepository = new StorageRepositoryImpl();
        this.storageService = new StorageService(storageRepository, productRepository);

        setupSearchField();
        setupGrid();
        setupRedirectButton();

        HorizontalLayout header = new HorizontalLayout(openDialogButton, searchField);
        add(header, grid);

        refreshGrid();
    }

    private void setupSearchField() {
        searchField.setWidth("300px");
        searchField.setPlaceholder("Search by SKU...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setClearButtonVisible(true);

        searchField.addValueChangeListener(e -> {
            currentSearchTerm = e.getValue().trim().toLowerCase();
            refreshGrid();
        });
    }

    private void setupRedirectButton() {
        openDialogButton.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.navigate("productOrder")); // <- redireciona para a rota "productOrder"
        });
    }

    private void setupGrid() {
        grid.addColumn(s -> s.getProduct().getSku()).setHeader("Product SKU").setSortable(true).setAutoWidth(true);
        grid.addColumn(s -> s.getProduct().getName()).setHeader("Product Name").setAutoWidth(true);
        grid.addColumn(Storage::getProductQuantity).setHeader("Quantity").setAutoWidth(true);

        grid.setHeight("400px");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
    }

    private void refreshGrid() {
        List<Storage> storages = storageService.getAllStorages();

        List<Storage> filtered = storages.stream()
                .filter(this::matchesFilter)
                .toList();

        grid.setItems(filtered);
    }

    private boolean matchesFilter(Storage storage) {
        if (currentSearchTerm.isEmpty())
            return true;
        Product product = storage.getProduct();
        return product != null && product.getSku().toLowerCase().contains(currentSearchTerm);
    }
}
