package com.codefathers.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList; // Adicionado
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codefathers.model.dto.CreateShippingOrderDTO;
import com.codefathers.model.entity.Order;
import com.codefathers.model.entity.PurchaseOrder;
import com.codefathers.model.entity.ShippingOrder;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.model.enums.ShippingServiceStatus;
import com.codefathers.repository.implementations.EmployeeRepositoryImpl;
import com.codefathers.repository.implementations.OrderRepositoryImpl;
import com.codefathers.repository.implementations.PurchaseOrderRepositoryImpl;
import com.codefathers.repository.implementations.ShippingOrderRepositoryImpl;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.repository.implementations.StorageRepositoryImpl;
import com.codefathers.service.OrderService;
import com.codefathers.service.PurchaseOrderService;
import com.codefathers.service.ShippingOrderService;
import com.codefathers.service.ShippingProviderService;
import com.codefathers.util.ValidatorUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView; // Usar GridListDataView para filtragem em memória
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Shipping Orders")
@Route("shipping-orders")
public class ShippingOrderView extends VerticalLayout {

    private final ShippingOrderService orderService;
    private final ShippingProviderService providerService;
    private final OrderService sellOrderService;
    private final PurchaseOrderService purchaseOrderService;
    private final Grid<ShippingOrder> grid = new Grid<>(ShippingOrder.class, false);
    private ComboBox<String> statusFilter;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private TextField searchField = new TextField();
    private Checkbox showInactiveCheckBox = new Checkbox("Show inactive");

    private GridListDataView<ShippingOrder> dataView;
    private List<ShippingOrder> allOrdersCache = new ArrayList<>();
    private Grid.Column<ShippingOrder> statusColumn;

