package com.codefathers.view;

import com.codefathers.model.dto.CreateShippingAreaDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.implementations.ShippingAreaRepositoryImpl;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.service.ShippingAreaService;
import com.codefathers.service.ShippingProviderService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.List;

@Route("shipping-areas")
@PageTitle("Shipping Areas")
public class ShippingAreaView extends VerticalLayout {

    private final ShippingAreaService shippingAreaService;
    private final ShippingProviderService shippingProviderService;

    private final Grid<ShippingArea> grid = new Grid<>(ShippingArea.class);
    private final Button addButton = new Button("Nova Área de Entrega");

    public ShippingAreaView() {

        this.shippingAreaService = new ShippingAreaService(
                new ShippingAreaRepositoryImpl(),
                Validation.buildDefaultValidatorFactory().getValidator()
        );
        this.shippingProviderService = new ShippingProviderService(new ShippingProviderRepositoryImpl());

        configureGrid();
        configureAddButton();

        add(addButton, grid);
        updateGrid();
    }

    private void configureGrid() {
        grid.removeAllColumns();

        // Adicione colunas de forma explícita
        grid.addColumn(ShippingArea::getDescription)
                .setHeader("Descrição")
                .setAutoWidth(true);

        grid.addColumn(area -> area.getShippingProvider() != null ?
                        area.getShippingProvider().getName() : "N/A")
                .setHeader("Transportadora")
                .setAutoWidth(true);

        grid.addColumn(area -> String.join(", ", area.getStates()))
                .setHeader("Estados")
                .setAutoWidth(true);
    }

    private void updateGrid() {
        try {
            List<ShippingArea> areas = shippingAreaService.findAllShippingAreas();
            System.out.println("[DEBUG] Áreas carregadas: " + areas.size()); // Log para debug

            if (areas.isEmpty()) {
                Notification.show("Nenhuma área encontrada", 3000, Notification.Position.MIDDLE);
            } else {
                grid.setItems(areas);
                Notification.show("Dados atualizados", 2000, Notification.Position.BOTTOM_END);
            }
        } catch (Exception e) {
            Notification.show("Erro ao carregar: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }


    private void configureAddButton() {
        addButton.addClickListener(e -> openCreateDialog());
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("400px");

        FormLayout formLayout = new FormLayout();

        TextField descriptionField = new TextField("Descrição");
        descriptionField.setRequired(true);

        ComboBox<ShippingProvider> providerComboBox = new ComboBox<>("Transportadora");
        List<ShippingProvider> providers = shippingProviderService.listAllShippingProviders();
        providerComboBox.setItems(providers);
        providerComboBox.setItemLabelGenerator(ShippingProvider::getName);
        providerComboBox.setRequired(true);

        TextField statesField = new TextField("Estados (separados por vírgula)");
        statesField.setRequired(true);

        formLayout.add(descriptionField, providerComboBox, statesField);

        Button saveButton = new Button("Salvar", event -> {
            if (descriptionField.isEmpty() || providerComboBox.isEmpty() || statesField.isEmpty()) {
                Notification.show("Todos os campos são obrigatórios", 3000, Notification.Position.MIDDLE);
                return;
            }

            CreateShippingAreaDTO dto = CreateShippingAreaDTO.builder()
                    .description(descriptionField.getValue())
                    .shippingProvider(providerComboBox.getValue())
                    .states(statesField.getValue().split("\\s*,\\s*"))
                    .build();

            try {
                shippingAreaService.saveShippingArea(dto);
                Notification.show("Área de entrega salva com sucesso!");
                dialog.close();
                updateGrid();
            } catch (Exception ex) {
                Notification.show("Erro ao salvar: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);

        VerticalLayout dialogLayout = new VerticalLayout(formLayout, buttonsLayout);
        dialog.add(dialogLayout);
        dialog.open();
    }
}
