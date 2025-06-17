package com.codefathers.view;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.codefathers.model.dto.CreateEmployeeDTO;
import com.codefathers.model.dto.UpdateEmployeeDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.enums.EmployeeGender;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.repository.implementations.EmployeeRepositoryImpl;
import com.codefathers.service.EmployeeService;
import com.codefathers.util.ValidatorUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import com.vaadin.flow.data.provider.QuerySortOrder; // <<< IMPORT CORRETO ADICIONADO
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Employee | Gestão de funcionários")
@Route("employee")
public class EmployeeView extends VerticalLayout {
    private EmployeeService employeeService;
    private Employee currentEmployee;

    // Componentes para o diálogo de criação
    private TextField createfullname = new TextField("Full Name");
    private DatePicker createBirthDate = new DatePicker("Birth Date");
    private ComboBox<EmployeeGender> createGender = new ComboBox<>("Gender");
    private ComboBox<EmployeeRole> createRole = new ComboBox<>("Role");

    // Componentes para o diálogo de atualização
    private TextField updatefullname = new TextField("Full Name");
    private DatePicker updateBirthDate = new DatePicker("Birth Date");
    private ComboBox<EmployeeGender> updateGender = new ComboBox<>("Gender");
    private ComboBox<EmployeeRole> updateRole = new ComboBox<>("Role");

    // --- Botões do Diálogo de Criação ---
    private Button createEmployeeButton = new Button("Create Employee", new Icon(VaadinIcon.PLUS));
    private Button createSaveButton = new Button("Save");
    private Button createClearButton = new Button("Clear");
    private Button createCloseButton = new Button("Close");

    private Button updateSaveButton = new Button("Save");
    private Button updateClearButton = new Button("Clear");
    private Button updateCloseButton = new Button("Close");
    private Button setInactiveButton = new Button("Inactivate Employee");

    // Grid e Diálogos
    private Grid<Employee> grid = new Grid<>(Employee.class, false);
    private Dialog createDialog = new Dialog();
    private Dialog updateDialog = new Dialog();
    private TextField searchField = new TextField();
    private GridLazyDataView<Employee> dataView;
    private Grid.Column<Employee> statusColumn;

    // Utilitários e estado
    private String currentSearchTerm = "";
    private Checkbox showInactiveCheckbox = new Checkbox("Show inactive employees");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public EmployeeView() {
        var employeeRepository = new EmployeeRepositoryImpl();
        this.employeeService = new EmployeeService(employeeRepository, ValidatorUtil.getValidator());

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureFormFields();
        setupGrid();
        setupCreateDialog();
        setupUpdateDialog();
        setupEventListeners();
        setupSearchField();

        HorizontalLayout leftLayout = new HorizontalLayout(createEmployeeButton, searchField);
        leftLayout.setAlignItems(Alignment.CENTER);
        leftLayout.setSpacing(true);

        HorizontalLayout rightLayout = new HorizontalLayout(showInactiveCheckbox);
        rightLayout.setAlignItems(Alignment.CENTER);

        HorizontalLayout headerLayout = new HorizontalLayout(leftLayout, rightLayout);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setWidth("100%");

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

        createBirthDate.setPlaceholder("DD/MM/YYYY");
    }

    private void saveNewEmployee() {
        try {
            CreateEmployeeDTO dto = CreateEmployeeDTO.builder()
                    .fullName(createfullname.getValue())
                    .gender(createGender.getValue())
                    .role(createRole.getValue())
                    .birthDate(createBirthDate.getValue())
                    .active(true)
                    .build();

            employeeService.createEmployee(dto);
            Notification.show("Employee created successfully!");

            dataView.refreshAll();
            clearCreateForm();
            createDialog.close();

        } catch (Exception ex) {
            Notification.show("Error creating employee: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
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
            Notification.show("Employee updated successfully!");

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
            currentSearchTerm = e.getValue() != null ? e.getValue().trim() : "";
            dataView.refreshAll();
        });
    }

