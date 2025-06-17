package com.codefathers.view;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import com.codefathers.model.dto.AtualizarStatusShippingProviderDTO;
import com.codefathers.model.dto.CreateShippingProviderDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.service.ShippingProviderService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Shipping Providers")
@Route("shipping-providers")
public class ShippingProviderView extends VerticalLayout {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ShippingProviderService service;
    private final Grid<ShippingProvider> grid = new Grid<>(ShippingProvider.class, false);
    private String currentSearchTerm = "";
    private Checkbox showInactiveCheckBox = new Checkbox("Show inactive shipping providers");
    private boolean currentShowInactive = false;
    private Grid.Column<ShippingProvider> statusColumn;
    private TextField searchField = new TextField("");
    private GridLazyDataView<ShippingProvider> dataView;

    public ShippingProviderView() {
        this.service = new ShippingProviderService(new ShippingProviderRepositoryImpl());
        setupSearchField();

        Button newButton = new Button("Create Shipping Provider", new Icon(VaadinIcon.PLUS), e -> openFormDialog(null));
        HorizontalLayout leftLayout = new HorizontalLayout(newButton, searchField);
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
        setupLazyDataProvider();

        add(headerLayout, grid);

        showInactiveCheckBox.addValueChangeListener(e -> {
            currentShowInactive = e.getValue();
            statusColumn.setVisible(e.getValue());
            refreshGrid();
        });

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        refreshGrid();
    }

    private void refreshGrid() {
        if (dataView != null) {
            dataView.refreshAll();
        }
        Notification.show("Grid updated", 2000, Notification.Position.BOTTOM_END);
    }

