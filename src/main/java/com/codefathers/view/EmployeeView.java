package com.codefathers.view;

import com.codefathers.model.dto.CreateEmployeeDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.enums.EmployeeGender;
import com.codefathers.model.enums.EmployeeRole;
import com.codefathers.repository.EmployeeRepositoryImpl;
import com.codefathers.service.EmployeeService;
import com.codefathers.util.ValidationUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import javassist.runtime.Inner;
import java.time.LocalDate;

import static org.jsoup.helper.ValidationException.Validator;

@Route("employee")
public class EmployeeView extends VerticalLayout {
    private EmployeeService employeeService;

    private TextField fullname = new TextField("Nome completo");
    private DatePicker birthDate = new DatePicker("Data de nascimento");
    private ComboBox<EmployeeGender> gender = new ComboBox<>("Gênero");
    private ComboBox<EmployeeRole> role = new ComboBox<>("Cargo");

    private Button createEmployeeButton = new Button("Criar Funcionário");
    private Button saveButton = new Button("Salvar");
    private Button removeButton = new Button("Remover");
    private Button closeDialogButton = new Button("Fechar");

    private Grid<Employee> grid = new Grid<>(Employee.class, false);
    private Dialog dialog = new Dialog();
    private Employee currentEmployee;

    public EmployeeView() {
        var employeeRepository = new EmployeeRepositoryImpl();
        this.employeeService = new EmployeeService(employeeRepository, ValidationUtil.getValidator());

        configureFormFields();
        setUpForm();
        setUpGrid();
        setUpDialog();
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        HorizontalLayout contentWrapper = new HorizontalLayout();
        contentWrapper.setWidth("100%");
        contentWrapper.setMaxWidth("1200px");
        contentWrapper.setJustifyContentMode(JustifyContentMode.CENTER);

        contentWrapper.addClassNames(
            LumoUtility.Padding.Horizontal.LARGE,
            LumoUtility.Padding.Top.LARGE,
            LumoUtility.Padding.Bottom.LARGE
        );

        VerticalLayout innerContent = new VerticalLayout();
        innerContent.setAlignItems(Alignment.STRETCH);
        innerContent.add (
                new H1("Gerenciamento de funcionários"),
                createEmployeeButton,
                grid
        );
        innerContent.setSpacing(true);
        contentWrapper.add(innerContent);
        add(contentWrapper);
        updateGrid();
    }

    private void configureFormFields() {
        gender.setItems(EmployeeGender.values());
        gender.setItemLabelGenerator(EmployeeGender::name);

        role.setItems(EmployeeRole.values());
        role.setItemLabelGenerator(EmployeeRole::name);

        birthDate.setPlaceholder("YYYY-MM-DD");
    }

    private void saveEmployee() {
        String fullName = fullname.getValue();
        LocalDate employeeBirthDate = birthDate.getValue();
        EmployeeGender employeeGender = gender.getValue();
        EmployeeRole employeeRole = role.getValue();
        try {
            if (fullName.isEmpty() || employeeBirthDate == null || employeeGender == null || employeeRole == null) {
                Notification.show("Todos os campos são obrigatórios!", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (currentEmployee == null) {
                CreateEmployeeDTO createEmployeeDTO = CreateEmployeeDTO.builder()
                        .fullName(fullName)
                        .birthDate(employeeBirthDate)
                        .gender(employeeGender)
                        .role(employeeRole)
                        .build();

                employeeService.createEmployee(createEmployeeDTO);
                Notification.show("Funcionário criado com sucesso!", 3000, Notification.Position.MIDDLE);
            } else {
                Notification.show("O funcionário já existe.", 3000, Notification.Position.MIDDLE);
            }
            updateGrid();
            clearForm();
            dialog.close();
        } catch (IllegalArgumentException ex) {
            Notification.show("Erro de validação: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            ex.printStackTrace();
        } catch (Exception ex) {
            Notification.show("Erro ao salvar funcionário: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            ex.printStackTrace();
        }
    }

    private void setUpForm() {
        saveButton.addClickListener(e -> saveEmployee());
        removeButton.addClickListener(e -> removeEmployee());
        createEmployeeButton.addClickListener(e -> {
            clearForm();
            currentEmployee = null;
            dialog.open();
        });
        closeDialogButton.addClickListener(e -> dialog.close());
    }

    private void setUpGrid() {
        grid.addColumn(Employee::getFullName).setHeader("Nome completo").setAutoWidth(true);
        grid.addColumn(Employee::getBirthDate).setHeader("Data de nascimento").setAutoWidth(true);
        grid.addColumn(Employee::getGender).setHeader("Gênero").setAutoWidth(true);
        grid.addColumn(Employee::getRole).setHeader("Cargo").setAutoWidth(true);

        grid.asSingleSelect().addValueChangeListener(event -> {
            currentEmployee = event.getValue();
            if (currentEmployee != null) {
                populateForm(currentEmployee);
                dialog.open();
            } else {
                clearForm();
            }
        });
        grid.setHeight("300px");
    }

    private void setUpDialog() {
        dialog.setHeaderTitle("Cadastrar ou Editar Funcionário");
        dialog.setTop("50px");
        dialog.setLeft("50px");
        dialog.setResizable(true);
        dialog.setDraggable(true);
        dialog.getElement().getStyle().set("width", "400px");
        dialog.getElement().getStyle().set("height", "400px");

        HorizontalLayout buttons = new HorizontalLayout(saveButton, removeButton, closeDialogButton);
        VerticalLayout formLayout = new VerticalLayout(fullname, birthDate, gender, role, buttons);
        formLayout.setWidth("400px");

        dialog.add(formLayout);
    }

    private void updateGrid() {
        grid.setItems(employeeService.employeeList());
    }

    private void clearForm() {
        fullname.clear();
        birthDate.clear();
        gender.clear();
        role.clear();
        currentEmployee = null;
    }

    private void populateForm(Employee employee) {
        fullname.setValue(employee.getFullName());
        birthDate.setValue(employee.getBirthDate());
        gender.setValue(employee.getGender());
        role.setValue(employee.getRole());
    }

    private void removeEmployee() {
        if (currentEmployee != null) {
            employeeService.deleteEmployeeByID(currentEmployee.getId());
            Notification.show("Funcionário removido com sucesso!", 3000, Notification.Position.MIDDLE);
            updateGrid();
            clearForm();
            dialog.close();
        } else {
            Notification.show("Selecione um funcionário para remover.", 3000, Notification.Position.MIDDLE);
        }
    }
}