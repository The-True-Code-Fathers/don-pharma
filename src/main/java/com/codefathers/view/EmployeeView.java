package com.codefathers.view;

import com.codefathers.model.dto.CreateEmployeeDTO;
import com.codefathers.model.dto.CreateProductDTO;
import com.codefathers.model.dto.UpdateEmployeeDTO;
import com.codefathers.model.dto.UpdateProductDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Product;
import com.codefathers.model.enums.EmployeeGender;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.repository.implementations.EmployeeRepositoryImpl;
import com.codefathers.service.EmployeeService;
import com.codefathers.util.ValidatorUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;

@Route("employee")
public class EmployeeView extends VerticalLayout {
    private EmployeeService employeeService;
    private Employee currentEmployee;

    private TextField createfullname = new TextField("Full Name");
    private DatePicker createBirthDate = new DatePicker("Birth Date");
    private ComboBox<EmployeeGender> createGender = new ComboBox<>("Gender", EmployeeGender.values());
    private ComboBox<EmployeeRole> createRole = new ComboBox<>("Role", EmployeeRole.values());

    private TextField updatefullname = new TextField("Full Name");
    private DatePicker updateBirthDate = new DatePicker("Birth Date");
    private ComboBox<EmployeeGender> updateGender = new ComboBox<>("Gender", EmployeeGender.values());
    private ComboBox<EmployeeRole> updateRole = new ComboBox<>("Role", EmployeeRole.values());

    private Button createEmployeeButton = new Button("Create Employee");
    private Button createSaveButton = new Button("Save");
    private Button createClearButton = new Button("Clear");
    private Button createCloseButton = new Button("Close");


    private Button updateSaveButton = new Button("Save");
    private Button updateClearButton = new Button("Clear");
    private Button updateCloseButton = new Button("Close");
    private Button setInactiveButton = new Button("Inactivate Employee");

    private Grid<Employee> grid = new Grid<>(Employee.class, false);
    private Dialog createDialog = new Dialog();
    private Dialog updateDialog = new Dialog();
    private TextField searchField = new TextField();
    private GridLazyDataView<Employee> dataView;
    private Grid.Column<Employee> statusColumn;

    private String currentSearchTerm = "";
    private Boolean currentShowInactive = false;

    private com.vaadin.flow.component.checkbox.Checkbox showInactiveCheckbox =
            new com.vaadin.flow.component.checkbox.Checkbox("Show inactive employees");


    public EmployeeView() {
        var employeeRepository = new EmployeeRepositoryImpl();
        this.employeeService = new EmployeeService(employeeRepository, ValidatorUtil.getValidator());

        configureFormFields();
        setupGrid();
        setupCreateDialog();
        setupUpdateDialog();
        setupEventListeners();
        setSizeFull();
        setupSearchField();

        HorizontalLayout leftLayout = new HorizontalLayout(createEmployeeButton, searchField);
        leftLayout.setAlignItems(Alignment.CENTER);
        leftLayout.setSpacing(true);
        
        HorizontalLayout rightLayout = new HorizontalLayout(showInactiveCheckbox);
        rightLayout.setAlignItems(Alignment.CENTER);

        HorizontalLayout headerLayout = new HorizontalLayout(leftLayout, rightLayout);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setWidth("80%");

        add(headerLayout, grid);
        setupLazyDataProvider();
    }


    private void configureFormFields() {
        createGender.setItems(EmployeeGender.values());
        createGender.setItemLabelGenerator(EmployeeGender::getLabel);

        updateGender.setItems(EmployeeGender.values());
        updateGender.setItemLabelGenerator(EmployeeGender::getLabel);

        createRole.setItems(EmployeeRole.values());
        createRole.setItemLabelGenerator(EmployeeRole::getLabel);

        updateRole.setItems(EmployeeRole.values());
        updateRole.setItemLabelGenerator(EmployeeRole::getLabel);

        createBirthDate.setPlaceholder("YYYY-MM-DD");
    }

