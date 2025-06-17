package com.codefathers.view;

import com.codefathers.model.dto.PlanoVendedorDTO;
import com.codefathers.model.dto.ProdutoSugeridoDTO;
import com.codefathers.model.entity.Product;
import com.codefathers.repository.implementations.*;
import com.codefathers.service.MetaPorVendedorService;
import com.codefathers.service.OrderItemService;
import com.codefathers.service.ProductService;
import com.codefathers.service.PurchaseOrderItemService;
import com.codefathers.util.AverageProductPriceUtil;
import com.codefathers.util.ValidatorUtil;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Goals")
@Route("goals")
@Slf4j
public class GoalsView extends VerticalLayout {

    private MetaPorVendedorService metaPorVendedorService;

    private TextField createGoal = new TextField("", "Goal");
    private TextField createSeller = new TextField("", "Seller");

    private TextField searchField = new TextField();
    private String currentSearchTerm = "";

    private Button createRunService = new Button("Run Goals");
    private Button createCloseButton = new Button("Close");

    private TextField getName = new TextField("Name");
    private TextField getSales = new TextField("Total Sales");
    private TextArea getGoal = new TextArea("Goal");

    private PlanoVendedorDTO currentPlan;

    private Grid<PlanoVendedorDTO> grid = new Grid<>(PlanoVendedorDTO.class, false);
    private Grid<ProdutoSugeridoDTO> grid2 = new Grid<>(ProdutoSugeridoDTO.class, false);
    private List<PlanoVendedorDTO> cachePlanoVendedor = new ArrayList<>();

    private GridLazyDataView<MetaPorVendedorService> dataView;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Dialog createDialog = new Dialog();


    public GoalsView() {

        this.metaPorVendedorService = new MetaPorVendedorService();

        grid = new Grid<>(PlanoVendedorDTO.class, false);
        setupGrid();

        add(grid);

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        setupSearchField();
        setupGrid();
        setupGrid2();
        setupCreateDialog();
        setupEventListeners();

        HorizontalLayout layout = new HorizontalLayout(createRunService, createSeller, createGoal, searchField);
        layout.setAlignItems(Alignment.CENTER);
        layout.setSpacing(true);

        HorizontalLayout headerLayout = new HorizontalLayout(layout);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setWidth("100%");

        add(headerLayout, grid);
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

    private void populateUpdateForm(PlanoVendedorDTO plan) {
        currentPlan = plan;
        getName.setValue(plan.getVendedorNome());
        getName.setReadOnly(true);
        getSales.setValue(plan.getTotalVendido().toString());
        getSales.setReadOnly(true);
        getGoal.setValue(plan.getMeta().toString());
        getGoal.setReadOnly(true);
    }

    private void setupSearchField() {
        searchField.setWidth("400px");
        searchField.setPlaceholder("Search Name");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setClearButtonVisible(true);

        searchField.addValueChangeListener(e -> {
            currentSearchTerm = e.getValue().trim();
            dataView.refreshAll();
        });
    }

    private void setupCreateDialog() {
        createDialog.setHeaderTitle("Goal");
        createDialog.setDraggable(true);
        createDialog.setWidth("600px");
        createDialog.setHeight("100%");

        getName.setWidthFull();
        getSales.setWidthFull();
        getGoal.setWidthFull();

        VerticalLayout formLayout = new VerticalLayout(getName, getSales, getGoal);
        formLayout.setSpacing(true);
        formLayout.setPadding(false);
        formLayout.setMargin(false);
        formLayout.setWidthFull();


        HorizontalLayout gridLayout = new HorizontalLayout(grid2);
        gridLayout.setWidthFull();
        gridLayout.setJustifyContentMode(JustifyContentMode.START);

        VerticalLayout dialogContent = new VerticalLayout(formLayout, gridLayout, createCloseButton);
        dialogContent.setPadding(false);
        dialogContent.setMargin(false);
        dialogContent.setSpacing(false);
        dialogContent.setSizeUndefined();

        createDialog.removeAll();
        createDialog.add(dialogContent);

        createDialog.add(new VerticalLayout(formLayout, gridLayout, createCloseButton));
    }

    private void setupGrid() {

        grid.addColumn(PlanoVendedorDTO::getVendedorNome)
                .setHeader("Vendedor")
                .setSortable(true)
                .setFlexGrow(1);
        grid.addColumn(plano -> plano.getTotalVendido() != null
                        ? plano.getTotalVendido().toString()
                        : "0.00")
                .setHeader("Total Vendido")
                .setSortable(true)
                .setFlexGrow(1);
        grid.addColumn(plano -> plano.getMeta() != null
                        ? plano.getMeta().toString()
                        : "0.00")
                .setHeader("Meta")
                .setSortable(true)
                .setFlexGrow(1);
        grid.addColumn(plano -> "Produtos Sugeridos")
                .setHeader("Ações")
                .setSortable(false)
                .setFlexGrow(1);

        grid.addColumn(data -> {
            return data.getCreatedAt() != null ? data.getCreatedAt().format(dateFormatter) : "";
        }).setHeader("Created At").setSortable(true).setFlexGrow(1);


        grid.addItemClickListener(event -> {
            if (event.getClickCount() == 2) {
                currentPlan = event.getItem();
                populateUpdateForm(currentPlan);

                List<ProdutoSugeridoDTO> produtos = currentPlan.getProdutosSugeridos();
                grid2.setItems(produtos);

                createDialog.open();
            }
        });


        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setWidth("100%");
        grid.setHeightFull();
        if (cachePlanoVendedor != null) {
            grid.setItems(cachePlanoVendedor);
        }
        refreshGrid();
    }

    private void setupGrid2() {

        grid2.addColumn(ProdutoSugeridoDTO::getSku).setHeader("SKU").setSortable(true).setFlexGrow(1);
        grid2.addColumn(ProdutoSugeridoDTO::getNome).setHeader("Total Sales").setSortable(true).setFlexGrow(2);
        grid2.addColumn(ProdutoSugeridoDTO::getQuantidadeSugerida).setHeader("Goal").setSortable(true).setFlexGrow(3);

        grid2.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        VerticalLayout dialogLayout = new VerticalLayout(grid2);
        dialogLayout.setSizeFull();
        dialogLayout.setPadding(false);
        dialogLayout.setMargin(false);
        dialogLayout.setSpacing(false);
        createDialog.add(dialogLayout);

    }

    private boolean matchesCurrentFilters(PlanoVendedorDTO plano) {

        String searchTermLower = currentSearchTerm.toLowerCase();
        return matchesTerm(plano.getVendedorNome(), searchTermLower);

    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value != null && !value.isBlank() && value.toLowerCase().contains(searchTerm);
    }

    private void setupEventListeners() {
        createRunService.addClickListener(e -> {
            MetaPorVendedorService metaPorVendedorService = new MetaPorVendedorService();
            List<PlanoVendedorDTO> planos = metaPorVendedorService.calcularPlanoPorVendedor(
                    new BigDecimal(createGoal.getValue())
            );

            cachePlanoVendedor = planos;
            grid.setItems(planos);
        });

        createCloseButton.addClickListener(e -> createDialog.close());

    }

}
