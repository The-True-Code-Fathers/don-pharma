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

    private final ComboBox<ShippingProvider> providerComboBox = new ComboBox<>("Provedor");
    private final TextField destinationState = new TextField("Estado de Destino");
    private final TextField destinationCity = new TextField("Cidade de Destino");
    private final BigDecimalField weight = new BigDecimalField("Peso (kg)");
    private final ComboBox<ShippingServiceStatus> status = new ComboBox<>("Status");
    private final TextField estimatedDays = new TextField("Dias Estimados");
    private final DatePicker shipmentDate = new DatePicker("Data de Envio");
    private final DatePicker deliveryDate = new DatePicker("Data Prevista");
    private final BigDecimalField shippingCost = new BigDecimalField("Custo do Frete");

    private final Button saveButton = new Button("Salvar");
    private final Button clearButton = new Button("Limpar");

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

        status.setItems(ShippingServiceStatus.values());

        loadProviders();
        setupGrid();
        setupForm();

        add(createFormLayout(), grid);
        updateGrid();
    }

    private void loadProviders() {
        providerComboBox.setItems(shippingProviderService.listAllShippingProviders());
        providerComboBox.setItemLabelGenerator(ShippingProvider::getName);
    }

    private void setupGrid() {
        grid.addColumn(order -> order.getId()).setHeader("ID");
        grid.addColumn(order -> order.getShippingProvider().getName()).setHeader("Provedor");
        grid.addColumn(ShippingOrder::getDestinationCity).setHeader("Cidade");
        grid.addColumn(ShippingOrder::getDestinationState).setHeader("Estado");
        grid.addColumn(ShippingOrder::getWeight).setHeader("Peso");
        grid.addColumn(ShippingOrder::getStatus).setHeader("Status");
        grid.setHeight("300px");
    }

    private void setupForm() {
        saveButton.addClickListener(e -> saveShippingOrder());
        clearButton.addClickListener(e -> clearForm());
    }

    private HorizontalLayout createFormLayout() {
        HorizontalLayout form = new HorizontalLayout(
                providerComboBox, destinationState, destinationCity, weight,
                status, estimatedDays, shipmentDate, deliveryDate, shippingCost,
                saveButton, clearButton
        );
        form.setWrap(true);
        return form;
    }

    private void saveShippingOrder() {
        try {
            var provId = providerComboBox.getValue();
            Integer days = estimatedDays.isEmpty() ? null : Integer.parseInt(estimatedDays.getValue());

            CreateShippingOrderDTO dto = CreateShippingOrderDTO.builder()
                    .shippingProvider(provId)
                    .destinationState(destinationState.getValue())
                    .destinationCity(destinationCity.getValue())
                    .weight(weight.getValue())
                    .status(status.getValue())
                    .estimatedDeliveryDays(days)
                    .shipmentDate(shipmentDate.getValue())
                    .deliveryDate(deliveryDate.getValue())
                    .shippingCost(shippingCost.getValue())
                    .build();

            shippingOrderService.createOrder(dto);
            Notification.show("Pedido criado com sucesso!");
            updateGrid();
            clearForm();

        } catch (Exception e) {
            Notification.show("Erro ao salvar: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
            e.printStackTrace();
        }
    }

    private void clearForm() {
        providerComboBox.clear();
        destinationState.clear();
        destinationCity.clear();
        weight.clear();
        status.clear();
        estimatedDays.clear();
        shipmentDate.clear();
        deliveryDate.clear();
        shippingCost.clear();
    }

    private void updateGrid() {
        grid.setItems(shippingOrderService.listAllShippingOrders());
    }
}