    public ShippingOrderView() {
        this.providerService = new ShippingProviderService(new ShippingProviderRepositoryImpl());
        this.sellOrderService = new OrderService(new OrderRepositoryImpl(), new EmployeeRepositoryImpl(),
                new StorageRepositoryImpl(), ValidatorUtil.getValidator());
        this.purchaseOrderService = new PurchaseOrderService(new PurchaseOrderRepositoryImpl(),
                new EmployeeRepositoryImpl(), new StorageRepositoryImpl(), ValidatorUtil.getValidator());
        this.orderService = new ShippingOrderService(new ShippingOrderRepositoryImpl(),
                new ShippingProviderRepositoryImpl(), new OrderRepositoryImpl(), new PurchaseOrderRepositoryImpl(),
                ValidatorUtil.getValidator());

        setupSearchField();
        setupStatusFilter();

        Button newButton = new Button("Create Order", new Icon(VaadinIcon.PLUS), e -> openFormDialog(null));
        HorizontalLayout leftLayout = new HorizontalLayout(newButton, searchField, statusFilter);
        leftLayout.setAlignItems(Alignment.CENTER);
        leftLayout.setSpacing(true);

        HorizontalLayout rightLayout = new HorizontalLayout(showInactiveCheckBox);
        rightLayout.setAlignItems(Alignment.CENTER);
        rightLayout.setSpacing(true);

        HorizontalLayout headerLayout = new HorizontalLayout(leftLayout, rightLayout);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setWidthFull();

        setupGrid();
        loadAllOrdersAndSetupDataProvider();

        add(headerLayout, grid);

        showInactiveCheckBox.addValueChangeListener(e -> {
            statusColumn.setVisible(e.getValue());
            refreshGrid();
        });

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    private void setupGrid() {
        grid.removeAllColumns();
        grid.addColumn(order -> order.getShippingProvider().getName()).setHeader("Shipping Provider").setAutoWidth(true)
                .setSortable(true);
        grid.addColumn(ShippingOrder::getDestinationState).setHeader("Destination State").setAutoWidth(true)
                .setSortable(true);
        grid.addColumn(ShippingOrder::getDestinationCity).setHeader("Destination City").setAutoWidth(true)
                .setSortable(true);
        grid.addColumn(order -> String.format("%.2f kg", order.getWeight().doubleValue())).setHeader("Weight")
                .setAutoWidth(true).setSortable(true);
        grid.addColumn(ShippingOrder::getStatus).setHeader("Status").setAutoWidth(true).setSortable(true);
        grid.addColumn(ShippingOrder::getEstimatedDeliveryDays).setHeader("Estimated Days").setAutoWidth(true)
                .setSortable(true);
        grid.addColumn(order -> order.getShipmentDate() != null ? order.getShipmentDate().format(DATE_FORMATTER) : "-")
                .setHeader("Shipment Date").setAutoWidth(true).setSortable(true);
        grid.addColumn(order -> order.getDeliveryDate() != null ? order.getDeliveryDate().format(DATE_FORMATTER) : "-")
                .setHeader("Delivery Date").setAutoWidth(true).setSortable(true);
        grid.addColumn(order -> String.format("R$ %.2f", order.getShippingCost().doubleValue())).setHeader("Cost")
                .setAutoWidth(true).setSortable(true);
        grid.addColumn(order -> order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_FORMATTER) : "-")
                .setHeader("Created At").setAutoWidth(true).setSortable(true);

        statusColumn = grid.addColumn(sp -> sp.isActive() ? "Active" : "Inactive")
                .setHeader("Active Status")
                .setAutoWidth(true);
        statusColumn.setVisible(false);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeightFull();
        grid.setWidthFull();
        grid.getStyle().set("margin-top", "10px");

        grid.addItemDoubleClickListener(event -> {
            try {
                ShippingOrder item = event.getItem();
                if (item != null) {
                    openFormDialog(item);
                } else {
                    Notification.show("Select a valid item", 3000, Notification.Position.MIDDLE);
                }
            } catch (Exception e) {
                Notification.show("Error opening editor: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                e.printStackTrace();
            }
        });
    }

    private void setupSearchField() {
        searchField.setWidth("400px");
        searchField.setPlaceholder("Search by Shipping Provider, City, Status...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setClearButtonVisible(true);

        searchField.addValueChangeListener(e -> {
            refreshGrid();
        });
    }

    private void setupStatusFilter() {
        statusFilter = new ComboBox<>("");
        statusFilter.setPlaceholder("Filter by Status");
        statusFilter.setItems(Stream.concat(
                Stream.of("ALL"),
                Arrays.stream(ShippingServiceStatus.values()).map(Enum::name)).toList());
        statusFilter.setValue("ALL");
        statusFilter.setAllowCustomValue(false);
        statusFilter.addValueChangeListener(e -> refreshGrid()); // Re-aplica filtros
        statusFilter.setWidth("200px");
    }

    private void loadAllOrdersAndSetupDataProvider() {
        try {
            allOrdersCache = orderService.listAll();
            dataView = grid.setItems(allOrdersCache);
            dataView.setFilter(this::matchesCurrentFilters);
        } catch (Exception e) {
            Notification.show("Error loading shipping orders: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            e.printStackTrace();
        }
    }

    private boolean matchesCurrentFilters(ShippingOrder order) {
        if (!showInactiveCheckBox.getValue() && !order.isActive()) {
            return false;
        }

        String currentSearchTerm = searchField.getValue().trim();
        if (!currentSearchTerm.isEmpty()) {
            String searchTermLower = currentSearchTerm.toLowerCase();
            boolean matchesSearch = (order.getShippingProvider() != null
                    && order.getShippingProvider().getName().toLowerCase().contains(searchTermLower)) ||
                    (order.getDestinationCity() != null
                            && order.getDestinationCity().toLowerCase().contains(searchTermLower))
                    ||
                    (order.getStatus() != null && order.getStatus().name().toLowerCase().contains(searchTermLower));
            if (!matchesSearch) {
                return false;
            }
        }

        String selectedStatus = statusFilter.getValue();
        if (selectedStatus != null && !selectedStatus.equals("ALL")) {
            return order.getStatus() != null && order.getStatus().name().equals(selectedStatus);
        }

        return true;
    }

    private void refreshGrid() {
        if (dataView != null) {
            loadAllOrdersAndSetupDataProvider();
        }
    }

    private void openFormDialog(ShippingOrder order) {
        Dialog dialog = new Dialog();
        dialog.setWidth("700px");
        dialog.setHeight("auto");
        dialog.setResizable(false);

        H3 title = new H3(order == null ? "New Shipping Order" : "Edit Shipping Order");
        title.getStyle().set("margin", "0 0 20px 0");

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setSpacing(true);
        mainLayout.getStyle().set("gap", "30px");

        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setPadding(false);
        leftColumn.setSpacing(true);
        leftColumn.setWidth("50%");
        leftColumn.getStyle().set("gap", "20px");

        ComboBox<ShippingProvider> orderCombo = new ComboBox<>("Shipping Provider");
        try {
            List<ShippingProvider> providers = providerService.listAllShippingProviders();
            orderCombo.setItems(providers != null ? providers : List.of());
        } catch (Exception ex) {
            Notification.show("Error loading shipping providers: " + ex.getMessage(), 5000,
                    Notification.Position.MIDDLE);
            orderCombo.setItems(List.of());
        }
        orderCombo.setItemLabelGenerator(ShippingProvider::getName);
        orderCombo.setWidthFull();

        HorizontalLayout destinationLayout = new HorizontalLayout();
        destinationLayout.setWidthFull();
        destinationLayout.setSpacing(true);
        destinationLayout.getStyle().set("gap", "15px");

        TextField stateField = new TextField("State");
        stateField.setPlaceholder("Ex: SP");
        stateField.setWidth("40%");

        TextField cityField = new TextField("City");
        cityField.setPlaceholder("Ex: São Paulo");
        cityField.setWidth("60%");

        destinationLayout.add(stateField, cityField);

        HorizontalLayout weightCostLayout = new HorizontalLayout();
        weightCostLayout.setWidthFull();
        weightCostLayout.setSpacing(true);
        weightCostLayout.getStyle().set("gap", "15px");

        NumberField weightField = new NumberField("Weight (kg)");
        weightField.setPlaceholder("Ex: 2.5");
        weightField.setWidth("50%");

        NumberField costField = new NumberField("Cost");
        costField.setPrefixComponent(new Span("R$"));
        costField.setWidth("50%");

        weightCostLayout.add(weightField, costField);

        HorizontalLayout statusLayout = new HorizontalLayout();
        statusLayout.setWidthFull();
        statusLayout.setSpacing(true);
        statusLayout.getStyle().set("gap", "15px");

        ComboBox<ShippingServiceStatus> statusCombo = new ComboBox<>("Status");
        List<ShippingServiceStatus> allowedStatuses = Arrays.stream(ShippingServiceStatus.values())
                .filter(status -> status != ShippingServiceStatus.ENTREGUE && status != ShippingServiceStatus.ATRASADO)
                .toList();
        statusCombo.setItems(order == null ? allowedStatuses : Arrays.asList(ShippingServiceStatus.values()));
        statusCombo.setWidth("60%");

        IntegerField estimatedDaysField = new IntegerField("Estimated Days");
        estimatedDaysField.setPlaceholder("Ex: 5");
        estimatedDaysField.setWidth("40%");

        statusLayout.add(statusCombo, estimatedDaysField);

        HorizontalLayout datesLayout = new HorizontalLayout();
        datesLayout.setWidthFull();
        datesLayout.setSpacing(true);
        datesLayout.getStyle().set("gap", "15px");

        DatePicker shipmentDatePicker = new DatePicker("Shipment Date");
        shipmentDatePicker.setWidth("50%");

        DatePicker deliveryDatePicker = new DatePicker("Delivery Date");
        deliveryDatePicker.setWidth("50%");

        datesLayout.add(shipmentDatePicker, deliveryDatePicker);

        leftColumn.add(orderCombo, destinationLayout, weightCostLayout, statusLayout, datesLayout);

        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setPadding(false);
        rightColumn.setSpacing(true);
        rightColumn.setWidth("50%");
        rightColumn.getStyle().set("gap", "20px");

        MultiSelectComboBox<Order> sellOrdersCombo = new MultiSelectComboBox<>("Sell Orders");
        try {
            final List<Order> sellOrders = sellOrderService.findAll();
            if (sellOrders != null && !sellOrders.isEmpty()) {
                sellOrdersCombo.setItems(sellOrders);
                
                sellOrdersCombo.setItemLabelGenerator(sellOrder -> {
                    int orderNumber = sellOrders.indexOf(sellOrder) + 1; // +1 para começar em 1
                    String formattedDate = sellOrder.getCreatedAt() != null 
                        ? sellOrder.getCreatedAt().format(DATE_FORMATTER) 
                        : "N/A";
                    String description = sellOrder.getDescription() != null 
                        ? " - " + sellOrder.getDescription() 
                        : "";
                    return "Order #" + orderNumber + description + " (" + formattedDate + ")";
                });
            } else {
                sellOrdersCombo.setItems(List.of());
            }
        } catch (Exception ex) {
            Notification.show("Error loading sell orders: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            sellOrdersCombo.setItems(List.of());
        }
        sellOrdersCombo.setWidthFull();

        MultiSelectComboBox<PurchaseOrder> purchaseOrdersCombo = new MultiSelectComboBox<>("Purchase Orders");
        try {
            final List<PurchaseOrder> purchaseOrders = purchaseOrderService.listAll();
            if (purchaseOrders != null && !purchaseOrders.isEmpty()) {
                purchaseOrdersCombo.setItems(purchaseOrders);
                
                purchaseOrdersCombo.setItemLabelGenerator(purchaseOrder -> {
                    int orderNumber = purchaseOrders.indexOf(purchaseOrder) + 1;
                    String formattedDate = purchaseOrder.getCreatedAt() != null 
                        ? purchaseOrder.getCreatedAt().format(DATE_FORMATTER) 
                        : "N/A";
                    return "Purchase #" + orderNumber + " (" + formattedDate + ")";
                });
            } else {
                purchaseOrdersCombo.setItems(List.of());
            }
        } catch (Exception ex) {
            Notification.show("Error loading purchase orders: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            purchaseOrdersCombo.setItems(List.of());
        }
        purchaseOrdersCombo.setWidthFull();

        rightColumn.add(sellOrdersCombo, purchaseOrdersCombo);

        mainLayout.add(leftColumn, rightColumn);

        if (order != null) {
            orderCombo.setValue(order.getShippingProvider());
            stateField.setValue(order.getDestinationState());
            cityField.setValue(order.getDestinationCity());
            weightField.setValue(order.getWeight() != null ? order.getWeight().doubleValue() : null);
            statusCombo.setValue(order.getStatus());
            estimatedDaysField.setValue(order.getEstimatedDeliveryDays());
            shipmentDatePicker.setValue(order.getShipmentDate());
            deliveryDatePicker.setValue(order.getDeliveryDate());
            costField.setValue(order.getShippingCost() != null ? order.getShippingCost().doubleValue() : null);

            List<Order> currentSellOrders = order.getOrders();
            if (currentSellOrders != null) {
                sellOrdersCombo.setValue(currentSellOrders);
            }
            List<PurchaseOrder> currentPurchaseOrders = order.getPurchaseOrder();
            if (currentPurchaseOrders != null) {
                purchaseOrdersCombo.setValue(currentPurchaseOrders);
            }
        }

        Button saveButton = new Button(order == null ? "Register" : "Update", e -> {
            try {
                CreateShippingOrderDTO dto = buildOrderDTO(orderCombo.getValue(), stateField.getValue(),
                        cityField.getValue(), weightField.getValue(), statusCombo.getValue(),
                        estimatedDaysField.getValue(), shipmentDatePicker.getValue(), deliveryDatePicker.getValue(),
                        costField.getValue(), sellOrdersCombo.getSelectedItems(),
                        purchaseOrdersCombo.getSelectedItems());

                if (order != null) {
                    orderService.updateOrder(order.getId(), dto, order.isActive());
                    Notification.show("Order updated successfully!");
                } else {
                    orderService.createOrder(dto);
                    Notification.show("Order registered successfully!");
                }

                refreshGrid();
                dialog.close();
            } catch (Exception ex) {
                showError(ex);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button toggleStatusButton = new Button(order != null && order.isActive() ? "Deactivate" : "Activate", e -> {
            String action = order.isActive() ? "deactivate" : "activate";
            try {
                CreateShippingOrderDTO dto = CreateShippingOrderDTO.builder()
                        .shippingProviderId(order.getShippingProvider().getId())
                        .destinationState(order.getDestinationState())
                        .destinationCity(order.getDestinationCity())
                        .weight(order.getWeight())
                        .status(order.getStatus())
                        .estimatedDeliveryDays(order.getEstimatedDeliveryDays())
                        .shipmentDate(order.getShipmentDate())
                        .deliveryDate(order.getDeliveryDate())
                        .shippingCost(order.getShippingCost())
                        .order(order.getOrders() != null
                                ? order.getOrders().stream().map(Order::getId).collect(Collectors.toList())
                                : List.of())
                        .purchaseOrder(order.getPurchaseOrder() != null ? order.getPurchaseOrder().stream()
                                .map(PurchaseOrder::getId).collect(Collectors.toList()) : List.of())
                        .build();

                orderService.updateOrder(order.getId(), dto, !order.isActive());

                Notification.show("Order " + (!order.isActive() ? "activated" : "deactivated") + " successfully!");
                refreshGrid();
            } catch (Exception ex) {
                showError(ex);
            }
        });
        toggleStatusButton.setVisible(order != null);
        toggleStatusButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, toggleStatusButton, cancelButton);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonsLayout.setWidthFull();
        buttonsLayout.getStyle().set("margin-top", "25px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setWidthFull();
        dialogLayout.getStyle().set("padding", "25px");

        dialogLayout.add(title, new Hr(), mainLayout, buttonsLayout);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private CreateShippingOrderDTO buildOrderDTO(ShippingProvider provider, String state, String city, Double weight,
            ShippingServiceStatus status, Integer estimatedDays, LocalDate shipmentDate, LocalDate deliveryDate,
            Double cost, Set<Order> sellOrders, Set<PurchaseOrder> purchaseOrders) {

        if (provider == null)
            throw new IllegalArgumentException("Shipping provider is mandatory");
        if (state == null || state.isBlank())
            throw new IllegalArgumentException("State is mandatory");
        if (city == null || city.isBlank())
            throw new IllegalArgumentException("City is mandatory");
        if (weight == null || weight <= 0)
            throw new IllegalArgumentException("Weight must be positive");
        if (status == null)
            throw new IllegalArgumentException("Status is mandatory");
        if (estimatedDays == null || estimatedDays <= 0)
            throw new IllegalArgumentException("Estimated days must be positive");
        if (cost == null || cost < 0)
            throw new IllegalArgumentException("Cost cannot be negative");

        List<UUID> orderIds = sellOrders != null ? sellOrders.stream().map(Order::getId).toList() : List.of();
        List<UUID> purchaseOrderIds = purchaseOrders != null
                ? purchaseOrders.stream().map(PurchaseOrder::getId).toList()
                : List.of();

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
                .order(orderIds)
                .purchaseOrder(purchaseOrderIds)
                .active(true)
                .build();
    }

    private void showError(Exception e) {
        Notification.show("Error: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        e.printStackTrace();
    }
}