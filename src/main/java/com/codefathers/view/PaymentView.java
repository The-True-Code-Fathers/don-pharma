package com.codefathers.view;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.codefathers.model.dto.CreatePaymentDTO;
import com.codefathers.model.entity.Employee;
import com.codefathers.model.entity.Payment;
import com.codefathers.model.enums.EmployeeBenefits;
import com.codefathers.repository.implementations.EmployeeRepositoryImpl;
import com.codefathers.repository.implementations.PaymentRepositoryImpl;
import com.codefathers.service.EmployeeService;
import com.codefathers.service.PaymentService;
import com.codefathers.util.TaxCalculatorUtil;
import com.codefathers.util.ValidatorUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

@Route("payment")
public class PaymentView extends VerticalLayout {
    private final PaymentService paymentService;
    private final EmployeeService employeeService;

    private final TextField searchField = new TextField();
    private final Button openDialogButton = new Button("Create Payment");

    private final Grid<Payment> grid = new Grid<>(Payment.class, false);
    private final Dialog paymentDialog = new Dialog();
    private final Dialog editDialog = new Dialog();

    // Form fields
    private final ComboBox<Employee> employeeComboBox = new ComboBox<>("Employee");
    private final NumberField grossIncomeField = new NumberField("Gross Income");
    private final NumberField amountInTaxesField = new NumberField("Amount in Taxes");
    private final NumberField mealVoucherField = new NumberField("Meal Voucher");
    private final NumberField foodVoucherField = new NumberField("Food Voucher");
    private final NumberField healthInsuranceField = new NumberField("Health Insurance");
    private final NumberField dentalInsuranceField = new NumberField("Dental Insurance");
    private final NumberField profitSharingField = new NumberField("Profit Sharing");
    private final TextField netIncomeDisplay = new TextField("Net Income");
    private final TextField totalIncomeDisplay = new TextField("Total Income");

    private String currentSearchTerm = "";
    private Payment currentPaymentEditing = null;

    // Edit dialog fields
    private final ComboBox<Employee> editEmployeeComboBox = new ComboBox<>("Employee");
    private final NumberField editGrossIncomeField = new NumberField("Gross Income");
    private final NumberField editAmountInTaxesField = new NumberField("Amount in Taxes");
    private final NumberField editMealVoucherField = new NumberField("Meal Voucher");
    private final NumberField editFoodVoucherField = new NumberField("Food Voucher");
    private final NumberField editHealthInsuranceField = new NumberField("Health Insurance");
    private final NumberField editDentalInsuranceField = new NumberField("Dental Insurance");
    private final NumberField editProfitSharingField = new NumberField("Profit Sharing");
    private final TextField editNetIncomeDisplay = new TextField("Net Income");
    private final TextField editTotalIncomeDisplay = new TextField("Total Income");
    private final Button toggleStatusButton = new Button(); // Botão de ativar/desativar

    public PaymentView() {
        var paymentRepository = new PaymentRepositoryImpl();
        var employeeRepository = new EmployeeRepositoryImpl();

        this.paymentService = new PaymentService(paymentRepository, employeeRepository);
        this.employeeService = new EmployeeService(employeeRepository, ValidatorUtil.getValidator());

        setupSearchField();
        setupGrid();
        setupPaymentDialog();
        setupEditDialog();

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setWidthFull();
        topLayout.setAlignItems(Alignment.END);

        searchField.setWidth("300px");
        topLayout.add(openDialogButton, searchField);

        add(topLayout, grid, paymentDialog, editDialog);

        refreshGrid();
    }

    private void setupGrid() {
        grid.addColumn(payment -> payment.getId().toString()).setHeader("ID").setSortable(true);
        grid.addColumn(payment -> payment.getEmployee().getFullName()).setHeader("Employee").setSortable(true);
        grid.addColumn(payment -> payment.getEmployee().getRole().toString()).setHeader("Role").setSortable(true);
        grid.addColumn(payment -> "R$ " + payment.getGrossIncome().toString()).setHeader("Gross Income")
                .setSortable(true);
        grid.addColumn(payment -> "R$ " + calculateNetIncome(payment).toString()).setHeader("Net Income")
                .setSortable(true);
        grid.addColumn(payment -> "R$ " + calculateTotalIncome(payment).toString()).setHeader("Total Income")
                .setSortable(true);
        grid.addColumn(payment -> payment.getCreatedAt().toString()).setHeader("Created At").setSortable(true);
        grid.addColumn(payment -> payment.isActive() ? "Active" : "Inactive").setHeader("Status").setSortable(true);

        grid.setAllRowsVisible(true);
        grid.setWidth("90%");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        grid.addItemDoubleClickListener(event -> {
            Payment selectedPayment = event.getItem();
            showPaymentDetails(selectedPayment);
        });
    }

