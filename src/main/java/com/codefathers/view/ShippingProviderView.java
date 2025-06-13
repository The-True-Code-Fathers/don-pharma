package com.codefathers.view;

import com.codefathers.model.dto.CreateShippingProviderDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.service.ShippingProviderService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Route("shipping-providers")
public class ShippingProviderView extends VerticalLayout {

    private final ShippingProviderService service;
    private final Grid<ShippingProvider> grid = new Grid<>(ShippingProvider.class, false);

    public ShippingProviderView() {
        this.service = new ShippingProviderService(new ShippingProviderRepositoryImpl());

        Button newButton = new Button("Nova Transportadora", e -> openFormDialog(null));

        setupGrid();
        add(newButton, grid);
        updateGrid();
    }

    private void setupGrid() {
        grid.removeAllColumns();
        grid.addColumn(ShippingProvider::getName).setHeader("Nome").setAutoWidth(true);
        grid.addColumn(ShippingProvider::getCnpj).setHeader("CNPJ").setAutoWidth(true);
        grid.addColumn(sp -> sp.getBasePrice().toString()).setHeader("Preço Base").setAutoWidth(true);
        grid.addColumn(sp -> sp.getDailyCapacity().toString()).setHeader("Capacidade Diária").setAutoWidth(true);

        // Nova coluna para mostrar os estados atendidos
        grid.addColumn(sp -> {
            if (sp.getShippingAreas() == null || sp.getShippingAreas().isEmpty()) {
                return "Nenhum";
            }
            return sp.getShippingAreas().stream()
                    .flatMap(area -> Arrays.stream(area.getStates()))
                    .distinct()
                    .collect(Collectors.joining(", "));
        }).setHeader("Estados Atendidos").setAutoWidth(true);

        grid.setId("custom-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // Restante do método permanece igual...
        grid.addItemDoubleClickListener(event -> {
            try {
                ShippingProvider item = event.getItem();
                if (item != null) {
                    openFormDialog(item);
                } else {
                    Notification.show("Selecione um item válido", 3000, Notification.Position.MIDDLE);
                }
            } catch (Exception e) {
                Notification.show("Erro ao abrir editor: " + e.getMessage(),
                        5000, Notification.Position.MIDDLE);
                e.printStackTrace();
            }
        });

        grid.setHeight("300px");
        grid.setWidthFull();
        grid.getStyle().set("margin-top", "10px");
    }

    private void openFormDialog(ShippingProvider provider) {
        Dialog dialog = new Dialog();
        dialog.setWidth("480px"); // Largura mais compacta

        // Campos
        TextField nameField = new TextField("Nome");
        nameField.setPlaceholder("Digite o nome...");
        nameField.setWidthFull();

        TextField cnpjField = new TextField("CNPJ");
        cnpjField.setPlaceholder("Apenas números");
        cnpjField.setWidthFull();

        TextField basePriceField = new TextField("Preço Base");
        basePriceField.setPlaceholder("Ex: 199.90");
        basePriceField.setWidthFull();

        IntegerField dailyCapacityField = new IntegerField("Capacidade Diária");
        dailyCapacityField.setPlaceholder("Ex: 50");
        dailyCapacityField.setWidthFull();

        TextArea areasField = new TextArea("Áreas de Atendimento");
        areasField.setPlaceholder("Ex: Norte: AM, PA; Sul: RS, SC");
        areasField.setWidthFull();

        // Preenche os campos se for edição
        if (provider != null) {
            nameField.setValue(provider.getName());
            cnpjField.setValue(provider.getCnpj());
            basePriceField.setValue(provider.getBasePrice().toString());
            dailyCapacityField.setValue(provider.getDailyCapacity().intValue());

            StringBuilder sb = new StringBuilder();
            for (ShippingArea area : provider.getShippingAreas()) {
                sb.append(area.getDescription()).append(": ")
                        .append(String.join(", ", area.getStates())).append("; ");
            }
            areasField.setValue(sb.toString().trim());
        }

        // Botões
        Button saveButton = new Button(provider == null ? "Cadastrar" : "Atualizar", e -> {
            try {
                CreateShippingProviderDTO dto = buildDTO(
                        nameField.getValue(),
                        cnpjField.getValue(),
                        basePriceField.getValue(),
                        dailyCapacityField.getValue(),
                        areasField.getValue()
                );

                if (provider != null) {
                    dto.setId(provider.getId());
                    service.updateShippingProvider(dto);
                    Notification.show("Atualizado com sucesso!");
                } else {
                    service.registerShippingProvider(dto);
                    Notification.show("Cadastrado com sucesso!");
                }

                updateGrid();
                dialog.close();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        Button deleteButton = new Button("Deletar", e -> {
            try {
                service.removeShippingProvider(provider.getId());
                Notification.show("Removido com sucesso!");
                updateGrid();
                dialog.close();
            } catch (Exception ex) {
                showError(ex);
            }
        });
        deleteButton.setVisible(provider != null);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        // Layout final
        VerticalLayout formLayout = new VerticalLayout(
                nameField,
                cnpjField,
                basePriceField,
                dailyCapacityField,
                areasField,
                new HorizontalLayout(saveButton, deleteButton, cancelButton)
        );
        formLayout.setPadding(false);
        formLayout.setSpacing(true);
        formLayout.setWidthFull();

        dialog.add(formLayout);
        dialog.open();
    }



    private CreateShippingProviderDTO buildDTO(String name, String cnpj, String priceStr, Integer capacity, String areasText) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }

        if (cnpj == null || cnpj.isBlank()) {
            throw new IllegalArgumentException("CNPJ é obrigatório.");
        }

        if (cnpj.length() != 14 || !cnpj.matches("\\d+")) {
            throw new IllegalArgumentException("CNPJ deve conter 14 dígitos numéricos.");
        }

        BigDecimal basePrice;
        try {
            basePrice = new BigDecimal(priceStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Preço Base inválido.");
        }

        BigDecimal dailyCapacity;
        try {
            dailyCapacity = capacity != null ? new BigDecimal(capacity) : BigDecimal.ZERO;
            if (dailyCapacity.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Capacidade Diária deve ser positiva.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Capacidade Diária inválida.");
        }

        return CreateShippingProviderDTO.builder()
                .name(name)
                .cnpj(cnpj)
                .basePrice(basePrice)
                .dailyCapacity(dailyCapacity)
                .shippingAreas(parseAreas(areasText))
                .build();
    }

    private List<ShippingArea> parseAreas(String input) {
        List<ShippingArea> areas = new ArrayList<>();
        if (input == null || input.isBlank()) return areas;

        Arrays.stream(input.split(";"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .forEach(item -> {
                    String[] parts = item.split(":", 2);
                    if (parts.length == 2) {
                        ShippingArea area = new ShippingArea();
                        area.setDescription(parts[0].trim());
                        // Convertendo a string de estados para array
                        String[] states = Arrays.stream(parts[1].trim().split(","))
                                .map(String::trim)
                                .toArray(String[]::new);
                        area.setStates(states);
                        areas.add(area);
                    }
                });

        return areas;
    }

    private void showError(Exception e) {
        Notification.show("Erro: " + e.getMessage(), 4000, Notification.Position.MIDDLE);
        e.printStackTrace();
    }

    private void updateGrid() {
        grid.setItems(service.listAllShippingProviders());
    }
}