    private void setupGrid() {
        grid.removeAllColumns();

        grid.addColumn(ShippingProvider::getId).setHeader("Id").setAutoWidth(true).setSortable(true);
        grid.addColumn(ShippingProvider::getName).setHeader("Name").setAutoWidth(true).setSortable(true);
        grid.addColumn(sp -> formatCNPJ(sp.getCnpj())).setHeader("CNPJ").setAutoWidth(true).setSortable(true);
        grid.addColumn(sp -> String.format("R$ %.2f", sp.getBasePrice())).setHeader("Base Price").setAutoWidth(true)
                .setSortable(true);
        grid.addColumn(sp -> sp.getDailyCapacity().toString()).setHeader("Daily Capacity").setAutoWidth(true)
                .setSortable(true);
        grid.addColumn(sp -> sp.getCreatedAt().format(DATE_FORMAT)).setHeader("Created At").setSortable(true);

        statusColumn = grid.addColumn(sp -> sp.isActive() ? "Active" : "Inactive")
                .setHeader("Status")
                .setAutoWidth(true);
        statusColumn.setVisible(false);

        grid.setId("custom-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setWidthFull();
        grid.getStyle().set("margin-top", "10px");
        grid.setHeightFull();

        grid.addItemDoubleClickListener(event -> {
            try {
                ShippingProvider item = event.getItem();
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
        searchField.setPlaceholder("Search by ID, Shipping name...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setClearButtonVisible(true);

        searchField.addValueChangeListener(e -> {
            currentSearchTerm = e.getValue().trim();
            refreshGrid();
        });
    }

    private void setupLazyDataProvider() {
        CallbackDataProvider<ShippingProvider, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();
                    List<ShippingProvider> allProviders = service.listAllShippingProviders();
                    Stream<ShippingProvider> filteredStream = allProviders.stream()
                            .filter(this::matchesCurrentFilters);

                    return filteredStream
                            .skip(offset)
                            .limit(limit);
                },
                query -> {
                    List<ShippingProvider> allProviders = service.listAllShippingProviders();
                    return (int) allProviders.stream()
                            .filter(this::matchesCurrentFilters)
                            .count();
                });
        dataView = grid.setItems(dataProvider);
    }

    private boolean matchesCurrentFilters(ShippingProvider provider) {
        if (!showInactiveCheckBox.getValue() && !provider.isActive()) {
            return false;
        }

        if (currentSearchTerm.isEmpty()) {
            return true;
        }

        String searchTermLower = currentSearchTerm.toLowerCase();
        return (provider.getId() != null && provider.getId().toString().toLowerCase().contains(searchTermLower)) ||
                (provider.getName() != null && provider.getName().toLowerCase().contains(searchTermLower)) ||
                (provider.getCnpj() != null && provider.getCnpj().toLowerCase().contains(searchTermLower));
    }

    private String formatCNPJ(String cnpj) {
        if (cnpj == null || cnpj.length() != 14)
            return cnpj;
        return String.format("%s.%s.%s/%s-%s", cnpj.substring(0, 2), cnpj.substring(2, 5), cnpj.substring(5, 8),
                cnpj.substring(8, 12), cnpj.substring(12));
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
        basePriceField.setPlaceholder("Ex: 199.90");
        basePriceField.setPrefixComponent(new Span("R$"));

        IntegerField dailyCapacityField = new IntegerField("Daily Capacity");
        dailyCapacityField.setPlaceholder("Ex: 50");
        dailyCapacityField.setWidthFull();

        if (provider != null) {
            nameField.setValue(provider.getName() != null ? provider.getName() : "");
            cnpjField.setValue(provider.getCnpj() != null ? provider.getCnpj() : "");
            basePriceField.setValue(provider.getBasePrice() != null ? provider.getBasePrice().toString() : "");
            dailyCapacityField
                    .setValue(provider.getDailyCapacity() != null ? provider.getDailyCapacity().intValue() : 0);

            StringBuilder sb = new StringBuilder();
            if (provider.getShippingAreas() != null) {
                for (ShippingArea area : provider.getShippingAreas()) {
                    sb.append(area.getDescription()).append(": ").append(String.join(", ", area.getStates()))
                            .append("; ");
                }
            }
        }

        Button saveButton = new Button(provider == null ? "Register" : "Update", e -> {
            try {
                CreateShippingProviderDTO dto = buildDTO(
                        nameField.getValue(),
                        cnpjField.getValue(),
                        basePriceField.getValue(),
                        dailyCapacityField.getValue());

                if (provider != null) {
                    dto.setId(provider.getId());
                    service.updateShippingProvider(dto);
                    Notification.show("Updated successfully!");
                } else {
                    service.registerShippingProvider(dto);
                    Notification.show("Registered successfully!");
                }

                refreshGrid();
                dialog.close();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        Button toggleStatusButton = new Button(provider != null && provider.isActive() ? "Deactivate" : "Activate",
                e -> {
                    ConfirmDialog confirmDialog = new ConfirmDialog();
                    confirmDialog.setHeader(
                            provider.isActive() ? "Deactivate Shipping Provider" : "Activate Shipping Provider");
                    confirmDialog.setText("Are you sure you want to "
                            + (provider.isActive() ? "deactivate" : "activate") + " this shipping provider?");
                    confirmDialog.setCancelButton("Cancel", event -> confirmDialog.close());
                    confirmDialog.setConfirmButton(provider.isActive() ? "Deactivate" : "Activate", event -> {
                        try {
                            AtualizarStatusShippingProviderDTO dto = AtualizarStatusShippingProviderDTO.builder()
                                    .id(provider.getId())
                                    .active(!provider.isActive())
                                    .build();

                            service.atualizarStatusTransportadora(dto);

                            Notification.show(
                                    "Shipping provider " + (dto.isActive() ? "activated" : "deactivated")
                                            + " successfully!");
                            refreshGrid();
                            dialog.close();
                        } catch (Exception ex) {
                            showError(ex);
                        } finally {
                            confirmDialog.close();
                        }
                    });
                    confirmDialog.open();
                });

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        VerticalLayout formLayout = new VerticalLayout(
                nameField,
                cnpjField,
                basePriceField,
                dailyCapacityField,
                new HorizontalLayout(saveButton, toggleStatusButton, cancelButton));
        formLayout.setPadding(false);
        formLayout.setSpacing(true);
        formLayout.setWidthFull();

        dialog.add(formLayout);
        dialog.open();
    }

    private CreateShippingProviderDTO buildDTO(String name, String cnpj, String priceStr, Integer capacity) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name is mandatory.");
        if (cnpj == null || cnpj.isBlank())
            throw new IllegalArgumentException("CNPJ is mandatory.");
        if (cnpj.length() != 14 || !cnpj.matches("\\d+"))
            throw new IllegalArgumentException("CNPJ must contain 14 numeric digits.");

        BigDecimal basePrice;
        try {
            basePrice = new BigDecimal(priceStr);
            if (basePrice.compareTo(BigDecimal.ZERO) < 0)
                throw new IllegalArgumentException("Base Price must be non-negative.");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Base Price.");
        }

        BigDecimal dailyCapacity;
        try {
            dailyCapacity = capacity != null ? new BigDecimal(capacity) : BigDecimal.ZERO;
            if (dailyCapacity.compareTo(BigDecimal.ZERO) < 0)
                throw new IllegalArgumentException("Daily Capacity must be positive.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Daily Capacity.");
        }

        return CreateShippingProviderDTO.builder()
                .name(name.trim())
                .cnpj(cnpj.trim())
                .basePrice(basePrice)
                .dailyCapacity(dailyCapacity)
                .build();
    }

    private void showError(Exception e) {
        Notification.show("Error: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        e.printStackTrace();
    }
}
