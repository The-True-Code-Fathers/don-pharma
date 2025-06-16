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
    private final Button openDialogButton = new Button("Add to Storage");

    private final Grid<Storage> grid = new Grid<>(Storage.class, false);

    private String currentSearchTerm = "";

    public StorageView() {
        // Inicializa os serviços
        var productRepository = new ProductRepositoryImpl();
        var storageRepository = new StorageRepositoryImpl();
        this.storageService = new StorageService(storageRepository, productRepository);

        // Configura a aparência e comportamento do layout principal
        setSizeFull(); // 1. Faz o VerticalLayout ocupar toda a tela

        // Configura os componentes da UI
        setupSearchField();
        setupGrid();
        setupRedirectButton();

        // Monta o cabeçalho e adiciona os componentes ao layout
        HorizontalLayout header = new HorizontalLayout(openDialogButton, searchField);
        header.setWidthFull();
        header.expand(searchField); // Faz o campo de busca expandir

        add(header, grid);
        setFlexGrow(1, grid); // 1. Faz a grid expandir e ocupar o espaço restante

        // Carrega os dados na grid
        refreshGrid();
    }

    private void setupSearchField() {
        searchField.setPlaceholder("Search by SKU or Name..."); // 3. Placeholder atualizado
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
        // Configura as colunas da grid
        grid.addColumn(s -> s.getProduct().getSku()).setHeader("Product SKU")
                .setSortable(true) // 2. Coluna classificável
                .setAutoWidth(true);
        grid.addColumn(s -> s.getProduct().getName()).setHeader("Product Name")
                .setSortable(true) // 2. Coluna classificável
                .setAutoWidth(true);
        grid.addColumn(Storage::getProductQuantity).setHeader("Quantity")
                .setSortable(true) // 2. Coluna classificável
                .setAutoWidth(true);

        // Remove a altura fixa para permitir que a grid expanda
        // grid.setHeight("400px"); // 1. Linha removida
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

        // 3. Lógica de pesquisa atualizada para SKU e Nome
        boolean skuMatches = product.getSku() != null && product.getSku().toLowerCase().contains(currentSearchTerm);
        boolean nameMatches = product.getName() != null && product.getName().toLowerCase().contains(currentSearchTerm);

        return skuMatches || nameMatches;
    }
}