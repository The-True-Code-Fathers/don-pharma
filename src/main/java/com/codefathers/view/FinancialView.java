package com.codefathers.view;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate; // Importar LocalDate
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.codefathers.repository.implementations.OrderItemRepositoryImpl;
import com.codefathers.repository.implementations.OrderRepositoryImpl;
import com.codefathers.repository.implementations.PaymentRepositoryImpl;
import com.codefathers.repository.implementations.PurchaseOrderItemRepositoryImpl;
import com.codefathers.repository.implementations.ShippingOrderRepositoryImpl;
import com.codefathers.service.FinancialService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button; // Importar Button
import com.vaadin.flow.component.datepicker.DatePicker; // Importar DatePicker
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;

@PageTitle("Financial")
@Route("financial")
@Slf4j
public class FinancialView extends VerticalLayout {

    private final FinancialService financialService;
    private final NumberFormat currencyFormatter;

    // Componentes da UI
    private DatePicker startDatePicker; // Novo: Seletor de data de início
    private DatePicker endDatePicker;   // Novo: Seletor de data de fim
    private Button filterButton;        // Novo: Botão para aplicar o filtro
    private Div summaryCards;
    private Grid<CategoryData> inflowsGrid;
    private Grid<CategoryData> outflowsGrid;
    private HorizontalLayout detailsLayout;

    public FinancialView() {
        this.financialService = new FinancialService(new PaymentRepositoryImpl(), new PurchaseOrderItemRepositoryImpl(),
                new OrderItemRepositoryImpl(), new ShippingOrderRepositoryImpl(), new OrderRepositoryImpl());
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        setupLayout();
        createHeader();
        createDateFilters(); // Novo: Adiciona os seletores de data e botão
        createSummaryCards();
        createTransactionDetailsGrids();

        // Inicializa com dados dos últimos 30 dias, por exemplo
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
        refreshData(); // Chama refreshData inicialmente com as datas padrão
    }

    private void setupLayout() {
        setSizeFull();
        getStyle().set("padding", "var(--lumo-space-m)");
        setSpacing(false);
        addClassName("financial-view");
    }

    private void createHeader() {
        H1 title = new H1("Financial Report");
        title.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.Margin.Bottom.MEDIUM);
        
