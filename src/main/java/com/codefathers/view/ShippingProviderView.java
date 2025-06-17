package com.codefathers.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.codefathers.model.dto.AtualizarStatusShippingProviderDTO;
import com.codefathers.model.dto.CreateShippingProviderDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.service.ShippingProviderService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("shipping-providers")
public class ShippingProviderView extends VerticalLayout {

    private final ShippingProviderService service;
    private final Grid<ShippingProvider> grid = new Grid<>(ShippingProvider.class, false);
    private final Checkbox checkboxShowInactives = new Checkbox("Show Inactives");

    public ShippingProviderView() {
        this.service = new ShippingProviderService(new ShippingProviderRepositoryImpl());

        Button newButton = new Button("New Shipping Provider", e -> openFormDialog(null));

        checkboxShowInactives.addValueChangeListener(event -> updateGrid());

        HorizontalLayout topBar = new HorizontalLayout(newButton, checkboxShowInactives);
        topBar.setWidthFull();
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        setupGrid();

        add(topBar, grid);
        updateGrid();
    }

    private void setupGrid() {
        grid.removeAllColumns();

        grid.addColumn(ShippingProvider::getName)
                .setHeader("Name")
                .setAutoWidth(true);

        grid.addColumn(sp -> formatCNPJ(sp.getCnpj()))
                .setHeader("CNPJ")
                .setAutoWidth(true);

        grid.addColumn(sp -> String.format("R$ %.2f", sp.getBasePrice()))
                .setHeader("Base Price")
                .setAutoWidth(true);

        grid.addColumn(sp -> sp.getDailyCapacity() != null ? sp.getDailyCapacity().toString() : "0")
                .setHeader("Daily Capacity")
                .setAutoWidth(true);

        grid.addColumn(sp -> {
                    if (sp.getShippingAreas() == null || sp.getShippingAreas().isEmpty()) return "Empty";
                    return sp.getShippingAreas().stream()
                            .flatMap(area -> area.getStates().stream())
                            .distinct()
                            .collect(Collectors.joining(", "));
                })
                .setHeader("Covered States")
                .setAutoWidth(true);

        grid.addColumn(sp -> sp.isActive() ? "Active" : "Inactive")
                .setHeader("Status")
                .setAutoWidth(true);

        grid.setId("custom-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("300px");
        grid.setWidthFull();
        grid.getStyle().set("margin-top", "10px");

        grid.addItemDoubleClickListener(event -> {
            ShippingProvider item = event.getItem();
            if (item != null) {
                openFormDialog(item);
            } else {
                Notification.show("Select a valid item", 3000, Notification.Position.MIDDLE);
            }
        });
    }

    private String formatCNPJ(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) return cnpj;
        return String.format("%s.%s.%s/%s-%s",
                cnpj.substring(0, 2),
                cnpj.substring(2, 5),
                cnpj.substring(5, 8),
                cnpj.substring(8, 12),
                cnpj.substring(12));
    }

