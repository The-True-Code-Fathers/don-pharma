package com.codefathers.view;

import com.codefathers.model.dto.CreateShippingAreaDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.ShippingAreaRepositoryImpl;
import com.codefathers.repository.ShippingProviderRepositoryImpl;
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
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("shipping-areas")
public class ShippingAreaView extends VerticalLayout {

    private final ShippingAreaService shippingAreaService;
    private final ShippingProviderService shippingProviderService;

    private final Grid<ShippingArea> grid = new Grid<>(ShippingArea.class, false);

    public ShippingAreaView() {
        var providerRepository = new ShippingProviderRepositoryImpl();
        var areaRepository = new ShippingAreaRepositoryImpl();
        var validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();

        this.shippingProviderService = new ShippingProviderService(providerRepository);
        this.shippingAreaService = new ShippingAreaService(areaRepository, validator);

        Button newAreaButton = new Button("Nova Área", e -> openEditDialog(null));

        setupGrid();

        add(newAreaButton, grid);
        updateGrid();
    }

    private void setupGrid() {
        grid.addColumn(area -> area.getShippingProvider().getName()).setHeader("Provedor");
        grid.addColumn(ShippingArea::getDescription).setHeader("Descrição");
        grid.addColumn(ShippingArea::getStates).setHeader("Estados Atendidos");
        grid.setHeight("300px");

        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));
    }

    private void openEditDialog(ShippingArea area) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        ComboBox<ShippingProvider> providerComboBox = new ComboBox<>("Provedor");
        providerComboBox.setItems(shippingProviderService.listAllShippingProviders());
        providerComboBox.setItemLabelGenerator(ShippingProvider::getName);
        if (area != null) providerComboBox.setValue(area.getShippingProvider());

        TextArea descriptionField = new TextArea("Descrição");
        descriptionField.setValue(area != null && area.getDescription() != null ? area.getDescription() : "");

        TextField statesField = new TextField("Estados Atendidos");
        statesField.setPlaceholder("Ex: SP, RJ");
        statesField.setValue(area != null ? area.getStates() : "");

        Button saveOrUpdateButton = new Button(area == null ? "Cadastrar" : "Atualizar", e -> {
            try {
                ShippingProvider provider = providerComboBox.getValue();
                if (provider == null) {
                    Notification.show("Selecione um provedor.");
                    return;
                }

                CreateShippingAreaDTO dto = new CreateShippingAreaDTO();
                dto.setShippingProvider(provider);
                dto.setDescription(descriptionField.getValue());
                dto.setStates(statesField.getValue());

                if (area == null) {
                    shippingAreaService.saveShippingArea(dto);
                    Notification.show("Área criada com sucesso!");
                } else {
                    area.setShippingProvider(provider);
                    area.setDescription(descriptionField.getValue());
                    area.setStates(statesField.getValue());
                    shippingAreaService.updateShippingArea(area);
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
            if (area != null) {
                try {
                    shippingAreaService.deleteShippingAreaById(area.getId());
                    Notification.show("Área deletada com sucesso!");
                    updateGrid();
                    dialog.close();
                } catch (Exception ex) {
                    Notification.show("Erro ao deletar: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                    ex.printStackTrace();
                }
            }
        });
        deleteButton.setVisible(area != null);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveOrUpdateButton, deleteButton, cancelButton);

        VerticalLayout dialogLayout = new VerticalLayout(
                providerComboBox, descriptionField, statesField, buttons
        );

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void updateGrid() {
        grid.setItems(shippingAreaService.findAllShippingAreas());
    }
}