    private void saveNewEmployee() {
        try {
            CreateEmployeeDTO dto = CreateEmployeeDTO.builder()
                    .fullName(createfullname.getValue())
                    .gender(createGender.getValue())
                    .role(createRole.getValue())
                    .birthDate(createBirthDate.getValue())
                    .active(true) // New employees are active by default
                    .build();

            employeeService.createEmployee(dto);
            Notification.show("Employee created successfully!");

            dataView.refreshAll();
            clearCreateForm();
            createDialog.close();

        } catch (Exception ex) {
            Notification.show("Error creating product: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            ex.printStackTrace();
        }
    }

    private void updateExistingEmployee() {
        try {
            if (currentEmployee == null) {
                Notification.show("No Employee selected for update");
                return;
            }

            UpdateEmployeeDTO dto = UpdateEmployeeDTO.builder()
                    .fullName(updatefullname.getValue())
                    .role(updateRole.getValue())
                    .birthDate(updateBirthDate.getValue())
                    .gender(updateGender.getValue())
                    .active(currentEmployee.isActive())
                    .build();

            employeeService.updateEmployee(dto, currentEmployee.getId());
            Notification.show("Product updated successfully!");

            dataView.refreshAll();
            clearUpdateForm();
            updateDialog.close();

        } catch (Exception ex) {
            Notification.show("Error updating employee: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            ex.printStackTrace();
        }
    }

    private void setupSearchField() {
        searchField.setWidth("400px");
        searchField.setPlaceholder("Search by name...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setClearButtonVisible(true);

        searchField.addValueChangeListener(e -> {
            currentSearchTerm = e.getValue().trim();
            dataView.refreshAll();
        });
    }

    private void setupLazyDataProvider() {
        CallbackDataProvider<Employee, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();

                    List<Employee> allEmployees = employeeService.employeeList();

                    return allEmployees.stream()
                            .filter(this::matchesCurrentFilters)
                            .skip(offset)
                            .limit(limit);
                },
                query -> {
                    List<Employee> allEmployees = employeeService.employeeList();
                    return (int) allEmployees.stream()
                            .filter(this::matchesCurrentFilters)
                            .count();
                }
        );

        dataView = grid.setItems(dataProvider);

        showInactiveCheckbox.addValueChangeListener(e -> {
            currentShowInactive = e.getValue();
            statusColumn.setVisible(e.getValue());
            dataView.refreshAll();
        });
    }


    private void setupGrid() {
        grid.addColumn(Employee::getFullName).setHeader("Full Name").setAutoWidth(true);
        grid.addColumn(Employee::getBirthDate).setHeader("Birth Date").setAutoWidth(true);
        grid.addColumn(employee -> employee.getGender().getLabel()).setHeader("Gender").setAutoWidth(true);
        grid.addColumn(employee -> employee.getRole().getLabel()).setHeader("Role").setAutoWidth(true);

        statusColumn = grid.addColumn(product -> product.isActive() ? "Active" : "Inactive")
                .setHeader("Status")
                .setAutoWidth(true);
        statusColumn.setVisible(false);

        grid.addItemClickListener(event -> {
            if (event.getClickCount() == 2) {
                currentEmployee= event.getItem();
                populateUpdateForm(currentEmployee);
                updateDialog.open();
            }
        });

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setPageSize(20);
        grid.setWidth("80%");
    }

    private void setupCreateDialog() {
        createDialog.setHeaderTitle("Create Employee");
        createDialog.setDraggable(true);
        createDialog.getElement().getStyle().set("width", "400px");
        createDialog.getElement().getStyle().set("height", "350px");

        createfullname.setWidth("350px");
        createBirthDate.setWidth("350px");
        createGender.setWidth("350px");
        createRole.setWidth("350px");

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.add(createfullname, createBirthDate, createGender, createRole);
        formLayout.setSpacing(true);
        formLayout.setMargin(true);

        HorizontalLayout buttonsLayout = new HorizontalLayout(createSaveButton, createClearButton, createCloseButton);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.EVENLY);

        // Main layout
        VerticalLayout mainLayout = new VerticalLayout(formLayout, buttonsLayout);
        mainLayout.setSpacing(true);
        mainLayout.setPadding(true);

        createDialog.add(mainLayout);
    }