    // =================================================================================
    // MÉTODO CORRIGIDO
    // =================================================================================
    private void setupLazyDataProvider() {
        CallbackDataProvider<Employee, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    List<Employee> allEmployees = employeeService.employeeList();
                    Stream<Employee> stream = allEmployees.stream().filter(this::matchesCurrentFilters);

                    // Lógica de Ordenação corrigida para usar QuerySortOrder
                    if (!query.getSortOrders().isEmpty()) {
                        QuerySortOrder sortOrder = query.getSortOrders().get(0);
                        stream = stream.sorted(getComparator(sortOrder));
                    }

                    return stream.skip(query.getOffset()).limit(query.getLimit());
                },
                query -> {
                    List<Employee> allEmployees = employeeService.employeeList();
                    return (int) allEmployees.stream().filter(this::matchesCurrentFilters).count();
                });
        dataView = grid.setItems(dataProvider);
        showInactiveCheckbox.addValueChangeListener(e -> {
            statusColumn.setVisible(e.getValue());
            dataView.refreshAll();
        });
    }

    // =================================================================================
    // MÉTODO AUXILIAR CORRIGIDO
    // =================================================================================
    private Comparator<Employee> getComparator(QuerySortOrder sortOrder) {
        // Usa sortOrder.getSorted() que retorna a String (a chave da coluna)
        // diretamente
        Comparator<Employee> comparator = switch (sortOrder.getSorted()) {
            case "id" -> Comparator.comparing(Employee::getId);
            case "fullName" -> Comparator.comparing(Employee::getFullName);
            case "birthDate" -> Comparator.comparing(Employee::getBirthDate);
            case "gender" -> Comparator.comparing(e -> e.getGender().getLabel());
            case "role" -> Comparator.comparing(e -> e.getRole().getLabel());
            case "createdAt" ->
                Comparator.comparing(Employee::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            case "status" -> Comparator.comparing(Employee::isActive);
            default -> (e1, e2) -> 0;
        };

        if (sortOrder.getDirection() == SortDirection.DESCENDING) {
            return comparator.reversed();
        }
        return comparator;
    }

    private void setupGrid() {
        grid.addColumn(Employee::getId).setHeader("ID").setSortable(true).setKey("id").setFlexGrow(2);
        grid.addColumn(Employee::getFullName).setHeader("Full Name").setSortable(true).setKey("fullName")
                .setFlexGrow(2);
        grid.addColumn(Employee::getBirthDate).setHeader("Birth Date").setSortable(true).setKey("birthDate")
                .setFlexGrow(1);
        grid.addColumn(employee -> employee.getGender().getLabel()).setHeader("Gender").setSortable(true)
                .setKey("gender").setFlexGrow(1);
        grid.addColumn(employee -> employee.getRole().getLabel()).setHeader("Role").setSortable(true).setKey("role")
                .setFlexGrow(1);
        grid.addColumn(employee -> employee.getCreatedAt() != null ? employee.getCreatedAt().format(dateFormatter) : "")
                .setHeader("Created At").setSortable(true).setKey("createdAt").setFlexGrow(1);

        statusColumn = grid.addColumn(employee -> employee.isActive() ? "Active" : "Inactive")
                .setHeader("Status").setSortable(true).setKey("status")
                .setFlexGrow(0).setWidth("120px");
        statusColumn.setVisible(false);

        grid.addItemClickListener(event -> {
            if (event.getClickCount() == 2) {
                currentEmployee = event.getItem();
                populateUpdateForm(currentEmployee);
                updateDialog.open();
            }
        });

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setPageSize(20);
        grid.setWidth("100%");
        grid.setHeightFull();
    }

    private void setupCreateDialog() {
        createDialog.setHeaderTitle("Create Employee");
        createDialog.setDraggable(true);
        createDialog.setWidth("450px");

        createfullname.setWidthFull();
        createBirthDate.setWidthFull();
        createGender.setWidthFull();
        createRole.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout(createfullname, createBirthDate, createGender, createRole);
        formLayout.setSpacing(true);
        formLayout.setPadding(false);

        HorizontalLayout buttonsLayout = new HorizontalLayout(createSaveButton, createClearButton, createCloseButton);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonsLayout.setWidthFull();

        createDialog.add(new VerticalLayout(formLayout, buttonsLayout));
    }

    private void setupUpdateDialog() {
        updateDialog.setHeaderTitle("Update Employee");
        updateDialog.setDraggable(true);
        updateDialog.setWidth("450px");

        updatefullname.setWidthFull();
        updateBirthDate.setWidthFull();
        updateGender.setWidthFull();
        updateRole.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout(updatefullname, updateBirthDate, updateGender, updateRole);
        formLayout.setSpacing(true);
        formLayout.setPadding(false);

        HorizontalLayout buttonsLayout = new HorizontalLayout(updateSaveButton, updateClearButton, updateCloseButton,
                setInactiveButton);
        buttonsLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonsLayout.setWidthFull();

        updateDialog.add(new VerticalLayout(formLayout, buttonsLayout));
    }

    private void populateUpdateForm(Employee employee) {
        currentEmployee = employee;
        updatefullname.setValue(employee.getFullName());
        updateBirthDate.setValue(employee.getBirthDate());
        updateBirthDate.setReadOnly(true);
        updateGender.setValue(employee.getGender());
        updateRole.setValue(employee.getRole());
        setInactiveButton.setText(employee.isActive() ? "Set Inactive" : "Set Active");
    }

    private void setupEventListeners() {
        createEmployeeButton.addClickListener(e -> {
            clearCreateForm();
            createDialog.open();
        });

        createSaveButton.addClickListener(e -> saveNewEmployee());
        createClearButton.addClickListener(e -> clearCreateForm());
        createCloseButton.addClickListener(e -> createDialog.close());

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
        return matchesTerm(employee.getFullName(), searchTermLower)
                || matchesTerm(employee.getRole().getLabel(), searchTermLower);
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value != null && value.toLowerCase().contains(searchTerm);
    }

    private void clearCreateForm() {
        createfullname.clear();
        createBirthDate.clear();
        createRole.clear();
        createGender.clear();
    }

    private void clearUpdateForm() {
        updatefullname.clear();
        updateBirthDate.clear();
        updateBirthDate.setReadOnly(false);
        updateRole.clear();
        updateGender.clear();
        currentEmployee = null;
    }

    private void toggleEmployeeActive() {
        if (currentEmployee != null) {
            currentEmployee.setActive(!currentEmployee.isActive());
            updateExistingEmployee();
        }
    }
}