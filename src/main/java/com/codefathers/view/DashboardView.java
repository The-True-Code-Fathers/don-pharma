package com.codefathers.view;

import com.codefathers.factory.ServiceFactory;
import com.codefathers.model.entity.SystemUser;
import com.codefathers.service.AuthService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Route("")
@PageTitle("Dashboard | Sistema de Gestão")
public class DashboardView extends VerticalLayout {

    private AuthService authService = ServiceFactory.getAuthService();

    public DashboardView() {
        setSizeFull();
        addClassName("dashboard-view");

        // Header
        add(createHeader());

        // KPI Cards
        add(createKpiSection());

        // Charts Section
        add(createChartsSection());

        // Tables Section
        add(createTablesSection());
    }

    private Component createHeader() {
        H2 title = new H2("\uD83D\uDC4B Welcome to Don Pharma");

        if (AuthService.isLoggedIn()) {
            SystemUser user = authService.getCurrentUser().orElseThrow(() -> new IllegalStateException("User is not logged in"));
            title = new H2("\uD83D\uDC4B Welcome to Don Pharma, " + user.getUsername());
        }
        title.addClassNames(LumoUtility.Margin.Bottom.NONE, LumoUtility.Margin.Top.MEDIUM);

        VerticalLayout header = new VerticalLayout(title);
        header.setPadding(false);
        header.setSpacing(false);

        return header;
    }

    private Component createKpiSection() {
        // FlexLayout allows items to wrap to the next line on smaller screens
        FlexLayout kpiLayout = new FlexLayout();
        kpiLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP); // Allow cards to wrap
        kpiLayout.setJustifyContentMode(JustifyContentMode.AROUND); // Center cards
        kpiLayout.setAlignItems(FlexComponent.Alignment.END); // Align items to the start of the cross axis
        kpiLayout.setMinWidth("90%");
        //kpiLayout.setGap("1rem"); // Add spacing between cards