    // Método para calcular Net Income (Salário - Impostos)
    private BigDecimal calculateNetIncome(Payment payment) {
        return payment.getGrossIncome().subtract(payment.getAmountInTaxes());
    }

    // Método para calcular Total Income (Salário + Benefícios - Impostos)
    private BigDecimal calculateTotalIncome(Payment payment) {
        BigDecimal totalBenefits = payment.getMealVoucherAmount()
                .add(payment.getFoodVoucherAmount())
                .add(payment.getHealthInsuranceAmount())
                .add(payment.getDentalInsuranceAmount())
                .add(payment.getProfitSharingAmount());

        return payment.getGrossIncome()
                .add(totalBenefits)
                .subtract(payment.getAmountInTaxes());
    }

    private void setupSearchField() {
        searchField.setPlaceholder("Search payments...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setClearButtonVisible(true);

        searchField.addValueChangeListener(e -> {
            currentSearchTerm = e.getValue().trim().toLowerCase();
            refreshGrid();
        });
    }

    private void setupPaymentDialog() {
        employeeComboBox.setItems(employeeService.employeeList());
        employeeComboBox
                .setItemLabelGenerator(employee -> employee.getFullName() + " (" + employee.getRole().toString() + ")");
        employeeComboBox.setPlaceholder("Select an employee");
        employeeComboBox.addValueChangeListener(e -> {
            Employee selectedEmployee = e.getValue();
            if (selectedEmployee != null) {
                populateDefaultValues(selectedEmployee);
                // Recalcular taxes quando gross income mudar
                if (grossIncomeField.getValue() != null) {
                    updateTaxesForCreation(selectedEmployee, grossIncomeField.getValue());
                }
            } else {
                clearFields();
            }
            updateIncomeDisplays();
        });
        // Setup number fields with R$ prefix
        setupNumberFieldWithCurrency(grossIncomeField, 0.01);
        setupNumberFieldWithCurrency(amountInTaxesField, 0.00);
        setupNumberFieldWithCurrency(mealVoucherField, 0.00);
        setupNumberFieldWithCurrency(foodVoucherField, 0.00);
        setupNumberFieldWithCurrency(healthInsuranceField, 0.00);
        setupNumberFieldWithCurrency(dentalInsuranceField, 0.00);
        setupNumberFieldWithCurrency(profitSharingField, 0.00);

        amountInTaxesField.setReadOnly(true);

        // Setup income display fields
        setupIncomeDisplayField(netIncomeDisplay);
        setupIncomeDisplayField(totalIncomeDisplay);

        // Employee selection listener to populate default values
        employeeComboBox.addValueChangeListener(e -> {
            Employee selectedEmployee = e.getValue();
            if (selectedEmployee != null) {
                populateDefaultValues(selectedEmployee);
            } else {
                clearFields();
            }
            updateIncomeDisplays();
        });

        // Add value change listeners to update income displays
        grossIncomeField.addValueChangeListener(e -> updateIncomeDisplays());
        amountInTaxesField.addValueChangeListener(e -> updateIncomeDisplays());
        mealVoucherField.addValueChangeListener(e -> updateIncomeDisplays());
        foodVoucherField.addValueChangeListener(e -> updateIncomeDisplays());
        healthInsuranceField.addValueChangeListener(e -> updateIncomeDisplays());
        dentalInsuranceField.addValueChangeListener(e -> updateIncomeDisplays());
        profitSharingField.addValueChangeListener(e -> updateIncomeDisplays());

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        formLayout.add(
                employeeComboBox, grossIncomeField,
                amountInTaxesField, mealVoucherField,
                foodVoucherField, healthInsuranceField,
                dentalInsuranceField, profitSharingField,
                netIncomeDisplay, totalIncomeDisplay);

        Button createButton = new Button("Create Payment", e -> createPayment());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> {
            paymentDialog.close();
            clearFields();
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(createButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout dialogContent = new VerticalLayout(formLayout, buttonLayout);
        dialogContent.setPadding(true);
        dialogContent.setSpacing(true);

        grossIncomeField.addValueChangeListener(e -> {
            if (employeeComboBox.getValue() != null && e.getValue() != null) {
                updateTaxesForCreation(employeeComboBox.getValue(), e.getValue());
            }
            updateIncomeDisplays();
        });

        paymentDialog.setHeaderTitle("Create Payment");
        paymentDialog.add(dialogContent);
        paymentDialog.setWidth("600px");

        openDialogButton.addClickListener(e -> paymentDialog.open());
    }

    private void setupEditDialog() {
        editEmployeeComboBox.setItems(employeeService.employeeList());
        editEmployeeComboBox
                .setItemLabelGenerator(employee -> employee.getFullName() + " (" + employee.getRole().toString() + ")");

        setupNumberFieldWithCurrency(editGrossIncomeField, 0.01);
        setupNumberFieldWithCurrency(editAmountInTaxesField, 0.00);
        setupNumberFieldWithCurrency(editMealVoucherField, 0.00);
        setupNumberFieldWithCurrency(editFoodVoucherField, 0.00);
        setupNumberFieldWithCurrency(editHealthInsuranceField, 0.00);
        setupNumberFieldWithCurrency(editDentalInsuranceField, 0.00);
        setupNumberFieldWithCurrency(editProfitSharingField, 0.00);

        // CORREÇÃO 1: Bloquear o campo Amount in Taxes para edição
        editAmountInTaxesField.setReadOnly(true);

        setupIncomeDisplayField(editNetIncomeDisplay);
        setupIncomeDisplayField(editTotalIncomeDisplay);

        editEmployeeComboBox.addValueChangeListener(e -> {
            if (e.getValue() != null && editGrossIncomeField.getValue() != null) {
                updateTaxesBasedOnEmployee(e.getValue(), editGrossIncomeField.getValue());
            }
            updateEditIncomeDisplays();
        });

        editGrossIncomeField.addValueChangeListener(e -> {
            if (editEmployeeComboBox.getValue() != null && e.getValue() != null) {
                updateTaxesBasedOnEmployee(editEmployeeComboBox.getValue(), e.getValue());
            }
            updateEditIncomeDisplays();
        });

        // Remover o listener do editAmountInTaxesField já que agora é read-only
        editMealVoucherField.addValueChangeListener(e -> updateEditIncomeDisplays());
        editFoodVoucherField.addValueChangeListener(e -> updateEditIncomeDisplays());
        editHealthInsuranceField.addValueChangeListener(e -> updateEditIncomeDisplays());
        editDentalInsuranceField.addValueChangeListener(e -> updateEditIncomeDisplays());
        editProfitSharingField.addValueChangeListener(e -> updateEditIncomeDisplays());
        editAmountInTaxesField.addValueChangeListener(e -> updateEditIncomeDisplays());

        FormLayout editFormLayout = new FormLayout();
        editFormLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        editFormLayout.add(
                editEmployeeComboBox, editGrossIncomeField,
                editAmountInTaxesField, editMealVoucherField,
                editFoodVoucherField, editHealthInsuranceField,
                editDentalInsuranceField, editProfitSharingField,
                editNetIncomeDisplay, editTotalIncomeDisplay);

        Button updateButton = new Button("Update Payment", e -> {
            if (currentPaymentEditing != null) {
                try {
                    currentPaymentEditing.setEmployee(editEmployeeComboBox.getValue());
                    currentPaymentEditing.setGrossIncome(BigDecimal.valueOf(editGrossIncomeField.getValue()));
                    currentPaymentEditing.setAmountInTaxes(BigDecimal.valueOf(editAmountInTaxesField.getValue()));
                    currentPaymentEditing.setMealVoucherAmount(BigDecimal.valueOf(editMealVoucherField.getValue()));
                    currentPaymentEditing.setFoodVoucherAmount(BigDecimal.valueOf(editFoodVoucherField.getValue()));
                    currentPaymentEditing
                            .setHealthInsuranceAmount(BigDecimal.valueOf(editHealthInsuranceField.getValue()));
                    currentPaymentEditing
                            .setDentalInsuranceAmount(BigDecimal.valueOf(editDentalInsuranceField.getValue()));
                    currentPaymentEditing.setProfitSharingAmount(BigDecimal.valueOf(editProfitSharingField.getValue()));

                    paymentService.update(currentPaymentEditing);
                    Notification.show("Payment updated successfully.");
                    refreshGrid();
                    editDialog.close();
                } catch (Exception ex) {
                    Notification.show("Error updating payment: " + ex.getMessage());
                }
            }
        });
        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Configure the toggleStatusButton
        toggleStatusButton.addClickListener(e -> {
            if (currentPaymentEditing != null) {
                togglePaymentStatus(currentPaymentEditing);
                // Update button text and theme immediately after toggle
                updateToggleStatusButton(currentPaymentEditing.isActive());
                // Also update the fields' enabled state based on the new status
                setEditFieldsEnabled(currentPaymentEditing.isActive());
            }
        });

        Button cancelEditButton = new Button("Cancel", e -> editDialog.close());

        HorizontalLayout editButtonLayout = new HorizontalLayout(updateButton, toggleStatusButton, cancelEditButton);
        editButtonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout editDialogContent = new VerticalLayout(editFormLayout, editButtonLayout);
        editDialogContent.setPadding(true);
        editDialogContent.setSpacing(true);

        editDialog.setHeaderTitle("Edit Payment");
        editDialog.add(editDialogContent);
        editDialog.setWidth("600px");
    }

    private void updateEditIncomeDisplays() {
        if (editGrossIncomeField.getValue() == null)
            return;

        BigDecimal grossIncome = BigDecimal.valueOf(editGrossIncomeField.getValue());
        BigDecimal amountInTaxes = BigDecimal.valueOf(
                editAmountInTaxesField.getValue() != null ? editAmountInTaxesField.getValue() : 0);
        BigDecimal mealVoucher = BigDecimal.valueOf(
                editMealVoucherField.getValue() != null ? editMealVoucherField.getValue() : 0);
        BigDecimal foodVoucher = BigDecimal.valueOf(
                editFoodVoucherField.getValue() != null ? editFoodVoucherField.getValue() : 0);
        BigDecimal healthInsurance = BigDecimal.valueOf(
                editHealthInsuranceField.getValue() != null ? editHealthInsuranceField.getValue() : 0);
        BigDecimal dentalInsurance = BigDecimal.valueOf(
                editDentalInsuranceField.getValue() != null ? editDentalInsuranceField.getValue() : 0);
        BigDecimal profitSharing = BigDecimal.valueOf(
                editProfitSharingField.getValue() != null ? editProfitSharingField.getValue() : 0);

        // Net Income = Gross Income - Taxes
        BigDecimal netIncome = grossIncome.subtract(amountInTaxes);

        // Total Income = Gross Income + Benefits - Taxes
        BigDecimal totalBenefits = mealVoucher.add(foodVoucher)
                .add(healthInsurance).add(dentalInsurance).add(profitSharing);
        BigDecimal totalIncome = grossIncome.add(totalBenefits).subtract(amountInTaxes);

        editNetIncomeDisplay.setValue("R$ " + netIncome.toString());
        editTotalIncomeDisplay.setValue("R$ " + totalIncome.toString());
    }

    private void updateTaxesBasedOnEmployee(Employee employee, Double grossIncome) {
        if (employee != null && grossIncome != null && grossIncome > 0) {
            BigDecimal grossIncomeDecimal = BigDecimal.valueOf(grossIncome);
            BigDecimal calculatedTax = TaxCalculatorUtil.calculateIR(grossIncomeDecimal);
            editAmountInTaxesField.setValue(calculatedTax.doubleValue());
        }
    }

    private void setupIncomeDisplayField(TextField field) {
        field.setReadOnly(true);
        field.getStyle()
                .set("--lumo-contrast-60pct", "var(--lumo-primary-color)")
                .set("font-weight", "bold");
    }

    private void setupNumberFieldWithCurrency(NumberField field, double min) {
        field.setMin(min);
        field.setStep(0.01);
        field.setValue(0.0);

        // Adiciona o prefixo R$
        Span prefix = new Span("R$");
        prefix.getElement().getThemeList().add("badge");
        field.setPrefixComponent(prefix);
    }

    // Substitua o método updateTaxesForCreation por este:
    private void updateTaxesForCreation(Employee employee, Double grossIncome) {
        if (employee != null && grossIncome != null && grossIncome > 0) {
            BigDecimal grossIncomeDecimal = BigDecimal.valueOf(grossIncome);
            BigDecimal calculatedTax = TaxCalculatorUtil.calculateIR(grossIncomeDecimal);
            amountInTaxesField.setValue(calculatedTax.doubleValue());
        }
    }

    private void populateDefaultValues(Employee employee) {
        Map<EmployeeBenefits, BigDecimal> benefits = employee.getRole().getBENEFITS();

        grossIncomeField.setValue(benefits.get(EmployeeBenefits.GROSS_INCOME).doubleValue());
        amountInTaxesField.setValue(benefits.get(EmployeeBenefits.AMOUNT_IN_TAXES).doubleValue());
        mealVoucherField.setValue(benefits.get(EmployeeBenefits.MEAL_VOUCHER).doubleValue());
        foodVoucherField.setValue(benefits.get(EmployeeBenefits.FOOD_VOUCHER).doubleValue());
        healthInsuranceField.setValue(benefits.get(EmployeeBenefits.HEALTH_INSURANCE).doubleValue());
        dentalInsuranceField.setValue(benefits.get(EmployeeBenefits.DENTAL_INSURANCE).doubleValue());
        profitSharingField.setValue(benefits.get(EmployeeBenefits.PROFIT_SHARING).doubleValue());
    }

    private void clearFields() {
        employeeComboBox.clear();
        grossIncomeField.setValue(0.0);
        amountInTaxesField.setValue(0.0);
        mealVoucherField.setValue(0.0);
        foodVoucherField.setValue(0.0);
        healthInsuranceField.setValue(0.0);
        dentalInsuranceField.setValue(0.0);
        profitSharingField.setValue(0.0);
        netIncomeDisplay.setValue("");
        totalIncomeDisplay.setValue("");
    }

    private void updateIncomeDisplays() {
        if (grossIncomeField.getValue() == null)
            return;

        BigDecimal grossIncome = BigDecimal.valueOf(grossIncomeField.getValue());
        BigDecimal amountInTaxes = BigDecimal
                .valueOf(amountInTaxesField.getValue() != null ? amountInTaxesField.getValue() : 0);
        BigDecimal mealVoucher = BigDecimal
                .valueOf(mealVoucherField.getValue() != null ? mealVoucherField.getValue() : 0);
        BigDecimal foodVoucher = BigDecimal
                .valueOf(foodVoucherField.getValue() != null ? foodVoucherField.getValue() : 0);
        BigDecimal healthInsurance = BigDecimal
                .valueOf(healthInsuranceField.getValue() != null ? healthInsuranceField.getValue() : 0);
        BigDecimal dentalInsurance = BigDecimal
                .valueOf(dentalInsuranceField.getValue() != null ? dentalInsuranceField.getValue() : 0);
        BigDecimal profitSharing = BigDecimal
                .valueOf(profitSharingField.getValue() != null ? profitSharingField.getValue() : 0);

        // Net Income = Gross Income - Taxes
        BigDecimal netIncome = grossIncome.subtract(amountInTaxes);

        // Total Income = Gross Income + Benefits - Taxes
        BigDecimal totalBenefits = mealVoucher.add(foodVoucher)
                .add(healthInsurance).add(dentalInsurance).add(profitSharing);
        BigDecimal totalIncome = grossIncome.add(totalBenefits).subtract(amountInTaxes);

        netIncomeDisplay.setValue("R$ " + netIncome.toString());
        totalIncomeDisplay.setValue("R$ " + totalIncome.toString());
    }

    private void createPayment() {
        Employee employee = employeeComboBox.getValue();

        if (employee == null) {
            Notification.show("Please select an employee.");
            return;
        }

        if (grossIncomeField.getValue() == null || grossIncomeField.getValue() <= 0) {
            Notification.show("Gross income must be greater than zero.");
            return;
        }

        CreatePaymentDTO dto = CreatePaymentDTO.builder()
                .employee(employee)
                .grossIncome(BigDecimal.valueOf(grossIncomeField.getValue()))
                .amountInTaxes(BigDecimal.valueOf(amountInTaxesField.getValue()))
                .mealVoucherAmount(BigDecimal.valueOf(mealVoucherField.getValue()))
                .foodVoucherAmount(BigDecimal.valueOf(foodVoucherField.getValue()))
                .healthInsuranceAmount(BigDecimal.valueOf(healthInsuranceField.getValue()))
                .dentalInsuranceAmount(BigDecimal.valueOf(dentalInsuranceField.getValue()))
                .profitSharingAmount(BigDecimal.valueOf(profitSharingField.getValue()))
                .build();

        try {
            Payment payment = paymentService.createPayment(dto);
            if (payment != null) {
                Notification.show("Payment created successfully.");
                clearFields();
                refreshGrid();
                paymentDialog.close();
            } else {
                Notification.show("Error creating payment. Please check the values.");
            }
        } catch (Exception ex) {
            Notification.show("Error creating payment: " + ex.getMessage());
        }
    }

    private void openEditDialog(Payment payment) {
        currentPaymentEditing = payment;

        // Populate fields with current payment data
        editEmployeeComboBox.setValue(payment.getEmployee());
        editGrossIncomeField.setValue(payment.getGrossIncome().doubleValue());
        editAmountInTaxesField.setValue(payment.getAmountInTaxes().doubleValue());
        editMealVoucherField.setValue(payment.getMealVoucherAmount().doubleValue());
        editFoodVoucherField.setValue(payment.getFoodVoucherAmount().doubleValue());
        editHealthInsuranceField.setValue(payment.getHealthInsuranceAmount().doubleValue());
        editDentalInsuranceField.setValue(payment.getDentalInsuranceAmount().doubleValue());
        editProfitSharingField.setValue(payment.getProfitSharingAmount().doubleValue());

        // Update income displays
        BigDecimal netIncome = calculateNetIncome(payment);
        BigDecimal totalIncome = calculateTotalIncome(payment);
        editNetIncomeDisplay.setValue("R$ " + netIncome.toString());
        editTotalIncomeDisplay.setValue("R$ " + totalIncome.toString());

        // Set the initial state of the toggle button and fields
        updateToggleStatusButton(payment.isActive());
        setEditFieldsEnabled(payment.isActive());

        editDialog.open();
    }

    private void togglePaymentStatus(Payment payment) {
        payment.setActive(!payment.isActive());
        try {
            paymentService.update(payment);
            Notification.show("Payment status updated.");
            refreshGrid();
        } catch (Exception e) {
            Notification.show("Error updating payment status: " + e.getMessage());
        }
    }

    // Helper method to update the toggle status button text and theme
    private void updateToggleStatusButton(boolean isActive) {
        if (isActive) {
            toggleStatusButton.setText("Deactivate");
            toggleStatusButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            toggleStatusButton.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
        } else {
            toggleStatusButton.setText("Activate");
            toggleStatusButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            toggleStatusButton.removeThemeVariants(ButtonVariant.LUMO_ERROR);
        }
    }

    // CORREÇÃO 4: Atualizar o método setEditFieldsEnabled para não desabilitar o
    // campo de taxes
    private void setEditFieldsEnabled(boolean enabled) {
        editEmployeeComboBox.setEnabled(enabled);
        editGrossIncomeField.setEnabled(enabled);
        // editAmountInTaxesField sempre fica read-only, não precisa ser controlado aqui
        editMealVoucherField.setEnabled(enabled);
        editFoodVoucherField.setEnabled(enabled);
        editHealthInsuranceField.setEnabled(enabled);
        editDentalInsuranceField.setEnabled(enabled);
        editProfitSharingField.setEnabled(enabled);
    }

    private void showPaymentDetails(Payment payment) {
        Dialog detailsDialog = new Dialog();
        detailsDialog.setHeaderTitle("Payment Details");
        detailsDialog.setWidth("600px");
        detailsDialog.setHeight("500px");

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        FormLayout paymentInfoLayout = new FormLayout();
        paymentInfoLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        TextField idField = new TextField("Payment ID");
        idField.setValue(payment.getId().toString());
        idField.setReadOnly(true);

        TextField employeeField = new TextField("Employee");
        employeeField.setValue(payment.getEmployee().getFullName());
        employeeField.setReadOnly(true);

        TextField roleField = new TextField("Role");
        roleField.setValue(payment.getEmployee().getRole().toString());
        roleField.setReadOnly(true);

        TextField grossIncomeField = new TextField("Gross Income");
        grossIncomeField.setValue("R$ " + payment.getGrossIncome().toString());
        grossIncomeField.setReadOnly(true);

        TextField netIncomeField = new TextField("Net Income");
        netIncomeField.setValue("R$ " + calculateNetIncome(payment).toString());
        netIncomeField.setReadOnly(true);

        TextField totalIncomeField = new TextField("Total Income");
        totalIncomeField.setValue("R$ " + calculateTotalIncome(payment).toString());
        totalIncomeField.setReadOnly(true);

        TextField createdAtField = new TextField("Created At");
        createdAtField.setValue(payment.getCreatedAt().toString());
        createdAtField.setReadOnly(true);

        TextField statusField = new TextField("Status");
        statusField.setValue(payment.isActive() ? "Active" : "Inactive");
        statusField.setReadOnly(true);

        paymentInfoLayout.add(idField, employeeField, roleField, grossIncomeField,
                netIncomeField, totalIncomeField, createdAtField, statusField);

        // Benefits breakdown
        VerticalLayout benefitsLayout = new VerticalLayout();
        benefitsLayout.add(new com.vaadin.flow.component.html.H4("Benefits Breakdown"));

        FormLayout benefitsForm = new FormLayout();
        benefitsForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        TextField taxesField = new TextField("Taxes");
        taxesField.setValue("R$ " + payment.getAmountInTaxes().toString());
        taxesField.setReadOnly(true);

        TextField mealField = new TextField("Meal Voucher");
        mealField.setValue("R$ " + payment.getMealVoucherAmount().toString());
        mealField.setReadOnly(true);

        TextField foodField = new TextField("Food Voucher");
        foodField.setValue("R$ " + payment.getFoodVoucherAmount().toString());
        foodField.setReadOnly(true);

        TextField healthField = new TextField("Health Insurance");
        healthField.setValue("R$ " + payment.getHealthInsuranceAmount().toString());
        healthField.setReadOnly(true);

        TextField dentalField = new TextField("Dental Insurance");
        dentalField.setValue("R$ " + payment.getDentalInsuranceAmount().toString());
        dentalField.setReadOnly(true);

        TextField profitField = new TextField("Profit Sharing");
        profitField.setValue("R$ " + payment.getProfitSharingAmount().toString());
        profitField.setReadOnly(true);

        benefitsForm.add(taxesField, mealField, foodField, healthField, dentalField, profitField);
        benefitsLayout.add(benefitsForm);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();

        Button editButton = new Button("Edit Payment", new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.setEnabled(payment.isActive()); // Bloqueia o botão se inativo
        editButton.addClickListener(e -> {
            detailsDialog.close();
            openEditDialog(payment);
        });

        Button closeButton = new Button("Close");
        closeButton.addClickListener(e -> detailsDialog.close());

        buttonLayout.add(editButton, closeButton);

        mainLayout.add(
                new com.vaadin.flow.component.html.H4("Payment Information"),
                paymentInfoLayout,
                benefitsLayout,
                buttonLayout);

        detailsDialog.add(mainLayout);
        detailsDialog.open();
    }

    private void refreshGrid() {
        List<Payment> payments = paymentService.listAll();
        List<Payment> filtered = payments.stream()
                .filter(this::matchesFilter)
                .toList();
        grid.setItems(filtered);
    }

    private boolean matchesFilter(Payment payment) {
        if (currentSearchTerm.isEmpty())
            return true;

        String id = payment.getId().toString().toLowerCase();
        String employeeName = payment.getEmployee().getFullName().toLowerCase();
        String role = payment.getEmployee().getRole().toString().toLowerCase();

        return id.contains(currentSearchTerm) ||
                employeeName.contains(currentSearchTerm) ||
                role.contains(currentSearchTerm);
    }
}