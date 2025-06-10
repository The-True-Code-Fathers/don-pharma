package com.codefathers.view;

import com.codefathers.model.dto.CreateShippingProviderDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.interfaces.ShippingProviderRepository;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.service.ShippingProviderService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Route("shipping-provider")
public class ShippingProviderView extends VerticalLayout {
    private  ShippingProviderService shippingProviderService;
    private Grid<ShippingProvider> grid = new Grid<>(ShippingProvider.class, false);

    public ShippingProviderView() {  // Remove o 'void' aqui
        // Instancia a cadeia de dependências corretamente
        ShippingProviderRepository repository = new ShippingProviderRepositoryImpl();
        this.shippingProviderService = new ShippingProviderService(repository);

        setupGrid();
        add(grid, createButtonsLayout());
        updateGrid();
    }

    private void setupGrid() {
        grid.addColumn(ShippingProvider::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(ShippingProvider::getName).setHeader("Nome").setAutoWidth(true);
        grid.addColumn(ShippingProvider::getCnpj).setHeader("CNPJ").setAutoWidth(true);
        grid.setWidthFull();
    }

    private HorizontalLayout createButtonsLayout() {
        Button addButton = new Button("Cadastrar Transportadora", e -> openCreateDialog());
        Button searchButton = new Button("Buscar por ID", e -> openSearchDialog());
        Button removeButton = new Button("Remover por ID", e -> openRemoveDialog());

        return new HorizontalLayout(addButton, searchButton, removeButton);
    }

    private void updateGrid() {
        List<ShippingProvider> providers = shippingProviderService.listAllShippingProviders();
        grid.setItems(providers);
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        TextField nameField = new TextField("Nome");
        TextField cnpjField = new TextField("CNPJ");
        TextField basePriceField = new TextField("Preço Base");
        TextField dailyCapacityField = new TextField("Capacidade Diária");

        TextArea shippingAreasField = new TextArea("Áreas de Atendimento");
        shippingAreasField.setPlaceholder("Exemplo: Área 1: SP, RJ; Área 2: MG, ES\nSepare cada área por ponto e vírgula ';' e os estados por vírgula ','");

        Button saveButton = new Button("Salvar", e -> {
            try {
                String name = nameField.getValue();
                String cnpj = cnpjField.getValue();
                BigDecimal basePrice = new BigDecimal(basePriceField.getValue());
                BigDecimal dailyCapacity = new BigDecimal(dailyCapacityField.getValue());

                List<ShippingArea> areas = parseShippingAreas(shippingAreasField.getValue());

                CreateShippingProviderDTO dto = CreateShippingProviderDTO.builder()
                        .name(name)
                        .cnpj(cnpj)
                        .basePrice(basePrice)
                        .dailyCapacity(dailyCapacity)
                        .shippingAreas(areas)
                        .build();

                shippingProviderService.registerShippingProvider(dto);
                Notification.show("Transportadora cadastrada com sucesso!", 3000, Notification.Position.MIDDLE);
                updateGrid();
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Erro ao cadastrar: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);

        VerticalLayout layout = new VerticalLayout(
                nameField, cnpjField, basePriceField, dailyCapacityField, shippingAreasField, buttons
        );

        dialog.add(layout);
        dialog.open();
    }

    private List<ShippingArea> parseShippingAreas(String input) {
        List<ShippingArea> areas = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) {
            return areas;
        }
        // Exemplo de input:
        // Área 1: SP, RJ; Área 2: MG, ES
        String[] parts = input.split(";");
        for (String part : parts) {
            String[] descAndStates = part.split(":");
            if (descAndStates.length == 2) {
                String desc = descAndStates[0].trim();
                String states = descAndStates[1].trim();
                ShippingArea area = new ShippingArea();
                area.setDescription(desc);
                area.setStates(states);
                areas.add(area);
            }
        }
        return areas;
    }

    private void openSearchDialog() {
        Dialog dialog = new Dialog();
        TextField idField = new TextField("Informe o ID da transportadora");
        Button searchButton = new Button("Buscar", e -> {
            try {
                UUID id = UUID.fromString(idField.getValue());
                ShippingProvider sp = shippingProviderService.searchShippingProviderPerId(id);
                if (sp != null) {
                    Notification.show("Transportadora encontrada: " + sp.toString(), 5000, Notification.Position.MIDDLE);
                } else {
                    Notification.show("Transportadora não encontrada.", 3000, Notification.Position.MIDDLE);
                }
                dialog.close();
            } catch (Exception ex) {
                Notification.show("ID inválido.", 3000, Notification.Position.MIDDLE);
            }
        });
        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        dialog.add(new VerticalLayout(idField, new HorizontalLayout(searchButton, cancelButton)));
        dialog.open();
    }

    private void openRemoveDialog() {
        Dialog dialog = new Dialog();
        TextField idField = new TextField("Informe o ID da transportadora para remover");
        Button removeButton = new Button("Remover", e -> {
            try {
                UUID id = UUID.fromString(idField.getValue());
                ShippingProvider removed = shippingProviderService.removeShippingProvider(id);
                if (removed != null) {
                    Notification.show("Transportadora removida com sucesso.", 3000, Notification.Position.MIDDLE);
                    updateGrid();
                } else {
                    Notification.show("Transportadora não encontrada.", 3000, Notification.Position.MIDDLE);
                }
                dialog.close();
            } catch (Exception ex) {
                Notification.show("ID inválido.", 3000, Notification.Position.MIDDLE);
            }
        });
        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        dialog.add(new VerticalLayout(idField, new HorizontalLayout(removeButton, cancelButton)));
        dialog.open();
    }
}