    private void openFormDialog(ShippingProvider provider) {
        Dialog dialog = new Dialog();
        dialog.setWidth("480px");

        TextField nameField = new TextField("Name");
        nameField.setPlaceholder("Enter the name...");
        nameField.setWidthFull();

        TextField cnpjField = new TextField("CNPJ");
        cnpjField.setPlaceholder("Numbers only");
        cnpjField.setWidthFull();

        TextField basePriceField = new TextField("Base Price");
        basePriceField.setWidthFull();
        basePriceField.setPlaceholder("Ex: 199.90");
        basePriceField.setPrefixComponent(new Span("R$"));

        IntegerField dailyCapacityField = new IntegerField("Daily Capacity");
        dailyCapacityField.setPlaceholder("Ex: 50");
        dailyCapacityField.setWidthFull();
        dailyCapacityField.setMin(0);

        TextArea areasField = new TextArea("Coverage Areas");
        areasField.setPlaceholder("Ex: North: AM, PA; South: RS, SC");
        areasField.setWidthFull();

        if (provider != null) {
            nameField.setValue(provider.getName() != null ? provider.getName() : "");
            cnpjField.setValue(provider.getCnpj() != null ? provider.getCnpj() : "");
            basePriceField.setValue(provider.getBasePrice() != null ? provider.getBasePrice().toString() : "");
            dailyCapacityField.setValue(provider.getDailyCapacity() != null ? provider.getDailyCapacity().intValue() : 0);

            StringBuilder sb = new StringBuilder();
            if (provider.getShippingAreas() != null) {
                for (ShippingArea area : provider.getShippingAreas()) {
                    sb.append(area.getDescription()).append(": ").append(String.join(", ", area.getStates())).append("; ");
                }
            }
            areasField.setValue(sb.toString().trim());
        }

        Button saveButton = new Button(provider == null ? "Register" : "Update", e -> {
            try {
                CreateShippingProviderDTO dto = buildDTO(
                        nameField.getValue(),
                        cnpjField.getValue(),
                        basePriceField.getValue(),
                        dailyCapacityField.getValue(),
                        areasField.getValue()
                );

                if (provider != null) {
                    dto.setId(provider.getId());
                    service.updateShippingProvider(dto);
                    Notification.show("Updated successfully!", 3000, Notification.Position.TOP_CENTER);
                } else {
                    service.registerShippingProvider(dto);
                    Notification.show("Registered successfully!", 3000, Notification.Position.TOP_CENTER);
                }
                updateGrid();
                dialog.close();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        Button toggleStatusButton = new Button(provider != null && provider.isActive() ? "Deactivate" : "Activate", e -> {
            ConfirmDialog confirm = new ConfirmDialog();
            boolean currentlyActive = provider != null && provider.isActive();
            confirm.setHeader(currentlyActive ? "Confirm Deactivation" : "Confirm Activation");
            confirm.setText("Are you sure you want to " + (currentlyActive ? "deactivate" : "activate") + " this shipping provider?");
            confirm.setConfirmText("Confirm");
            confirm.setCancelText("Cancel");

            confirm.addConfirmListener(confirmEvent -> {
                try {
                    AtualizarStatusShippingProviderDTO dto = AtualizarStatusShippingProviderDTO.builder()
                            .id(provider.getId())
                            .active(!currentlyActive)
                            .build();

                    service.atualizarStatusTransportadora(dto);

                    Notification.show("Shipping provider " + (dto.isActive() ? "activated" : "deactivated") + " successfully!", 3000, Notification.Position.TOP_CENTER);
                    updateGrid();
                    dialog.close();
                } catch (Exception ex) {
                    showError(ex);
                }
            });

            confirm.open();
        });
        toggleStatusButton.setVisible(provider != null);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, toggleStatusButton, cancelButton);
        buttons.setSpacing(true);

        VerticalLayout formLayout = new VerticalLayout(
                nameField,
                cnpjField,
                basePriceField,
                dailyCapacityField,
                areasField,
                buttons
        );
        formLayout.setPadding(false);
        formLayout.setSpacing(true);
        formLayout.setWidthFull();

        dialog.add(formLayout);
        dialog.open();
    }

    private CreateShippingProviderDTO buildDTO(String name, String cnpj, String priceStr, Integer capacity, String areasText) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required.");
        if (cnpj == null || cnpj.isBlank()) throw new IllegalArgumentException("CNPJ is required.");
        if (cnpj.length() != 14 || !cnpj.matches("\\d{14}")) throw new IllegalArgumentException("CNPJ must contain exactly 14 numeric digits.");

        BigDecimal basePrice;
        try {
            basePrice = new BigDecimal(priceStr);
            if (basePrice.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Base Price must be non-negative.");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Base Price.");
        }

        if (capacity == null || capacity < 0) throw new IllegalArgumentException("Daily Capacity must be zero or positive.");
        BigDecimal dailyCapacity = new BigDecimal(capacity);

        return CreateShippingProviderDTO.builder()
                .name(name.trim())
                .cnpj(cnpj.trim())
                .basePrice(basePrice)
                .dailyCapacity(dailyCapacity)
                .shippingAreas(parseAreas(areasText))
                .build();
    }

    private List<ShippingArea> parseAreas(String input) {
        List<ShippingArea> areas = new ArrayList<>();
        if (input == null || input.isBlank()) return areas;

        Arrays.stream(input.split(";"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .forEach(item -> {
                    String[] parts = item.split(":", 2);
                    if (parts.length == 2) {
                        ShippingArea area = new ShippingArea();
                        area.setDescription(parts[0].trim());
                        List<String> states = Arrays.stream(parts[1].trim().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
                        area.setStates(states);
                        areas.add(area);
                    }
                });

        return areas;
    }

    private void showError(Exception e) {
        Notification.show("Error: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        e.printStackTrace();
    }

    private void updateGrid() {
        if (checkboxShowInactives.getValue()) {
            grid.setItems(service.listAllShippingProvidersIncludingInactive());
        } else {
            grid.setItems(service.listAllShippingProviders());
        }
    }
}
