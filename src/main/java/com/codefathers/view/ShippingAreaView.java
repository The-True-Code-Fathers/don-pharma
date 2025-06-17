package com.codefathers.view;

import com.codefathers.model.dto.CreateShippingAreaDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.implementations.ShippingAreaRepositoryImpl;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.service.ShippingAreaService;
import com.codefathers.service.ShippingProviderService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.validation.Validation;

import java.util.ArrayList;
import java.util.List;

@Route("shipping-areas")
@PageTitle("Shipping Areas")
public class ShippingAreaView extends VerticalLayout {

    private final ShippingAreaService areaService;
    private final ShippingProviderService providerService;

    private final Grid<ShippingArea> grid = new Grid<>(ShippingArea.class, false);
    private TextField searchField = new TextField();
    private Checkbox showInactiveCheckBox = new Checkbox("Show inactive");
    private Grid.Column<ShippingArea> statusColumn;

    private List<ShippingArea> allAreasCache = new ArrayList<>();
    private GridListDataView<ShippingArea> dataView;

    public ShippingAreaView() {
        this.areaService = new ShippingAreaService(
                new ShippingAreaRepositoryImpl(),
                Validation.buildDefaultValidatorFactory().getValidator());
        this.providerService = new ShippingProviderService(new ShippingProviderRepositoryImpl());

        setupSearchField();

        Button newButton = new Button("Create Shipping Area", new Icon(VaadinIcon.PLUS), e -> openFormDialog(null));

        HorizontalLayout leftGroup = new HorizontalLayout(newButton, searchField);
        leftGroup.setSpacing(true);
        leftGroup.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout topLayout = new HorizontalLayout(leftGroup, showInactiveCheckBox);
        topLayout.setWidthFull();
        topLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        searchField.setMaxWidth("600px");

        setupGrid();
        loadAllAreasAndSetupDataProvider();

        add(topLayout, grid);
        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    private void setupSearchField() {
        searchField.setWidth("400px");
        searchField.setPlaceholder("Search by Description, States, Provider...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> refreshAreasGrid());
    }

    private void setupGrid() {
        grid.removeAllColumns();
        grid.addColumn(ShippingArea::getDescription).setHeader("Description").setAutoWidth(true).setSortable(true);
        grid.addColumn(area -> area.getShippingProvider() != null ? area.getShippingProvider().getName() : "None")
                .setHeader("Provider").setAutoWidth(true).setSortable(true);
        grid.addColumn(area -> area.getStates() != null ? area.getStates() : "")
                .setHeader("States").setAutoWidth(true).setSortable(true);

        statusColumn = grid.addColumn(area -> area.isActive() ? "Active" : "Inactive")
                .setHeader("Status")
                .setAutoWidth(true);
        statusColumn.setVisible(false);

        grid.addItemDoubleClickListener(event -> openFormDialog(event.getItem()));
        grid.setHeightFull();
        grid.setWidthFull();
        grid.getStyle().set("margin-top", "10px");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        showInactiveCheckBox.addValueChangeListener(e -> {
            statusColumn.setVisible(e.getValue());
            refreshAreasGrid();
        });
    }

    private void loadAllAreasAndSetupDataProvider() {
        try {
            allAreasCache = areaService.findAllShippingAreas();
            dataView = grid.setItems(allAreasCache);
            dataView.setFilter(this::matchesCurrentFilters);
        } catch (Exception e) {
            Notification.show("Error loading shipping areas: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
            e.printStackTrace();
        }
    }

    private boolean matchesCurrentFilters(ShippingArea area) {
        if (!showInactiveCheckBox.getValue() && !area.isActive()) {
            return false;
        }

        String searchTerm = searchField.getValue().trim().toLowerCase();
        if (!searchTerm.isEmpty()) {
            boolean matchesSearch = (area.getDescription() != null && area.getDescription().toLowerCase().contains(searchTerm)) ||
                    (area.getStates() != null && area.getStates().toLowerCase().contains(searchTerm)) ||
                    (area.getShippingProvider() != null && area.getShippingProvider().getName().toLowerCase().contains(searchTerm));
            if (!matchesSearch) {
                return false;
            }
        }
        return true;
    }

    private void refreshAreasGrid() {
        if (dataView != null) {
            loadAllAreasAndSetupDataProvider();
        }
    }

    private void openFormDialog(ShippingArea area) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        boolean isNew = area == null;
        H3 title = new H3(isNew ? "New Shipping Area" : "Edit Shipping Area");
        title.getStyle().set("margin", "0 0 20px 0");

        TextField descriptionField = new TextField("Description");
        descriptionField.setWidthFull();

        TextArea statesField = new TextArea("States (comma-separated)");
            statesField.setPlaceholder("Ex: SP, RJ, MG");
        statesField.setWidthFull();

        ComboBox<ShippingProvider> providerComboBox = new ComboBox<>("Shipping Provider");
        try {
            providerComboBox.setItems(providerService.listAllShippingProviders());
        } catch (Exception e) {
            Notification.show("Error loading providers: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            providerComboBox.setItems(List.of());
        }
        providerComboBox.setItemLabelGenerator(ShippingProvider::getName);
        providerComboBox.setWidthFull();

        if (!isNew) {
            descriptionField.setValue(area.getDescription());
            statesField.setValue(area.getStates() != null ? area.getStates() : "");
            providerComboBox.setValue(area.getShippingProvider());
        }

        Button saveButton = new Button(isNew ? "Create" : "Update", e -> {
            try {
                if (descriptionField.isEmpty() || statesField.isEmpty() || providerComboBox.getValue() == null) {
                    Notification.show("Please fill all required fields.", 3000, Notification.Position.MIDDLE);
                    return;
                }

                if (isNew) {
                    CreateShippingAreaDTO dto = CreateShippingAreaDTO.builder()
                            .description(descriptionField.getValue())
                            .shippingProvider(providerComboBox.getValue())
                            .states(statesField.getValue())
                            .build();
                    areaService.saveShippingArea(dto);
                    Notification.show("Area created successfully!");
                } else {
                    ShippingArea updated = ShippingArea.builder()
                            .id(area.getId())
                            .description(descriptionField.getValue())
                            .shippingProvider(providerComboBox.getValue())
                            .states(statesField.getValue())
                            .active(area.isActive())
                            .build();
                    areaService.updateShippingArea(updated);
                    Notification.show("Area updated successfully!");
                }

                refreshAreasGrid();
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Error: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                ex.printStackTrace();
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button toggleStatusButton = new Button(
                (area != null && area.isActive()) ? "Deactivate" : "Activate",
                e -> {
                    String action = (area != null && area.isActive()) ? "deactivate" : "activate";
                    ConfirmDialog confirm = new ConfirmDialog(
                            "Confirm Status Change",
                            "Are you sure you want to " + action + " this shipping area?",
                            "Confirm",
                            confirmEvent -> {
                                try {
                                    if (area != null) {
                                        areaService.atualizarStatusShippingArea(area.getId());
                                        Notification.show("Status updated successfully!");
                                        refreshAreasGrid();
                                        dialog.close();
                                    }
                                } catch (Exception ex) {
                                    Notification.show("Error updating status: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
                                    ex.printStackTrace();
                                }
                            },
                            "Cancel",
                            cancelEvent -> {}
                    );
                    confirm.open();
                });
        toggleStatusButton.setVisible(!isNew);
        toggleStatusButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, toggleStatusButton, cancelButton);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonsLayout.setWidthFull();
        buttonsLayout.getStyle().set("margin-top", "25px");

        VerticalLayout formLayout = new VerticalLayout(
                descriptionField,
                providerComboBox,
                statesField,
                buttonsLayout);
        formLayout.setWidthFull();
        formLayout.setPadding(false);
        formLayout.setSpacing(true);
        formLayout.getStyle().set("padding", "25px");

        dialog.add(new VerticalLayout(title, new Hr(), formLayout));
        dialog.open();
    }
}