    private void setupUpdateDialog() {
        updateDialog.setHeaderTitle("Update Employee");
        updateDialog.setDraggable(true);
        updateDialog.getElement().getStyle().set("width", "400px");
        updateDialog.getElement().getStyle().set("height", "350px");

        updatefullname.setWidth("350px");
        updateBirthDate.setWidth("350px");
        updateGender.setWidth("350px");
        updateRole.setWidth("350px");

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.add(updatefullname, updateBirthDate, updateGender, updateRole);
        formLayout.setSpacing(true);
        formLayout.setMargin(true);

        HorizontalLayout buttonsLayout = new HorizontalLayout(updateSaveButton, updateClearButton, updateCloseButton, setInactiveButton);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.EVENLY);

        // Main layout
        VerticalLayout mainLayout = new VerticalLayout(formLayout, buttonsLayout);
        mainLayout.setSpacing(true);
        mainLayout.setPadding(true);

        updateDialog.add(mainLayout);
    }

    private void updateGrid() {
        grid.setItems(employeeService.employeeList());
    }


    private void populateForm(Employee employee) {
        createfullname.setValue(employee.getFullName());
        createBirthDate.setValue(employee.getBirthDate());
        createGender.setValue(employee.getGender());
        createRole.setValue(employee.getRole());
    }

    private void populateUpdateForm(Employee employee) {
        updatefullname.setValue(employee.getFullName());
        updateBirthDate.setValue(employee.getBirthDate());
        updateBirthDate.setReadOnly(true);
        updateGender.setValue(employee.getGender());
        updateRole.setValue(employee.getRole());

        // Update button text based on product status
        setInactiveButton.setText(employee.isActive() ? "Set Inactive" : "Set Active");
    }

    private void setupEventListeners() {
        // Create dialog events
        createEmployeeButton.addClickListener(e -> {
            clearCreateForm();
            createDialog.open();
        });

        createSaveButton.addClickListener(e -> saveNewEmployee());
        createClearButton.addClickListener(e -> clearCreateForm());
        createCloseButton.addClickListener(e -> createDialog.close());

        // Update dialog events
        updateSaveButton.addClickListener(e -> updateExistingEmployee());
        updateClearButton.addClickListener(e -> clearUpdateForm());
        updateCloseButton.addClickListener(e -> updateDialog.close());
        setInactiveButton.addClickListener(e -> toggleEmployeeActive());
    }

    private boolean matchesCurrentFilters(Employee employee) {
        if (!showInactiveCheckbox.getValue() && !employee.isActive()) {
            return false;
        }

        if (currentSearchTerm.isEmpty()) {
            return true;
        }

        String searchTermLower = currentSearchTerm.toLowerCase();

        boolean matchesId = matchesTerm(employee.getId().toString(), searchTermLower);
        boolean matchesName = matchesTerm(employee.getFullName(), searchTermLower);
        boolean matchesRole = matchesTerm(employee.getRole().toString(), searchTermLower);


        return matchesId || matchesName || matchesRole;
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value != null && value.toLowerCase().contains(searchTerm);
    }

    private void clearCreateForm() {
        createfullname.clear();
        createfullname.setReadOnly(false);
        createBirthDate.clear();
        createRole.clear();
        createGender.clear();
    }

    private void clearUpdateForm() {
        createfullname.clear();
        createfullname.setReadOnly(false);
        createBirthDate.clear();
        createRole.clear();
        createGender.clear();
        currentEmployee = null;
    }

    private void toggleEmployeeActive() {
        if (currentEmployee != null) {
            currentEmployee.setActive(!currentEmployee.isActive());
            setInactiveButton.setText(currentEmployee.isActive() ? "Set Inactive" : "Set Active");
            updateExistingEmployee();
        }
    }
}