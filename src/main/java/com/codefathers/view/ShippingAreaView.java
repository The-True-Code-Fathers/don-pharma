package com.codefathers.view;

import com.codefathers.model.dto.CreateShippingAreaDTO;
import com.codefathers.model.entity.ShippingArea;
import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.repository.implementations.ShippingAreaRepositoryImpl;
import com.codefathers.repository.implementations.ShippingProviderRepositoryImpl;
import com.codefathers.service.ShippingAreaService;
import com.codefathers.service.ShippingProviderService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.validation.ConstraintViolationException;

import java.util.List;
import java.util.UUID;

@Route("shipping-area")
public class ShippingAreaView extends VerticalLayout {
    private ShippingAreaService shippingAreaService;
    private ShippingProviderService shippingProviderService;

    private final ComboBox<ShippingProvider> providerComboBox = new ComboBox<>("Provedor de Entrega");
    private final TextArea descriptionField = new TextArea("Descrição");
    private final TextField statesField = new TextField("Estados Atendidos");
    private final TextField idField = new TextField("Buscar/Remover por ID");

    private final Grid<ShippingArea> areaGrid = new Grid<>(ShippingArea.class, false);


    public ShippingAreaView() {
        try {
            // Inicializa repositórios e serviços
            var areaRepository = new ShippingAreaRepositoryImpl();
            var providerRepository = new ShippingProviderRepositoryImpl();
            var validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();

            this.shippingProviderService = new ShippingProviderService(providerRepository);
            this.shippingAreaService = new ShippingAreaService(areaRepository, validator);

            // Configura componentes
            configureComboBox();
            configureForm();
            configureGrid();

            // Adiciona componentes ao layout
            add(createFormLayout(), areaGrid);
            atualizarGrid();

            // Estilo
            setSizeFull();
            setPadding(true);
            setSpacing(true);

        } catch (Exception e) {
            Notification.show("Erro ao inicializar: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            e.printStackTrace();
        }
    }

    private void configureComboBox() {
        try {
            List<ShippingProvider> providers = shippingProviderService.listAllShippingProviders();
            providerComboBox.setItems(providers);
            providerComboBox.setItemLabelGenerator(ShippingProvider::getName);
            providerComboBox.setPlaceholder("Selecione um provedor");
            providerComboBox.setClearButtonVisible(true);

            if (providers.isEmpty()) {
                Notification.show("Nenhum provedor cadastrado.", 3000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            Notification.show("Erro ao carregar provedores: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void configureForm() {
        statesField.setPlaceholder("Ex: SP, RJ, MG");
        descriptionField.setPlaceholder("Descrição opcional");
        statesField.setClearButtonVisible(true);
        descriptionField.setClearButtonVisible(true);
        idField.setClearButtonVisible(true);
    }

    private VerticalLayout createFormLayout() {
        Button salvarBtn = new Button("Cadastrar", event -> cadastrarAreaEntrega());
        Button listarBtn = new Button("Listar", event -> atualizarGrid());
        Button buscarBtn = new Button("Buscar por ID", event -> buscarPorId());
        Button removerBtn = new Button("Remover por ID", event -> removerPorId());

        HorizontalLayout buttons = new HorizontalLayout(salvarBtn, listarBtn, buscarBtn, removerBtn);
        buttons.setSpacing(true);

        VerticalLayout formLayout = new VerticalLayout(
                providerComboBox,
                descriptionField,
                statesField,
                idField,
                buttons
        );
        formLayout.setSpacing(true);
        formLayout.setPadding(false);

        return formLayout;
    }

    private void cadastrarAreaEntrega() {
        try {
            ShippingProvider provider = providerComboBox.getValue();
            if (provider == null) {
                Notification.show("Selecione um provedor de entrega.", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (statesField.isEmpty()) {
                Notification.show("Informe os estados atendidos.", 3000, Notification.Position.MIDDLE);
                return;
            }

            CreateShippingAreaDTO dto = new CreateShippingAreaDTO();
            dto.setShippingProvider(provider);
            dto.setDescription(descriptionField.getValue());
            dto.setStates(statesField.getValue());

            shippingAreaService.saveShippingArea(dto);
            Notification.show("Área cadastrada com sucesso!", 3000, Notification.Position.MIDDLE);
            limparCampos();
            atualizarGrid();

        } catch (ConstraintViolationException e) {
            e.getConstraintViolations().forEach(v ->
                    Notification.show("Erro de validação: " + v.getMessage(), 5000, Notification.Position.MIDDLE));
        } catch (Exception e) {
            Notification.show("Erro ao cadastrar: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void atualizarGrid() {
        try {
            List<ShippingArea> areas = shippingAreaService.findAllShippingAreas();
            if (areas.isEmpty()) {
                Notification.show("Nenhuma área de entrega cadastrada.", 3000, Notification.Position.MIDDLE);
            }
            areaGrid.setItems(areas);
        } catch (Exception e) {
            Notification.show("Erro ao listar áreas: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void buscarPorId() {
        try {
            if (idField.isEmpty()) {
                Notification.show("Informe o ID para buscar.", 3000, Notification.Position.MIDDLE);
                return;
            }

            UUID id = UUID.fromString(idField.getValue());
            ShippingArea area = shippingAreaService.findShippingAreaById(id);

            if (area != null) {
                areaGrid.setItems(List.of(area)); // Corrige: precisa ser uma lista
            } else {
                Notification.show("Área não encontrada.", 3000, Notification.Position.MIDDLE);
                areaGrid.setItems(List.of()); // Limpa a grid
            }
        } catch (IllegalArgumentException e) {
            Notification.show("ID inválido.", 3000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Erro na busca: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }


    private void removerPorId() {
        try {
            if (idField.isEmpty()) {
                Notification.show("Informe o ID para remover.", 3000, Notification.Position.MIDDLE);
                return;
            }

            UUID id = UUID.fromString(idField.getValue());
            shippingAreaService.deleteShippingAreaById(id);
            Notification.show("Área removida com sucesso.", 3000, Notification.Position.MIDDLE);
            atualizarGrid();
            idField.clear();

        } catch (IllegalArgumentException e) {
            Notification.show("ID inválido.", 3000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Erro ao remover: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void configureGrid() {
        areaGrid.removeAllColumns();
        areaGrid.setWidthFull();
        areaGrid.setHeight("300px");

        areaGrid.addColumn(area -> area.getShippingProvider() != null ? area.getShippingProvider().getName() : "N/A")
                .setHeader("Provedor").setAutoWidth(true);

        areaGrid.addColumn(ShippingArea::getDescription)
                .setHeader("Descrição").setAutoWidth(true);

        areaGrid.addColumn(ShippingArea::getStates)
                .setHeader("Estados Atendidos").setAutoWidth(true);
    }


    private void limparCampos() {
        descriptionField.clear();
        statesField.clear();
    }
}