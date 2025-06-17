package com.codefathers.view;

import com.codefathers.model.dto.CreateShippingOrderDTO;
import com.codefathers.model.entity.ShippingOrder;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.model.enums.ShippingServiceStatus;
import com.codefathers.repository.implementations.ShippingOrderRepositoryImpl;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.service.ShippingOrderService;
import com.codefathers.service.ShippingProviderService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@PageTitle("Shipping Orders")
@Route("shipping-orders")
public class ShippingOrderView extends VerticalLayout {

    private final ShippingOrderService orderService;
    private final ShippingProviderService providerService;
    private final Grid<ShippingOrder> grid = new Grid<>(ShippingOrder.class, false);
    private final Checkbox showAllCheckbox = new Checkbox("Show all");

    public ShippingOrderView() {
        this.providerService = new ShippingProviderService(new ShippingProviderRepositoryImpl());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        this.orderService = new ShippingOrderService(
                new ShippingOrderRepositoryImpl(),
                new ShippingProviderRepositoryImpl(),
                validator
        );

        Button newButton = new Button("New Order", e -> openFormDialog(null));
        showAllCheckbox.addValueChangeListener(e -> updateGrid(showAllCheckbox.getValue()));

        HorizontalLayout topLayout = new HorizontalLayout(newButton, showAllCheckbox);
        topLayout.setWidthFull();
        topLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        setupGrid();
        add(topLayout, grid);
        updateGrid(false);
    }

    private void setupGrid() {
        grid.removeAllColumns();

        grid.addColumn(order -> order.getShippingProvider().getName()).setHeader("Provider").setAutoWidth(true);
        grid.addColumn(ShippingOrder::getDestinationState).setHeader("Destination State").setAutoWidth(true);
        grid.addColumn(ShippingOrder::getDestinationCity).setHeader("Destination City").setAutoWidth(true);
        grid.addColumn(order -> String.format("%.2f kg", order.getWeight())).setHeader("Weight").setAutoWidth(true);
        grid.addColumn(ShippingOrder::getStatus).setHeader("Status").setAutoWidth(true);
        grid.addColumn(ShippingOrder::getEstimatedDeliveryDays).setHeader("Estimated Days").setAutoWidth(true);
        grid.addColumn(ShippingOrder::getShipmentDate).setHeader("Shipment Date").setAutoWidth(true);
        grid.addColumn(ShippingOrder::getDeliveryDate).setHeader("Delivery Date").setAutoWidth(true);
        grid.addColumn(order -> String.format("R$ %.2f", order.getShippingCost())).setHeader("Shipping Cost").setAutoWidth(true);
        grid.addColumn(order -> order.isActive() ? "Active" : "Inactive").setHeader("Active Status").setAutoWidth(true);
        grid.addColumn(ShippingOrder::getCreatedAt).setHeader("Created At").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("300px");
        grid.setWidthFull();
        grid.getStyle().set("margin-top", "10px");

        grid.addItemDoubleClickListener(event -> {
            ShippingOrder item = event.getItem();
            if (item != null) {
                openFormDialog(item);
            } else {
                Notification.show("Please select a valid item", 3000, Notification.Position.MIDDLE);
            }
        });
    }

