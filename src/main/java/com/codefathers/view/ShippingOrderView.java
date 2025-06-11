package com.codefathers.view;

import com.codefathers.model.dto.CreateShippingOrderDTO;
import com.codefathers.model.entity.ShippingOrder;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.model.enums.ShippingServiceStatus;
import com.codefathers.repository.ShippingOrderRepositoryImpl;
import com.codefathers.repository.ShippingProviderRepositoryImpl;
import com.codefathers.service.ShippingOrderService;
import com.codefathers.service.ShippingProviderService;
import com.codefathers.util.ValidatorUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.UUID;

@Route("shipping-orders")
public class ShippingOrderView extends VerticalLayout {

    private ShippingOrderService shippingOrderService;
    private ShippingProviderService shippingProviderService;

    private final Grid<ShippingOrder> grid = new Grid<>(ShippingOrder.class, false);

    public ShippingOrderView() {
        ShippingProviderRepositoryImpl providerRepository = new ShippingProviderRepositoryImpl();
        ShippingOrderRepositoryImpl orderRepository = new ShippingOrderRepositoryImpl();
        this.shippingProviderService = new ShippingProviderService(providerRepository);

        this.shippingOrderService = new ShippingOrderService(
                orderRepository,
                providerRepository,
                ValidatorUtil.getValidator()
        );

        // Botão de cadastro
        Button newOrderButton = new Button("Novo Pedido", e -> openEditDialog(null));

        setupGrid();

        // Adiciona o botão e a grade na tela
        add(newOrderButton, grid);
        updateGrid();
    }


    private void setupGrid() {
        grid.addColumn(order -> order.getId()).setHeader("ID");
        grid.addColumn(order -> order.getShippingProvider().getName()).setHeader("Provedor");
        grid.addColumn(ShippingOrder::getDestinationCity).setHeader("Cidade");
        grid.addColumn(ShippingOrder::getDestinationState).setHeader("Estado");
        grid.addColumn(ShippingOrder::getWeight).setHeader("Peso");
        grid.addColumn(ShippingOrder::getStatus).setHeader("Status");
        grid.setHeight("300px");

        grid.addItemDoubleClickListener(event -> {
            ShippingOrder selectedOrder = event.getItem();
            if (selectedOrder != null) {
                openEditDialog(selectedOrder);
                grid.asSingleSelect().clear();
            }
        });
    }

    private void openEditDialog(ShippingOrder order) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");

        // Campos do formulário
        ComboBox<ShippingProvider> providerComboBox = new ComboBox<>("Provedor");
        providerComboBox.setItems(shippingProviderService.listAllShippingProviders());
        providerComboBox.setItemLabelGenerator(ShippingProvider::getName);
        if (order != null) providerComboBox.setValue(order.getShippingProvider());

        TextField destinationState = new TextField("Estado de Destino");
        destinationState.setValue(order != null && order.getDestinationState() != null ? order.getDestinationState() : "");

        TextField destinationCity = new TextField("Cidade de Destino");
        destinationCity.setValue(order != null && order.getDestinationCity() != null ? order.getDestinationCity() : "");

        BigDecimalField weight = new BigDecimalField("Peso (kg)");
        weight.setValue(order != null ? order.getWeight() : null);

        ComboBox<ShippingServiceStatus> status = new ComboBox<>("Status");
        status.setItems(ShippingServiceStatus.values());
        if (order != null) status.setValue(order.getStatus());

        TextField estimatedDays = new TextField("Dias Estimados");
        estimatedDays.setValue(order != null && order.getEstimatedDeliveryDays() != null ? order.getEstimatedDeliveryDays().toString() : "");

        DatePicker shipmentDate = new DatePicker("Data de Envio");
        shipmentDate.setValue(order != null ? order.getShipmentDate() : null);

        DatePicker deliveryDate = new DatePicker("Data Prevista");
        deliveryDate.setValue(order != null ? order.getDeliveryDate() : null);

        BigDecimalField shippingCost = new BigDecimalField("Custo do Frete");
        shippingCost.setValue(order != null ? order.getShippingCost() : null);

        Button saveOrUpdateButton = new Button(order == null ? "Cadastrar" : "Atualizar", e -> {
            try {
                UUID provId = providerComboBox.getValue().getId();
                Integer days = estimatedDays.isEmpty() ? null : Integer.parseInt(estimatedDays.getValue());

                CreateShippingOrderDTO dto = CreateShippingOrderDTO.builder()
                        .shippingProviderId(provId)
                        .destinationState(destinationState.getValue())
                        .destinationCity(destinationCity.getValue())
                        .weight(weight.getValue())
                        .status(status.getValue())
                        .estimatedDeliveryDays(days)
                        .shipmentDate(shipmentDate.getValue())
                        .deliveryDate(deliveryDate.getValue())
                        .shippingCost(shippingCost.getValue())
                        .build();

                if (order == null) {
                    shippingOrderService.createOrder(dto);
                    Notification.show("Pedido criado com sucesso!");
                } else {
                    shippingOrderService.updateOrder(order.getId(), dto);
                    Notification.show("Pedido atualizado com sucesso!");
                }

                updateGrid();
                dialog.close();

            } catch (Exception ex) {
                Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                ex.printStackTrace();
            }
        });

        Button deleteButton = new Button("Deletar", e -> {
            if (order == null) return; // Evita deletar quando for novo

            try {
                shippingOrderService.removeShippingOrder(order.getId());
                Notification.show("Pedido deletado com sucesso!");
                updateGrid();
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Erro ao deletar: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                ex.printStackTrace();
            }
        });
        deleteButton.setVisible(order != null); // só mostra ao editar

        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveOrUpdateButton, deleteButton, cancelButton);

        VerticalLayout dialogLayout = new VerticalLayout(
                providerComboBox, destinationState, destinationCity, weight,
                status, estimatedDays, shipmentDate, deliveryDate, shippingCost,
                buttons
        );

        dialog.add(dialogLayout);
        dialog.open();
    }


    private void updateGrid() {
        grid.setItems(shippingOrderService.listAllShippingOrders());
    }
}
