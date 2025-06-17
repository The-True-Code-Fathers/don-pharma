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
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
    private final Checkbox showAllCheckbox = new Checkbox("Show all areas (active and inactive)");

    public ShippingAreaView() {
        this.areaService = new ShippingAreaService(
                new ShippingAreaRepositoryImpl(),
                Validation.buildDefaultValidatorFactory().getValidator()
        );
        this.providerService = new ShippingProviderService(new ShippingProviderRepositoryImpl());

        Button newButton = new Button("New Shipping Area", e -> openFormDialog(null));
        newButton.setWidth("200px");

        setupGrid();

        showAllCheckbox.addValueChangeListener(e -> updateGrid());

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setWidthFull();
        topLayout.add(newButton, showAllCheckbox);
        topLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        topLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        add(topLayout, grid);
        updateGrid();
    }

    private void setupGrid() {
        grid.removeAllColumns();
        grid.addColumn(ShippingArea::getDescription).setHeader("Description").setAutoWidth(true);
        grid.addColumn(area -> area.getShippingProvider() != null ? area.getShippingProvider().getName() : "None")
                .setHeader("Shipping Provider").setAutoWidth(true);
        grid.addColumn(area -> String.join(", ", area.getStates())).setHeader("States").setAutoWidth(true);
        grid.addColumn(ShippingArea::getCep).setHeader("Origin ZIP Code").setAutoWidth(true);
        grid.addColumn(area -> area.isActive() ? "Active" : "Inactive").setHeader("Status").setAutoWidth(true);

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

        TextField descriptionField = new TextField("Description");
        descriptionField.setWidthFull();

        TextArea statesField = new TextArea("States (separated by commas)");
        statesField.setWidthFull();

        TextField cepField = new TextField("Origin ZIP Code");
        cepField.setWidthFull();

        ComboBox<ShippingProvider> providerComboBox = new ComboBox<>("Shipping Provider");
        providerComboBox.setItems(providerService.listAllShippingProviders());
        providerComboBox.setItemLabelGenerator(ShippingProvider::getName);
        providerComboBox.setWidthFull();

        Button buscarEstadoBtn = new Button("Find State", ev -> {
            try {
                String cep = cepField.getValue().replaceAll("[^0-9]", "");
                String uf = CepUtils.getStateByCep(cep);
                statesField.setValue(uf);
                Notification.show("State found: " + uf);
            } catch (Exception ex) {
                Notification.show("Error finding state: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        buscarEstadoBtn.setWidthFull();

        if (area != null) {
            descriptionField.setValue(area.getDescription());
            statesField.setValue(String.join(", ", area.getStates()));
            cepField.setValue(area.getCep() != null ? area.getCep() : "");
            providerComboBox.setValue(area.getShippingProvider());
        }

        Button saveButton = new Button(area == null ? "Create" : "Update", e -> {
            try {
                if (descriptionField.isEmpty() || statesField.isEmpty() || cepField.isEmpty()) {
                    Notification.show("All fields must be filled.", 3000, Notification.Position.MIDDLE);
                    return;
                }

                List<String> states = Arrays.stream(statesField.getValue().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();

                ShippingProvider selectedProvider = providerComboBox.getValue();
                if (selectedProvider == null) {
                    Notification.show("Please select a shipping provider.", 3000, Notification.Position.MIDDLE);
                    return;
                }

                if (area == null) {
                    CreateShippingAreaDTO dto = CreateShippingAreaDTO.builder()
                            .description(descriptionField.getValue())
                            .shippingProvider(selectedProvider)
                            .states(states.toArray(new String[0]))
                            .cep(cepField.getValue())
                            .build();
                    areaService.saveShippingArea(dto);
                    Notification.show("Shipping area successfully created!");
                } else {
                    ShippingArea updated = ShippingArea.builder()
                            .id(area.getId())
                            .description(descriptionField.getValue())
                            .shippingProvider(selectedProvider)
                            .states(states)
                            .cep(cepField.getValue())
                            .active(area.isActive())
                            .build();
                    areaService.updateShippingArea(updated);
                    Notification.show("Shipping area successfully updated!");
                }

                updateGrid();
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                ex.printStackTrace();
            }
        });

        Button toggleStatusButton = new Button(
                (area != null && area.isActive()) ? "Deactivate" : "Activate",
                e -> {
                    try {
                        if (area != null) {
                            areaService.atualizarStatusShippingArea(area.getId());
                            Notification.show("Status successfully updated!");
                            updateGrid();
                            dialog.close();
                        }
                    } catch (Exception ex) {
                        Notification.show("Error updating status: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                        ex.printStackTrace();
                    }
                }
        );
        toggleStatusButton.setVisible(area != null);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, toggleStatusButton, cancelButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);

        VerticalLayout formLayout = new VerticalLayout(
                descriptionField,
                providerComboBox,
                statesField,
                cepField,
                buscarEstadoBtn,
                buttonLayout
        );
        formLayout.setWidthFull();
        formLayout.setSpacing(true);

        dialog.add(formLayout);
        dialog.open();
    }

    private void updateGrid() {
        if (showAllCheckbox.getValue()) {
            grid.setItems(areaService.findAllShippingAreas());
        } else {
            grid.setItems(areaService.findActiveShippingAreas());
        }
    }
}