        Span subtitle = new Span("Business Key Performance Indicators");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY);

        kpiLayout.add(
                createKpiCard("Vendas Hoje", "R$ 45.230,00", "+12%", "⬆️", "success"),
                createKpiCard("Pedidos Ativos", "127", "+8", "📦", "primary"),
                createKpiCard("Produtos em Estoque", "1.847", "-23", "📦", "warning"),
                createKpiCard("Faturamento Mensal", "R$ 890.450,00", "+18%", "💰", "success")
        );
        kpiLayout.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);

        VerticalLayout layout = new VerticalLayout(subtitle, kpiLayout);
        layout.setSpacing(true);
        // Align all items within this VerticalLayout to the start (left)
        layout.setAlignItems(FlexComponent.Alignment.START);
        return layout;
    }

    private Component createKpiCard(String title, String value, String change, String iconString, String theme) {
        Span cardIcon = new Span(iconString);
        cardIcon.addClassNames(LumoUtility.FontSize.XLARGE); // Make emoji larger
        cardIcon.getStyle().set("color", getThemeColor(theme)); // Apply theme color

        H3 cardValue = new H3(value);
        cardValue.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontSize.XLARGE, LumoUtility.FontWeight.BOLD);

        Span cardTitle = new Span(title);
        cardTitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        Span changeSpan = new Span(change);
        changeSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.MEDIUM);
        changeSpan.getStyle().set("color", getThemeColor(theme));

        VerticalLayout content = new VerticalLayout(cardTitle, cardValue, changeSpan);
        content.setPadding(false);
        content.setSpacing(false);

        HorizontalLayout layout = new HorizontalLayout(cardIcon, content);
        layout.setAlignItems(Alignment.CENTER);
        layout.setPadding(true);
        layout.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.Margin.End.MEDIUM, // Added right margin for spacing
                LumoUtility.Margin.Bottom.MEDIUM // Added bottom margin for wrapping
        );
        layout.setFlexGrow(1, content); // Make content grow
        layout.setMinWidth("250px"); // Ensure cards don't get too small
        layout.setMaxWidth("350px"); // Limit card width for better layout on large screens

        return layout;
    }

    private Component createChartsSection() {
        FlexLayout chartsLayout = new FlexLayout();
        chartsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        chartsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        chartsLayout.setAlignItems(FlexComponent.Alignment.START);
        // chartsLayout.setGap("1rem"); // Removed setGap as it might not be available

        chartsLayout.add(createSalesSummary(), createOrderStatusSummary());
        chartsLayout.add(createTopProductsSummary(), createEmployeePerformanceSummary());

        chartsLayout.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        return chartsLayout;
    }

    /**
     * Creates a text-based summary of sales data, replacing a commercial chart.
     * @return A component displaying sales summary.
     */
    private Component createSalesSummary() {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(true);
        container.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.Margin.End.MEDIUM, // Added right margin for spacing
                LumoUtility.Margin.Bottom.MEDIUM // Added bottom margin for wrapping
        );
        container.setMinWidth("300px");
        container.setMaxWidth("500px");
        container.setFlexGrow(1);

        H3 title = new H3("Vendas dos Últimos 30 Dias");
        title.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.SMALL);

        Span totalSales = new Span("Total: R$ 45.000,00");
        totalSales.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD);

        Span trend = new Span("Trend: +18% from previous period");
        trend.addClassNames(LumoUtility.TextColor.SUCCESS, LumoUtility.FontSize.SMALL);

        Span dataPoints = new Span("Data points: 25k, 28k, 32k, 29k, 35k, 38k, 45k (values every 5 days)");
        dataPoints.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        container.add(title, totalSales, trend, dataPoints);
        return container;
    }

    /**
     * Creates a text-based summary of order status data, replacing a commercial chart.
     * @return A component displaying order status summary.
     */
    private Component createOrderStatusSummary() {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(true);
        container.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.Margin.End.MEDIUM, // Added right margin for spacing
                LumoUtility.Margin.Bottom.MEDIUM // Added bottom margin for wrapping
        );
        container.setMinWidth("300px");
        container.setMaxWidth("500px");
        container.setFlexGrow(1);

        H3 title = new H3("Status dos Pedidos");
        title.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.SMALL);

        VerticalLayout statusList = new VerticalLayout();
        statusList.setSpacing(false);
        statusList.setPadding(false);

        statusList.add(new Span("• Pendente: 45"));
        statusList.add(new Span("• Processando: 32"));
        statusList.add(new Span("• Enviado: 28"));
        statusList.add(new Span("• Entregue: 95"));
        statusList.add(new Span("• Cancelado: 8"));
        statusList.addClassNames(LumoUtility.FontSize.SMALL);

        container.add(title, statusList);
        return container;
    }

    /**
     * Creates a text-based summary of top products data, replacing a commercial chart.
     * @return A component displaying top products summary.
     */
    private Component createTopProductsSummary() {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(true);
        container.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.Margin.End.MEDIUM, // Added right margin for spacing
                LumoUtility.Margin.Bottom.MEDIUM // Added bottom margin for wrapping
        );
        container.setMinWidth("300px");
        container.setMaxWidth("500px");
        container.setFlexGrow(1);

        H3 title = new H3("Top 5 Produtos Mais Vendidos");
        title.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.SMALL);

        VerticalLayout productList = new VerticalLayout();
        productList.setSpacing(false);
        productList.setPadding(false);

        productList.add(new Span("1. Produto A: 127 units"));
        productList.add(new Span("2. Produto B: 98 units"));
        productList.add(new Span("3. Produto C: 87 units"));
        productList.add(new Span("4. Produto D: 76 units"));
        productList.add(new Span("5. Produto E: 65 units"));
        productList.addClassNames(LumoUtility.FontSize.SMALL);

        container.add(title, productList);
        return container;
    }

    /**
     * Creates a text-based summary of employee performance data, replacing a commercial chart.
     * @return A component displaying employee performance summary.
     */
    private Component createEmployeePerformanceSummary() {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(true);
        container.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.Margin.End.MEDIUM, // Added right margin for spacing
                LumoUtility.Margin.Bottom.MEDIUM // Added bottom margin for wrapping
        );
        container.setMinWidth("300px");
        container.setMaxWidth("500px");
        container.setFlexGrow(1);

        H3 title = new H3("Performance dos Vendedores");
        title.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.SMALL);

        VerticalLayout employeeList = new VerticalLayout();
        employeeList.setSpacing(false);
        employeeList.setPadding(false);

        employeeList.add(new Span("• João Silva: R$ 45.000"));
        employeeList.add(new Span("• Maria Santos: R$ 38.000"));
        employeeList.add(new Span("• Pedro Oliveira: R$ 35.000"));
        employeeList.add(new Span("• Ana Costa: R$ 32.000"));
        employeeList.add(new Span("• Carlos Lima: R$ 28.000"));
        employeeList.addClassNames(LumoUtility.FontSize.SMALL);

        container.add(title, employeeList);
        return container;
    }

    /**
     * Creates a section containing tables, replacing the commercial 'Board' with a FlexLayout.
     * Uses Vaadin Grid for tabular data display.
     * @return A component representing the Tables section.
     */
    private Component createTablesSection() {
        FlexLayout tablesLayout = new FlexLayout();
        tablesLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        tablesLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        tablesLayout.setAlignItems(FlexComponent.Alignment.START);
        // tablesLayout.setGap("1rem"); // Removed setGap as it might not be available

        tablesLayout.add(createRecentOrdersTable(), createLowStockTable());
        tablesLayout.add(createShippingStatusTable()); // Place shipping status table alone or adjust layout as needed

        return tablesLayout;
    }

    /**
     * Creates a table for recent orders using Vaadin Grid.
     * @return A component displaying recent orders table.
     */
    private Component createRecentOrdersTable() {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(true);
        container.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.Margin.End.MEDIUM, // Added right margin for spacing
                LumoUtility.Margin.Bottom.MEDIUM // Added bottom margin for wrapping
        );
        container.setMinWidth("400px"); // Minimum width for the table container
        container.setFlexGrow(1);

        H3 title = new H3("Pedidos Recentes");
        title.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.SMALL);

        Grid<Map<String, String>> grid = new Grid<>();
        grid.addColumn(row -> row.get("id")).setHeader("ID");
        grid.addColumn(row -> row.get("client")).setHeader("Cliente");
        grid.addColumn(row -> row.get("value")).setHeader("Valor");
        grid.addColumn(row -> row.get("status")).setHeader("Status");

        // Simulate data
        List<Map<String, String>> recentOrders = new ArrayList<>();
        recentOrders.add(createOrderData("#12345", "João Silva", "R$ 1.250,00", "Processando"));
        recentOrders.add(createOrderData("#12346", "Maria Santos", "R$ 890,50", "Enviado"));
        recentOrders.add(createOrderData("#12347", "Pedro Oliveira", "R$ 2.100,00", "Entregue"));
        recentOrders.add(createOrderData("#12348", "Ana Costa", "R$ 750,25", "Pendente"));
        recentOrders.add(createOrderData("#12349", "Carlos Lima", "R$ 1.450,75", "Processando"));
        grid.setItems(recentOrders);

        grid.setHeight("250px"); // Fixed height for the grid
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);

        container.add(title, grid);
        return container;
    }

    /** Helper to create order data for the Grid */
    private Map<String, String> createOrderData(String id, String client, String value, String status) {
        Map<String, String> data = new HashMap<>();
        data.put("id", id);
        data.put("client", client);
        data.put("value", value);
        data.put("status", status);
        return data;
    }

    /**
     * Creates a table for low stock products using Vaadin Grid.
     * @return A component displaying low stock products table.
     */
    private Component createLowStockTable() {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(true);
        container.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.Margin.End.MEDIUM, // Added right margin for spacing
                LumoUtility.Margin.Bottom.MEDIUM // Added bottom margin for wrapping
        );
        container.setMinWidth("400px");
        container.setFlexGrow(1);

        H3 title = new H3("Produtos com Estoque Baixo");
        title.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.SMALL);

        Grid<Map<String, String>> grid = new Grid<>();
        grid.addColumn(row -> row.get("product")).setHeader("Produto");
        grid.addColumn(row -> row.get("sku")).setHeader("SKU");
        grid.addColumn(row -> row.get("stock")).setHeader("Estoque");
        grid.addColumn(row -> row.get("min")).setHeader("Mín.");

        // Simulate data
        List<Map<String, String>> lowStockProducts = new ArrayList<>();
        lowStockProducts.add(createProductData("Produto A", "PRD001", "5", "10"));
        lowStockProducts.add(createProductData("Produto B", "PRD002", "3", "15"));
        lowStockProducts.add(createProductData("Produto C", "PRD003", "8", "20"));
        lowStockProducts.add(createProductData("Produto D", "PRD004", "2", "10"));
        grid.setItems(lowStockProducts);

        grid.setHeight("250px"); // Fixed height for the grid
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);

        container.add(title, grid);
        return container;
    }

    /** Helper to create product data for the Grid */
    private Map<String, String> createProductData(String product, String sku, String stock, String min) {
        Map<String, String> data = new HashMap<>();
        data.put("product", product);
        data.put("sku", sku);
        data.put("stock", stock);
        data.put("min", min);
        return data;
    }

    /**
     * Creates a table for shipping status using Vaadin Grid.
     * @return A component displaying shipping status table.
     */
    private Component createShippingStatusTable() {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(true);
        container.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.Margin.End.MEDIUM, // Added right margin for spacing
                LumoUtility.Margin.Bottom.MEDIUM // Added bottom margin for wrapping
        );
        container.setMinWidth("400px");
        container.setFlexGrow(1);

        H3 title = new H3("Status de Entregas");
        title.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.SMALL);

        Grid<Map<String, String>> grid = new Grid<>();
        grid.addColumn(row -> row.get("carrier")).setHeader("Transportadora");
        grid.addColumn(row -> row.get("deliveries")).setHeader("Entregas");
        grid.addColumn(row -> row.get("avgTime")).setHeader("Prazo Médio");
        grid.addColumn(row -> row.get("status")).setHeader("Status");

        // Simulate data
        List<Map<String, String>> shippingStatus = new ArrayList<>();
        shippingStatus.add(createShippingData("Correios", "45", "5 dias", "Normal"));
        shippingStatus.add(createShippingData("Transportadora X", "32", "3 dias", "Rápido"));
        shippingStatus.add(createShippingData("Express Y", "28", "2 dias", "Expresso"));
        grid.setItems(shippingStatus);

        grid.setHeight("250px"); // Fixed height for the grid
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);

        container.add(title, grid);
        return container;
    }

    /** Helper to create shipping data for the Grid */
    private Map<String, String> createShippingData(String carrier, String deliveries, String avgTime, String status) {
        Map<String, String> data = new HashMap<>();
        data.put("carrier", carrier);
        data.put("deliveries", deliveries);
        data.put("avgTime", avgTime);
        data.put("status", status);
        return data;
    }


    private String getThemeColor(String theme) {
        return switch (theme) {
            case "success" -> "var(--lumo-success-color)";
            case "warning" -> "var(--lumo-warning-color)";
            case "error" -> "var(--lumo-error-color)";
            case "primary" -> "var(--lumo-primary-color)";
            default -> "var(--lumo-contrast-color)";
        };
    }
}