        Icon moneyIcon = VaadinIcon.MONEY_DEPOSIT.create();
        moneyIcon.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.Margin.Right.SMALL);
        moneyIcon.setSize("30px");

        HorizontalLayout header = new HorizontalLayout(moneyIcon, title);
        header.setAlignItems(Alignment.CENTER);
        
        add(header);
    }

    // Novo método para criar os componentes de filtro de data
    private void createDateFilters() {
        startDatePicker = new DatePicker("Start date");
        startDatePicker.setLocale(new Locale("pt", "BR")); // Localização para exibir corretamente as datas
        startDatePicker.setPlaceholder("Select start date");

        endDatePicker = new DatePicker("End date");
        endDatePicker.setLocale(new Locale("pt", "BR")); // Localização para exibir corretamente as datas
        endDatePicker.setPlaceholder("Select end date");

        filterButton = new Button("Filter");
        filterButton.setIcon(VaadinIcon.FILTER.create());
        filterButton.addClickListener(event -> refreshData()); // Adiciona o listener para atualizar os dados ao clicar

        HorizontalLayout dateFilterLayout = new HorizontalLayout(startDatePicker, endDatePicker, filterButton);
        dateFilterLayout.setAlignItems(Alignment.BASELINE); // Alinha os componentes pela base
        dateFilterLayout.addClassNames(LumoUtility.Gap.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM); // Espaçamento e margem
        add(dateFilterLayout);
    }

    private void createSummaryCards() {
        summaryCards = new Div();
        summaryCards.addClassNames("summary-cards", LumoUtility.Display.FLEX, LumoUtility.Gap.MEDIUM, LumoUtility.Margin.Bottom.LARGE, LumoUtility.Flex.AUTO);
        add(summaryCards);
    }

    private void createTransactionDetailsGrids() {
        detailsLayout = new HorizontalLayout();
        detailsLayout.setSizeFull();
        detailsLayout.addClassNames(LumoUtility.Gap.LARGE);

        inflowsGrid = createDetailsGrid("MONEY INFLOWS", "success");
        outflowsGrid = createDetailsGrid("MONEY OUTFLOWS", "error");

        detailsLayout.add(inflowsGrid, outflowsGrid);
        add(detailsLayout);
    }

    private <T> Grid<T> createDetailsGrid(String header, String theme) {
        Grid<T> grid = new Grid<>();
        grid.addThemeName("compact");
        grid.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "var(--lumo-border-radius-l)");

        H3 gridHeader = new H3(header);
        gridHeader.getStyle()
            .set("margin", "var(--lumo-space-m)")
            .set("margin-bottom", "var(--lumo-space-s)")
            .set("font-size", "var(--lumo-font-size-l)");
        
        if ("success".equals(theme)) {
            gridHeader.addClassName(LumoUtility.TextColor.SUCCESS);
            grid.getStyle().set("border-top", "4px solid var(--lumo-success-color-50pct)");
        } else if ("error".equals(theme)) {
            gridHeader.addClassName(LumoUtility.TextColor.ERROR);
            grid.getStyle().set("border-top", "4px solid var(--lumo-error-color-50pct)");
        }
        
        VerticalLayout gridLayout = new VerticalLayout(gridHeader, grid);
        gridLayout.setSpacing(false);
        gridLayout.setPadding(false);
        gridLayout.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "var(--lumo-border-radius-l)");

        // O grid em si precisa ser adicionado ao layout, e o layout é retornado.
        // A referência ao grid é mantida pela variável de instância (inflowsGrid/outflowsGrid).
        // Isso permite configurar as colunas e os itens diretamente no grid.
        return grid; // Retorna o grid para que as colunas possam ser configuradas
    }


    private void refreshData() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        log.debug("Chart refresh requested");

        log.debug("start date: {}",  startDate);
        log.debug("end date: {}",  endDate);

        // Validação básica das datas
        if (startDate == null || endDate == null) {
            // Poderia mostrar uma notificação ao usuário ou usar um período padrão
            System.out.println("Por favor, selecione as datas inicial e final.");
            return;
        }

        if (startDate.isAfter(endDate)) {
            // Poderia mostrar uma notificação ao usuário
            System.out.println("A data inicial não pode ser posterior à data final.");
            return;
        }

        // Chame o método calculateFinancialData do FinancialService com as datas
        financialService.calculateFinancialData(startDate, endDate);

        BigDecimal totalInflows = financialService.getTotalInflows();
        BigDecimal totalOutflows = financialService.getTotalOutflows();
        BigDecimal netCashFlow = financialService.getCashFlow(); // Assuming cashFlow is updated after calculations

        log.debug("Total inflows: {}", totalInflows);
        log.debug("Total cashflows: {}", totalOutflows);
        log.debug("Net cash: {}", netCashFlow);

        updateSummaryCards(totalInflows, totalOutflows, netCashFlow);
        updateTransactionDetailsGrids(); // Não precisa mais passar os totais aqui, eles já estão no service
    }

    private void updateSummaryCards(BigDecimal totalInflows, BigDecimal totalOutflows, BigDecimal netCashFlow) {
        summaryCards.removeAll();

        Component inflowCard = createSummaryCard("Total Inflows", formatCurrency(totalInflows), VaadinIcon.ARROW_CIRCLE_UP_O, "success");
        Component outflowCard = createSummaryCard("Total Outflows", formatCurrency(totalOutflows), VaadinIcon.ARROW_CIRCLE_DOWN_O, "error");

        boolean isPositive = netCashFlow.compareTo(BigDecimal.ZERO) >= 0;
        VaadinIcon cashFlowIcon = isPositive ? VaadinIcon.TRENDING_UP : VaadinIcon.TRENDING_DOWN;
        String cashFlowTheme = isPositive ? "success" : "error";
        Component cashFlowCard = createSummaryCard("Cash flow", formatCurrency(netCashFlow), cashFlowIcon, cashFlowTheme);

        summaryCards.add(inflowCard, outflowCard, cashFlowCard);
    }

    private void updateTransactionDetailsGrids() {
        // --- Grid de Entradas ---
        inflowsGrid.setItems(List.of(
                new CategoryData("Product Sales", financialService.getTotalInflows()) // Obtém o valor do service
        ));
        
        // --- Grid de Saídas ---
        List<CategoryData> outflowData = new ArrayList<>();
        outflowData.add(new CategoryData("Miscellaneous Payments", financialService.getNetPayment()));
        outflowData.add(new CategoryData("Product Purchases", financialService.getPurchaseTotal()));
        outflowData.add(new CategoryData("Shipping Costs", financialService.getShippingTotal()));
        outflowsGrid.setItems(outflowData);
    }

    private Component createSummaryCard(String title, String value, VaadinIcon icon, String theme) {
        Div card = new Div();
        card.addClassNames(
            "summary-card", LumoUtility.Background.BASE, LumoUtility.Border.ALL, 
            LumoUtility.BorderRadius.LARGE, LumoUtility.Padding.MEDIUM,
            LumoUtility.BoxShadow.SMALL
        );
        card.getStyle().set("flex-grow", "1").set("min-width", "220px");
        
        String borderColorClass = "success".equals(theme) ? LumoUtility.BorderColor.SUCCESS : LumoUtility.BorderColor.ERROR;
        card.addClassName(borderColorClass);
        card.getStyle().set("border-left-width", "4px");

        Icon cardIcon = icon.create();
        cardIcon.addClassName("success".equals(theme) ? LumoUtility.TextColor.SUCCESS : LumoUtility.TextColor.ERROR);
        cardIcon.setSize("28px");

        Span titleSpan = new Span(title);
        titleSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.SEMIBOLD, LumoUtility.TextColor.BODY);

        VerticalLayout textContent = new VerticalLayout(titleSpan, valueSpan);
        textContent.setSpacing(false);
        textContent.setPadding(false);

        HorizontalLayout cardLayout = new HorizontalLayout(cardIcon, textContent);
        cardLayout.setAlignItems(Alignment.CENTER);
        cardLayout.setSpacing(true);

        card.add(cardLayout);
        return card;
    }
    
    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        
        if (inflowsGrid.getColumns().isEmpty()) {
            configureGridColumns(inflowsGrid);
        }
        if (outflowsGrid.getColumns().isEmpty()) {
            configureGridColumns(outflowsGrid);
        }
    }

    private void configureGridColumns(Grid<CategoryData> grid) {
        grid.addColumn(CategoryData::getName).setHeader("Categoria").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(data -> formatCurrency(data.getAmount())).setHeader("Valor").setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
    }

    private String formatCurrency(BigDecimal amount) {
        return currencyFormatter.format(amount != null ? amount : BigDecimal.ZERO);
    }

    public static class CategoryData {
        private final String name;
        private final BigDecimal amount;

        public CategoryData(String name, BigDecimal amount) {
            this.name = name;
            this.amount = amount;
        }

        public String getName() { return name; }
        public BigDecimal getAmount() { return amount; }
    }
}