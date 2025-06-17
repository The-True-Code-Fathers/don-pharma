package com.codefathers.view;

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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@PageTitle("Storage")
@Route("storage")
public class StorageView extends VerticalLayout {
    private final StorageService storageService;

    private final TextField searchField = new TextField();
    private final Button openDialogButton = new Button("Add to Storage", new Icon(VaadinIcon.PLUS));

    private final Grid<Storage> grid = new Grid<>(Storage.class, false);

    private String currentSearchTerm = "";

    public StorageView() {
        var productRepository = new ProductRepositoryImpl();
        var storageRepository = new StorageRepositoryImpl();
        this.storageService = new StorageService(storageRepository, productRepository);

        setSizeFull();

        setupSearchField();
        setupGrid();
        setupRedirectButton();

        HorizontalLayout header = new HorizontalLayout(openDialogButton, searchField);
        header.setWidthFull();

        add(header, grid);
        setFlexGrow(1, grid);
        refreshGrid();
    }

    private void setupSearchField() {
        searchField.setPlaceholder("Search by SKU or Name...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setClearButtonVisible(true);

        searchField.addValueChangeListener(e -> {
            currentSearchTerm = e.getValue() != null ? e.getValue().trim().toLowerCase() : "";
            refreshGrid();
        });
    }

    private void setupRedirectButton() {
        openDialogButton.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.navigate("purchaseOrder"));
        });
    }

    private void setupGrid() {
        grid.addColumn(s -> s.getProduct().getSku()).setHeader("Product SKU")
                .setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(s -> s.getProduct().getName()).setHeader("Product Name")
                .setSortable(true)
                .setAutoWidth(true);
        grid.addColumn(Storage::getProductQuantity).setHeader("Quantity")
                .setSortable(true)
                .setAutoWidth(true);

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
        if (currentSearchTerm.isEmpty()) {
            return true;
        }

        Product product = storage.getProduct();
        if (product == null) {
            return false;
        }

        boolean skuMatches = product.getSku() != null && product.getSku().toLowerCase().contains(currentSearchTerm);
        boolean nameMatches = product.getName() != null && product.getName().toLowerCase().contains(currentSearchTerm);

        return skuMatches || nameMatches;
    }
}