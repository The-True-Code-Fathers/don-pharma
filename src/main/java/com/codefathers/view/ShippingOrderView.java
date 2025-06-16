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
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@PageTitle("Shipping Orders")
@Route("shipping-orders")
public class ShippingOrderView extends VerticalLayout {

    private final ShippingOrderService orderService;
    private final ShippingProviderService providerService;
    private final Grid<ShippingOrder> grid = new Grid<>(ShippingOrder.class, false);

    public ShippingOrderView() {
        this.providerService = new ShippingProviderService(new ShippingProviderRepositoryImpl());
        this.orderService = new ShippingOrderService(
                new ShippingOrderRepositoryImpl(),
                new ShippingProviderRepositoryImpl(),
                null // Validator pode ser nulo ou ajustado conforme necessidade
        );

        Button newButton = new Button("Novo Pedido", e -> openFormDialog(null));

        setupGrid();
        add(newButton, grid);
        updateGrid();
    }

    private void setupGrid() {
        grid.removeAllColumns();

        grid.addColumn(order -> order.getShippingProvider().getName())
                .setHeader("Transportadora")
                .setAutoWidth(true);

        grid.addColumn(ShippingOrder::getDestinationState)
                .setHeader("Estado Destino")
                .setAutoWidth(true);

        grid.addColumn(ShippingOrder::getDestinationCity)
                .setHeader("Cidade Destino")
                .setAutoWidth(true);

        grid.addColumn(order -> String.format("%.2f kg", order.getWeight()))
                .setHeader("Peso")
                .setAutoWidth(true);

        grid.addColumn(ShippingOrder::getStatus)
                .setHeader("Status")
                .setAutoWidth(true);

        grid.addColumn(ShippingOrder::getEstimatedDeliveryDays)
                .setHeader("Dias Estimados")
                .setAutoWidth(true);

        grid.addColumn(ShippingOrder::getShipmentDate)
                .setHeader("Data Envio")
                .setAutoWidth(true);

        grid.addColumn(ShippingOrder::getDeliveryDate)
                .setHeader("Data Entrega")
                .setAutoWidth(true);

        grid.addColumn(order -> String.format("R$ %.2f", order.getShippingCost()))
                .setHeader("Custo")
                .setAutoWidth(true);

        grid.addColumn(order -> order.isActive() ? "Ativo" : "Inativo")
                .setHeader("Status Ativo")
                .setAutoWidth(true);

        grid.addColumn(ShippingOrder::getCreatedAt)
                .setHeader("Criado Em")
                .setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("300px");
        grid.setWidthFull();
        grid.getStyle().set("margin-top", "10px");

        grid.addItemDoubleClickListener(event -> {
            try {
                ShippingOrder item = event.getItem();
                if (item != null) {
                    openFormDialog(item);
                } else {
                    Notification.show("Selecione um item válido", 3000, Notification.Position.MIDDLE);
                }
            } catch (Exception e) {
                Notification.show("Erro ao abrir editor: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                e.printStackTrace();
            }
        });
    }

    private void openFormDialog(ShippingOrder order) {
        Dialog dialog = new Dialog();
        dialog.setWidth("480px");
        dialog.setHeight("650px"); // Altura para garantir boa visualização

        // Seção de informações básicas
        ComboBox<ShippingProvider> providerCombo = new ComboBox<>("Transportadora");
        try {
            List<ShippingProvider> providers = providerService.listAllShippingProviders();
            if (providers == null) {
                providers = List.of(); // lista vazia para evitar null
            }
            providerCombo.setItems(providers);
        } catch (Exception ex) {
            Notification.show("Erro ao carregar transportadoras: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            providerCombo.setItems(List.of());
        }
        providerCombo.setItemLabelGenerator(ShippingProvider::getName);
        providerCombo.setWidthFull();

        TextField stateField = new TextField("Estado Destino");
        stateField.setPlaceholder("Ex: SP");
        stateField.setWidthFull();

        TextField cityField = new TextField("Cidade Destino");
        cityField.setPlaceholder("Ex: São Paulo");
        cityField.setWidthFull();

        NumberField weightField = new NumberField("Peso (kg)");
        weightField.setPlaceholder("Ex: 2.5");
        weightField.setWidthFull();

        // Seção de status e datas
        ComboBox<ShippingServiceStatus> statusCombo = new ComboBox<>("Status");
        statusCombo.setItems(ShippingServiceStatus.values());
        statusCombo.setWidthFull();

        IntegerField estimatedDaysField = new IntegerField("Dias Estimados");
        estimatedDaysField.setPlaceholder("Ex: 5");
        estimatedDaysField.setWidthFull();

        DatePicker shipmentDatePicker = new DatePicker("Data de Envio");
        shipmentDatePicker.setWidthFull();

        DatePicker deliveryDatePicker = new DatePicker("Data de Entrega");
        deliveryDatePicker.setWidthFull();

        // Seção financeira
        NumberField costField = new NumberField("Custo");
        costField.setPrefixComponent(new Span("R$"));  // CORREÇÃO: usar Span, não Text
        costField.setWidthFull();

        // Preencher valores se for edição
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

        Button saveButton = new Button(order == null ? "Cadastrar" : "Atualizar", e -> {
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
                    Notification.show("Pedido atualizado com sucesso!");
                } else {
                    orderService.createOrder(dto);
                    Notification.show("Pedido cadastrado com sucesso!");
                }

                updateGrid();
                dialog.close();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        Button toggleStatusButton = new Button(order != null && order.isActive() ? "Desativar" : "Ativar", e -> {
            String action = order.isActive() ? "desativar" : "ativar";
            ConfirmDialog confirm = new ConfirmDialog(
                    "Confirmar " + (order.isActive() ? "Desativação" : "Ativação"),
                    "Tem certeza que deseja " + action + " este pedido?",
                    "Confirmar",
                    confirmEvent -> {
                        try {
                            orderService.updateOrder(order.getId(),
                                    CreateShippingOrderDTO.builder()
                                            .shippingProviderId(order.getShippingProvider().getId())
                                            .destinationState(order.getDestinationState())
                                            .destinationCity(order.getDestinationCity())
                                            .weight(order.getWeight())
                                            .status(order.getStatus())
                                            .estimatedDeliveryDays(order.getEstimatedDeliveryDays())
                                            .shipmentDate(order.getShipmentDate())
                                            .deliveryDate(order.getDeliveryDate())
                                            .shippingCost(order.getShippingCost())
                                            .build(),
                                    !order.isActive());

                            Notification.show("Pedido " + (!order.isActive() ? "ativado" : "desativado") + " com sucesso!");
                            updateGrid();
                            dialog.close();
                        } catch (Exception ex) {
                            showError(ex);
                        }
                    },
                    "Cancelar",
                    cancelEvent -> {}
            );
            confirm.open();
        });
        toggleStatusButton.setVisible(order != null);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());

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
        if (provider == null) throw new IllegalArgumentException("Transportadora é obrigatória");
        if (state == null || state.isBlank()) throw new IllegalArgumentException("Estado é obrigatório");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("Cidade é obrigatória");
        if (weight == null || weight <= 0) throw new IllegalArgumentException("Peso deve ser positivo");
        if (status == null) throw new IllegalArgumentException("Status é obrigatório");
        if (estimatedDays == null || estimatedDays <= 0) throw new IllegalArgumentException("Dias estimados deve ser positivo");
        if (cost == null || cost < 0) throw new IllegalArgumentException("Custo não pode ser negativo");

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
        Notification.show("Erro: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        e.printStackTrace();
    }

    private void updateGrid() {
        grid.setItems(orderService.listAllShippingOrders());
    }
}