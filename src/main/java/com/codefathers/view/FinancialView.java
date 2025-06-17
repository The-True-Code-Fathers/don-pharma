package com.codefathers.view;

import java.math.BigDecimal;
import java.text.NumberFormat;
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

@PageTitle("Financial")
@Route("financial")
public class FinancialView extends VerticalLayout {

    private final FinancialService financialService;
    private final NumberFormat currencyFormatter;

    // Componentes da UI
    private Div summaryCards;
    private Grid<CategoryData> inflowsGrid;
    private Grid<CategoryData> outflowsGrid;
    private HorizontalLayout detailsLayout; // CHANGED: Usando HorizontalLayout para os grids

    public FinancialView() {
        this.financialService = new FinancialService(new PaymentRepositoryImpl(), new PurchaseOrderItemRepositoryImpl(),
                new OrderItemRepositoryImpl(), new ShippingOrderRepositoryImpl(), new OrderRepositoryImpl());
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        setupLayout();
        createHeader();
        createSummaryCards();
        createTransactionDetailsGrids(); // CHANGED: Novo método para criar os grids

        refreshData();
    }

    private void setupLayout() {
        setSizeFull();
        // CHANGED: Reduzindo padding e espaçamento geral
        getStyle().set("padding", "var(--lumo-space-m)");
        setSpacing(false);
        addClassName("financial-view");
    }

    private void createHeader() {
        H1 title = new H1("Relatório Financeiro");
        title.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.Margin.Bottom.MEDIUM);
        
        Icon moneyIcon = VaadinIcon.MONEY_DEPOSIT.create();
        moneyIcon.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.Margin.Right.SMALL);
        moneyIcon.setSize("30px");

        HorizontalLayout header = new HorizontalLayout(moneyIcon, title);
        header.setAlignItems(Alignment.CENTER);
        
        add(header);
    }

    private void createSummaryCards() {
        summaryCards = new Div();
        summaryCards.addClassNames("summary-cards", LumoUtility.Display.FLEX, LumoUtility.Gap.MEDIUM, LumoUtility.Margin.Bottom.LARGE, LumoUtility.Flex.AUTO);
        add(summaryCards);
    }

    // CHANGED: Método renomeado e simplificado
    private void createTransactionDetailsGrids() {
        detailsLayout = new HorizontalLayout();
        detailsLayout.setSizeFull();
        detailsLayout.addClassNames(LumoUtility.Gap.LARGE); // Espaço entre os grids

        // Grid de Entradas
        inflowsGrid = createDetailsGrid("ENTRADAS DE DINHEIRO", "success");

        // Grid de Saídas
        outflowsGrid = createDetailsGrid("SAÍDAS DE DINHEIRO", "error");

        detailsLayout.add(inflowsGrid, outflowsGrid);
        add(detailsLayout);
    }

    // NEW METHOD: Cria e estiliza um grid para detalhes
    private <T> Grid<T> createDetailsGrid(String header, String theme) {
        Grid<T> grid = new Grid<>();
        grid.addThemeName("compact"); // Tema de grid mais denso
        grid.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "var(--lumo-border-radius-l)");

        // Adiciona um cabeçalho customizado ao grid
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

        // Como o grid está dentro de um layout, retornamos o layout
        // Para adicionar colunas e itens, você ainda usará a variável de instância `inflowsGrid` e `outflowsGrid`
        return grid;
    }


    private void refreshData() {
        BigDecimal totalInflows = financialService.getTotalInflows();
        BigDecimal totalOutflows = financialService.getTotalOutflows();
        BigDecimal netCashFlow = financialService.getCashFlow();

        updateSummaryCards(totalInflows, totalOutflows, netCashFlow);
        updateTransactionDetailsGrids(totalInflows, totalOutflows);
    }

    private void updateSummaryCards(BigDecimal totalInflows, BigDecimal totalOutflows, BigDecimal netCashFlow) {
        summaryCards.removeAll();

        Component inflowCard = createSummaryCard("Total Entradas", formatCurrency(totalInflows), VaadinIcon.ARROW_CIRCLE_UP_O, "success");
        Component outflowCard = createSummaryCard("Total Saídas", formatCurrency(totalOutflows), VaadinIcon.ARROW_CIRCLE_DOWN_O, "error");

        boolean isPositive = netCashFlow.compareTo(BigDecimal.ZERO) >= 0;
        VaadinIcon cashFlowIcon = isPositive ? VaadinIcon.TRENDING_UP : VaadinIcon.TRENDING_DOWN;
        String cashFlowTheme = isPositive ? "success" : "error";
        Component cashFlowCard = createSummaryCard("Fluxo de Caixa", formatCurrency(netCashFlow), cashFlowIcon, cashFlowTheme);

        summaryCards.add(inflowCard, outflowCard, cashFlowCard);
    }

    // REFACTORED: Lógica movida para cá para preencher os grids
    private void updateTransactionDetailsGrids(BigDecimal totalInflows, BigDecimal totalOutflows) {
        // --- Grid de Entradas ---
        inflowsGrid.setItems(List.of(
                new CategoryData("Vendas de Produtos", totalInflows)
        ));
        
        // --- Grid de Saídas ---
        List<CategoryData> outflowData = new ArrayList<>();
        outflowData.add(new CategoryData("Pagamentos Diversos", financialService.getPaymentsTotal()));
        outflowData.add(new CategoryData("Compras de Produtos", financialService.getPurchaseTotal()));
        outflowData.add(new CategoryData("Custos de Frete", financialService.getShippingTotal()));
        outflowsGrid.setItems(outflowData);
    }

    // REFACTORED: Parâmetros e estilo ajustados
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
    
    // REFACTORED: Configuração do Grid agora é feita na inicialização
    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent); // Garante que a lógica do framework seja executada
        
        // A configuração das colunas deve ser feita apenas uma vez.
        // onAttach é um bom lugar para isso, pois é chamado quando o componente é adicionado à UI.
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

    // --- Classe de Dados Simplificada ---
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