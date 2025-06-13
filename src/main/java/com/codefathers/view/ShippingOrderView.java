package com.codefathers.view;

import com.codefathers.model.dto.CreateShippingOrderDTO;
import com.codefathers.model.entity.ShippingOrder;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.model.enums.ShippingServiceStatus;
import com.codefathers.repository.implementations.ShippingOrderRepositoryImpl;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.service.ShippingOrderService;
import com.codefathers.service.ShippingProviderService;
import com.codefathers.util.ValidatorUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.UUID;

@Route("shipping-orders")
public class ShippingOrderView extends VerticalLayout {

    private final ShippingOrderService shippingOrderService;
    private final ShippingProviderService shippingProviderService;
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

        // Configuração inicial
        configureGrid();
        Button newOrderButton = new Button("Novo Pedido", e -> openEditDialog(null));
        add(newOrderButton, grid);
        updateGrid();
    }

    private void configureGrid() {
        grid.addColumn(ShippingOrder::getId).setHeader("ID");
        grid.addColumn(order -> order.getShippingProvider().getName()).setHeader("Provedor");
        grid.addColumn(ShippingOrder::getDestinationCity).setHeader("Cidade");
        grid.addColumn(ShippingOrder::getDestinationState).setHeader("Estado");
        grid.addColumn(ShippingOrder::getWeight).setHeader("Peso (kg)");
        grid.addColumn(ShippingOrder::getStatus).setHeader("Status");
        grid.addColumn(ShippingOrder::getShippingCost).setHeader("Custo Frete");
        grid.setHeight("400px");
        grid.setId("custom-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);


        grid.addItemDoubleClickListener(event -> {
            if (event.getItem() != null) {
                openEditDialog(event.getItem());
                grid.asSingleSelect().clear();
            }
        });
    }

    private void openEditDialog(ShippingOrder order) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");

        // Componentes do formulário
        ComboBox<ShippingProvider> providerComboBox = new ComboBox<>("Provedor");
        providerComboBox.setItems(shippingProviderService.listAllShippingProviders());
        providerComboBox.setItemLabelGenerator(ShippingProvider::getName);
        providerComboBox.setRequired(true);

        TextField destinationState = new TextField("Estado de Destino");
        destinationState.setRequired(true);
        destinationState.setPattern("[A-Za-z]{2}");
        destinationState.setErrorMessage("Digite a sigla do estado (2 letras)");

        TextField destinationCity = new TextField("Cidade de Destino");
        destinationCity.setRequired(true);

        BigDecimalField weight = new BigDecimalField("Peso (kg)");
        weight.setRequiredIndicatorVisible(true);

        ComboBox<ShippingServiceStatus> status = new ComboBox<>("Status");
        status.setItems(ShippingServiceStatus.values());
        status.setRequired(true);

        TextField estimatedDays = new TextField("Dias Estimados");
        estimatedDays.setPattern("\\d*");
        estimatedDays.setErrorMessage("Apenas números são permitidos");

        DatePicker shipmentDate = new DatePicker("Data de Envio");
        DatePicker deliveryDate = new DatePicker("Data Prevista");

        BigDecimalField shippingCost = new BigDecimalField("Custo do Frete");
        shippingCost.setRequiredIndicatorVisible(true);

        // Preenche os campos se estiver editando
        if (order != null) {
            providerComboBox.setValue(order.getShippingProvider());
            destinationState.setValue(order.getDestinationState());
            destinationCity.setValue(order.getDestinationCity());
            weight.setValue(order.getWeight());
            status.setValue(order.getStatus());
            estimatedDays.setValue(order.getEstimatedDeliveryDays() != null ?
                    order.getEstimatedDeliveryDays().toString() : "");
            shipmentDate.setValue(order.getShipmentDate());
            deliveryDate.setValue(order.getDeliveryDate());
            shippingCost.setValue(order.getShippingCost());
        }

        // Botões de ação
        Button saveButton = new Button(order == null ? "Cadastrar" : "Atualizar", e -> saveOrder(order, dialog,
                providerComboBox, destinationState, destinationCity, weight, status,
                estimatedDays, shipmentDate, deliveryDate, shippingCost));

        Button deleteButton = new Button("Deletar", e -> deleteOrder(order, dialog));
        deleteButton.setVisible(order != null);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        // Layout
        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, deleteButton, cancelButton);
        VerticalLayout formLayout = new VerticalLayout(
                providerComboBox, destinationState, destinationCity, weight,
                status, estimatedDays, shipmentDate, deliveryDate, shippingCost,
                buttonsLayout
        );

        dialog.add(formLayout);
        dialog.open();
    }

    private void saveOrder(ShippingOrder existingOrder, Dialog dialog,
                           ComboBox<ShippingProvider> providerComboBox,
                           TextField destinationState, TextField destinationCity,
                           BigDecimalField weight, ComboBox<ShippingServiceStatus> status,
                           TextField estimatedDays, DatePicker shipmentDate,
                           DatePicker deliveryDate, BigDecimalField shippingCost) {
        try {
            // Validação básica dos campos obrigatórios
            if (providerComboBox.isEmpty() || destinationState.isEmpty() ||
                    destinationCity.isEmpty() || weight.isEmpty() || status.isEmpty()) {
                Notification.show("Preencha todos os campos obrigatórios", 3000, Notification.Position.MIDDLE);
                return;
            }

            // Cria o DTO
            CreateShippingOrderDTO dto = CreateShippingOrderDTO.builder()
                    .shippingProviderId(providerComboBox.getValue().getId())
                    .destinationState(destinationState.getValue())
                    .destinationCity(destinationCity.getValue())
                    .weight(weight.getValue())
                    .status(status.getValue())
                    .estimatedDeliveryDays(estimatedDays.getValue().isEmpty() ?
                            null : Integer.parseInt(estimatedDays.getValue()))
                    .shipmentDate(shipmentDate.getValue())
                    .deliveryDate(deliveryDate.getValue())
                    .shippingCost(shippingCost.getValue())
                    .build();

            // Salva ou atualiza
            if (existingOrder == null) {
                shippingOrderService.createOrder(dto);
                Notification.show("Pedido criado com sucesso!");
            } else {
                shippingOrderService.updateOrder(existingOrder.getId(), dto);
                Notification.show("Pedido atualizado com sucesso!");
            }

            updateGrid();
            dialog.close();
        } catch (Exception ex) {
            Notification.show("Erro: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            ex.printStackTrace();
        }
    }

    private void deleteOrder(ShippingOrder order, Dialog dialog) {
        try {
            shippingOrderService.removeShippingOrder(order.getId());
            Notification.show("Pedido deletado com sucesso!");
            updateGrid();
            dialog.close();
        } catch (Exception ex) {
            Notification.show("Erro ao deletar: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
            ex.printStackTrace();
        }
    }

    private void updateGrid() {
        grid.setItems(shippingOrderService.listAllShippingOrders());
    }
}