    private void openFormDialog(ShippingOrder order) {
        Dialog dialog = new Dialog();
        dialog.setWidth("480px");
        dialog.setHeight("650px");

        List<ShippingProvider> allProviders;
        try {
            allProviders = providerService.listAllShippingProviders();
        } catch (Exception ex) {
            Notification.show("Error loading providers: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            allProviders = List.of();
        }

        ComboBox<ShippingProvider> providerCombo = new ComboBox<>("Provider");
        providerCombo.setItems(allProviders);
        providerCombo.setItemLabelGenerator(ShippingProvider::getName);
        providerCombo.setWidthFull();

        TextField stateField = new TextField("Destination State");
        stateField.setPlaceholder("Ex: SP");
        stateField.setWidthFull();

        TextField cityField = new TextField("Destination City");
        cityField.setPlaceholder("Ex: São Paulo");
        cityField.setWidthFull();

        NumberField weightField = new NumberField("Weight (kg)");
        weightField.setPlaceholder("Ex: 2.5");
        weightField.setWidthFull();

        ComboBox<ShippingServiceStatus> statusCombo = new ComboBox<>("Status");
        statusCombo.setItems(ShippingServiceStatus.values());
        statusCombo.setWidthFull();

        IntegerField estimatedDaysField = new IntegerField("Estimated Delivery Days");
        estimatedDaysField.setPlaceholder("Ex: 5");
        estimatedDaysField.setWidthFull();

        DatePicker shipmentDatePicker = new DatePicker("Shipment Date");
        shipmentDatePicker.setWidthFull();

        DatePicker deliveryDatePicker = new DatePicker("Delivery Date");
        deliveryDatePicker.setWidthFull();

        NumberField costField = new NumberField("Cost");
        costField.setPrefixComponent(new Span("R$"));
        costField.setWidthFull();

        List<ShippingProvider> finalAllProviders = allProviders;
        stateField.addValueChangeListener(event -> {
            String estado = event.getValue();
            if (estado == null || estado.isBlank()) {
                providerCombo.setItems(finalAllProviders);
            } else {
                List<ShippingProvider> filtered = finalAllProviders.stream()
                        .filter(p -> p.getServiceStates() != null &&
                                p.getServiceStates().stream()
                                        .anyMatch(s -> s.equalsIgnoreCase(estado.trim())))
                        .toList();
                providerCombo.setItems(filtered);

                ShippingProvider selected = providerCombo.getValue();
                if (selected != null && !filtered.contains(selected)) {
                    providerCombo.clear();
                }
            }
        });

        if (order != null) {
            providerCombo.setValue(order.getShippingProvider());
            stateField.setValue(order.getDestinationState());
            cityField.setValue(order.getDestinationCity());
            weightField.setValue(order.getWeight() != null ? order.getWeight().doubleValue() : null);
            statusCombo.setValue(order.getStatus());
            estimatedDaysField.setValue(order.getEstimatedDeliveryDays());
            shipmentDatePicker.setValue(order.getShipmentDate());
            deliveryDatePicker.setValue(order.getDeliveryDate());
            costField.setValue(order.getShippingCost() != null ? order.getShippingCost().doubleValue() : null);
        }

        Button saveButton = new Button(order == null ? "Create" : "Update", e -> {
            try {
                CreateShippingOrderDTO dto = buildOrderDTO(
                        providerCombo.getValue(),
                        stateField.getValue(),
                        cityField.getValue(),
                        weightField.getValue(),
                        statusCombo.getValue(),
                        estimatedDaysField.getValue(),
                        shipmentDatePicker.getValue(),
                        deliveryDatePicker.getValue(),
                        costField.getValue()
                );

                if (order != null) {
                    orderService.updateOrder(order.getId(), dto, order.isActive());
                    Notification.show("Order updated successfully!");
                } else {
                    orderService.createOrder(dto);
                    Notification.show("Order created successfully!");
                }

                updateGrid(showAllCheckbox.getValue());
                dialog.close();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        Button toggleStatusButton = new Button(order != null && order.isActive() ? "Deactivate" : "Activate");
        if (order != null) {
            final ShippingOrder currentOrder = order;
            toggleStatusButton.addClickListener(e -> {
                final String actionStr = currentOrder.isActive() ? "deactivate" : "activate";

                ConfirmDialog confirm = new ConfirmDialog(
                        "Confirm " + (currentOrder.isActive() ? "Deactivation" : "Activation"),
                        "Are you sure you want to " + actionStr + " this order?",
                        "Confirm",
                        confirmEvent -> {
                            try {
                                orderService.updateOrder(currentOrder.getId(),
                                        CreateShippingOrderDTO.builder()
                                                .shippingProviderId(currentOrder.getShippingProvider().getId())
                                                .destinationState(currentOrder.getDestinationState())
                                                .destinationCity(currentOrder.getDestinationCity())
                                                .weight(currentOrder.getWeight())
                                                .status(currentOrder.getStatus())
                                                .estimatedDeliveryDays(currentOrder.getEstimatedDeliveryDays())
                                                .shipmentDate(currentOrder.getShipmentDate())
                                                .deliveryDate(currentOrder.getDeliveryDate())
                                                .shippingCost(currentOrder.getShippingCost())
                                                .build(),
                                        !currentOrder.isActive());

                                Notification.show("Order " + (!currentOrder.isActive() ? "activated" : "deactivated") + " successfully!");
                                updateGrid(showAllCheckbox.getValue());
                                dialog.close();
                            } catch (Exception ex) {
                                showError(ex);
                            }
                        },
                        "Cancel",
                        cancelEvent -> {}
                );
                confirm.open();
            });
        }

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        VerticalLayout formLayout = new VerticalLayout(
                providerCombo,
                stateField,
                cityField,
                weightField,
                statusCombo,
                estimatedDaysField,
                shipmentDatePicker,
                deliveryDatePicker,
                costField,
                new HorizontalLayout(saveButton, toggleStatusButton, cancelButton)
        );
        formLayout.setPadding(false);
        formLayout.setSpacing(true);
        formLayout.setWidthFull();

        dialog.add(formLayout);
        dialog.open();
    }


    private CreateShippingOrderDTO buildOrderDTO(ShippingProvider provider,
                                                 String state,
                                                 String city,
                                                 Double weight,
                                                 ShippingServiceStatus status,
                                                 Integer estimatedDays,
                                                 LocalDate shipmentDate,
                                                 LocalDate deliveryDate,
                                                 Double cost) {
        if (provider == null) throw new IllegalArgumentException("Provider is required");
        if (state == null || state.isBlank()) throw new IllegalArgumentException("State is required");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("City is required");
        if (weight == null || weight <= 0) throw new IllegalArgumentException("Weight must be positive");
        if (status == null) throw new IllegalArgumentException("Status is required");
        if (estimatedDays == null || estimatedDays <= 0) throw new IllegalArgumentException("Estimated days must be positive");
        if (cost == null || cost < 0) throw new IllegalArgumentException("Cost cannot be negative");

        return CreateShippingOrderDTO.builder()
                .shippingProviderId(provider.getId())
                .destinationState(state)
                .destinationCity(city)
                .weight(BigDecimal.valueOf(weight))
                .status(status)
                .estimatedDeliveryDays(estimatedDays)
                .shipmentDate(shipmentDate)
                .deliveryDate(deliveryDate)
                .shippingCost(BigDecimal.valueOf(cost))
                .build();
    }

    private void showError(Exception e) {
        Notification.show("Error: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        e.printStackTrace();
    }

    private void updateGrid(boolean showAll) {
        if (showAll) {
            grid.setItems(orderService.listAllShippingOrdersIncludingInactive());
        } else {
            grid.setItems(orderService.listActiveShippingOrders()
                    .stream()
                    .filter(ShippingOrder::isActive)
                    .toList());
        }
    }

    private void updateGrid() {
        updateGrid(false);
    }
}
