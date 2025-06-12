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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.validation.Validation;

import java.util.Arrays;
import java.util.List;

@Route("shipping-areas")
@PageTitle("Shipping Areas")
public class ShippingAreaView extends VerticalLayout {

    private final ShippingAreaService areaService;
    private final ShippingProviderService providerService;
    private final Grid<ShippingArea> grid = new Grid<>(ShippingArea.class, false);

    public ShippingAreaView() {
        this.areaService = new ShippingAreaService(new ShippingAreaRepositoryImpl(), Validation.buildDefaultValidatorFactory().getValidator());
        this.providerService = new ShippingProviderService(new ShippingProviderRepositoryImpl());

        Button newButton = new Button("Nova Área de Entrega", e -> openFormDialog(null));

        setupGrid();
        add(newButton, grid);
        updateGrid();
    }

    private void setupGrid() {
        grid.removeAllColumns();
        grid.addColumn(ShippingArea::getDescription).setHeader("Descrição").setAutoWidth(true);
        grid.addColumn(area -> area.getShippingProvider() != null ? area.getShippingProvider().getName() : "Nenhuma")
                .setHeader("Transportadora").setAutoWidth(true);
        grid.addColumn(area -> String.join(", ", area.getStates())).setHeader("Estados").setAutoWidth(true);

        grid.addItemDoubleClickListener(event -> openFormDialog(event.getItem()));
        grid.setHeight("300px");
        grid.setWidthFull();
        grid.getStyle().set("margin-top", "10px");
    }

    private void openFormDialog(ShippingArea area) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        TextField descriptionField = new TextField("Descrição");
        TextArea statesField = new TextArea("Estados (separados por vírgula)");

        // ComboBox de transportadoras
        ComboBox<ShippingProvider> providerComboBox = new ComboBox<>("Transportadora");
        List<ShippingProvider> allProviders = providerService.listAllShippingProviders();
        providerComboBox.setItems(allProviders);
        providerComboBox.setItemLabelGenerator(ShippingProvider::getName);

        // Preenchimento em caso de edição
        if (area != null) {
            descriptionField.setValue(area.getDescription());
            statesField.setValue(String.join(", ", area.getStates()));
            if (area.getShippingProvider() != null) {
                providerComboBox.setValue(area.getShippingProvider());
            }
        }

        Button saveButton = new Button(area == null ? "Cadastrar" : "Atualizar", e -> {
            try {
                String[] states = Arrays.stream(statesField.getValue().split(","))
                        .map(String::trim)
                        .toArray(String[]::new);

                ShippingProvider selectedProvider = providerComboBox.getValue();
                if (selectedProvider == null) {
                    Notification.show("Selecione uma transportadora.", 3000, Notification.Position.MIDDLE);
                    return;
                }

                CreateShippingAreaDTO dto = CreateShippingAreaDTO.builder()
                        .description(descriptionField.getValue())
                        .shippingProvider(selectedProvider)
                        .states(states)
                        .build();

                if (area == null) {
                    areaService.saveShippingArea(dto);
                    Notification.show("Área criada com sucesso!");
                } else {
                    ShippingArea updated = ShippingArea.builder()
                            .id(area.getId())
                            .description(dto.getDescription())
                            .shippingProvider(dto.getShippingProvider())
                            .states(dto.getStates())
                            .build();
                    areaService.updateShippingArea(updated);
                    Notification.show("Área atualizada com sucesso!");
                }
                updateGrid();
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                ex.printStackTrace();
            }
        });

        Button deleteButton = new Button("Deletar", e -> {
            try {
                areaService.deleteShippingAreaById(area.getId());
                Notification.show("Área deletada com sucesso!");
                updateGrid();
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Erro ao deletar: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                ex.printStackTrace();
            }
        });
        deleteButton.setVisible(area != null);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        dialog.add(new VerticalLayout(
                descriptionField,
                providerComboBox,
                statesField,
                new HorizontalLayout(saveButton, deleteButton, cancelButton)
        ));
        dialog.open();
    }

    private void updateGrid() {
        grid.setItems(areaService.findAllShippingAreas());
    }
}
