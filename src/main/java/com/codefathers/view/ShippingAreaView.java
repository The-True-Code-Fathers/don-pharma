package com.codefathers.view;

import com.codefathers.model.dto.CreateShippingAreaDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.implementations.ShippingAreaRepositoryImpl;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.service.ShippingAreaService;
import com.codefathers.service.ShippingProviderService;
import com.codefathers.util.CepUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
        grid.addColumn(ShippingArea::getCep).setHeader("CEP").setAutoWidth(true);

        grid.addItemDoubleClickListener(event -> openFormDialog(event.getItem()));
        grid.setHeight("300px");
        grid.setWidthFull();
        grid.getStyle().set("margin-top", "10px");
        grid.setId("custom-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);


    }

    private void openFormDialog(ShippingArea area) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        TextField descriptionField = new TextField("Descrição");
        TextArea statesField = new TextArea("Estados (separados por vírgula)");
        TextField cepField = new TextField("CEP de origem");

        ComboBox<ShippingProvider> providerComboBox = new ComboBox<>("Transportadora");
        providerComboBox.setItems(providerService.listAllShippingProviders());
        providerComboBox.setItemLabelGenerator(ShippingProvider::getName);

        Button buscarEstadoBtn = new Button("Buscar Estado", ev -> {
            try {
                String cep = cepField.getValue().replaceAll("[^0-9]", "");
                String uf = CepUtils.getStateByCep(cep);
                statesField.setValue(uf);
                Notification.show("Estado encontrado: " + uf);
            } catch (Exception ex) {
                Notification.show("Erro ao buscar estado: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        if (area != null) {
            descriptionField.setValue(area.getDescription());
            statesField.setValue(String.join(", ", area.getStates()));
            cepField.setValue(area.getCep() != null ? area.getCep() : "");
            providerComboBox.setValue(area.getShippingProvider());
        }

        Button saveButton = new Button(area == null ? "Cadastrar" : "Atualizar", e -> {
            try {
                String[] states = Arrays.stream(statesField.getValue().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toArray(String[]::new);

                ShippingProvider selectedProvider = providerComboBox.getValue();
                if (selectedProvider == null) {
                    Notification.show("Selecione uma transportadora.", 3000, Notification.Position.MIDDLE);
                    return;
                }

                if (area == null) {
                    CreateShippingAreaDTO dto = CreateShippingAreaDTO.builder()
                            .description(descriptionField.getValue())
                            .shippingProvider(selectedProvider)
                            .states(states)
                            .cep(cepField.getValue())
                            .build();
                    areaService.saveShippingArea(dto);
                    Notification.show("Área criada com sucesso!");
                } else {
                    ShippingArea updated = ShippingArea.builder()
                            .id(area.getId())
                            .description(descriptionField.getValue())
                            .shippingProvider(selectedProvider)
                            .states(states)
                            .cep(cepField.getValue())
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
                if (area != null) {
                    areaService.deleteShippingAreaById(area.getId());
                    Notification.show("Área deletada com sucesso!");
                    updateGrid();
                    dialog.close();
                }
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
                cepField,
                buscarEstadoBtn,
                new HorizontalLayout(saveButton, deleteButton, cancelButton)
        ));
        dialog.open();
    }

    private void updateGrid() {
        grid.setItems(areaService.findAllShippingAreas());
    }
}