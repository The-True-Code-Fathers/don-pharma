package com.codefathers.view;

import com.codefathers.factory.ServiceFactory;
import com.codefathers.model.entity.SystemUser;
import com.codefathers.service.AuthService;
import com.codefathers.service.DashboardService;
import com.codefathers.util.JsonUtil;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI; // Import UI
import com.vaadin.flow.component.datepicker.DatePicker;
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
import com.vaadin.flow.theme.lumo.Lumo; // Import Lumo
import com.vaadin.flow.theme.lumo.LumoUtility;

// ApexCharts Imports
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.PlotOptionsBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.builder.YAxisBuilder;
import com.github.appreciated.apexcharts.config.builder.TitleSubtitleBuilder;
import com.github.appreciated.apexcharts.config.builder.LegendBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.helper.Series;
import com.github.appreciated.apexcharts.config.yaxis.builder.TitleBuilder;

// ApexCharts Theming Imports
import com.github.appreciated.apexcharts.config.builder.ThemeBuilder;
import com.github.appreciated.apexcharts.config.theme.Mode;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Route("")
@PageTitle("Dashboard | Don Pharma")
public class DashboardView extends FlexLayout {

    private final AuthService authService = ServiceFactory.getAuthService();
    private final DashboardService dashboardService = ServiceFactory.getDashboardService();

    private LocalDate startDay = LocalDate.of(2024, 1, 1);
    private LocalDate endDay = LocalDate.of(2026, 1, 1);

    public DashboardView() {
        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        addClassName("dashboard-view");
        add(createHeader());

        add(createKpiSection());

        FlexLayout chartContainer = new FlexLayout();
            chartContainer.setSizeFull();
            chartContainer.setAlignItems(Alignment.CENTER);
            chartContainer.setFlexDirection(FlexDirection.COLUMN);
            chartContainer.add(createMainChartsSection(), createWrapChartsSection());
        add(chartContainer);

    }

    private Component createHeader() {
        H2 title = new H2("\uD83D\uDC4B Welcome to Don Pharma");

        if (AuthService.isLoggedIn()) {
            SystemUser user = authService.getCurrentUser().orElseThrow(() -> new IllegalStateException("User is not logged in"));
            title = new H2("\uD83D\uDC4B Welcome to Don Pharma, " + user.getUsername());
        }
        title.addClassNames(LumoUtility.Margin.Bottom.NONE, LumoUtility.Margin.Top.MEDIUM);

        HorizontalLayout headerContent = new HorizontalLayout(title);
        headerContent.setWidthFull();
        headerContent.setJustifyContentMode(JustifyContentMode.START); // Align title to start
        headerContent.setAlignItems(Alignment.CENTER);

        VerticalLayout headerLayout = new VerticalLayout(headerContent);
        headerLayout.setPadding(true);
        headerLayout.setSpacing(true);
        headerLayout.setAlignItems(Alignment.CENTER);

        return headerLayout;
    }

    private Component createKpiSection() {
        var subtitle = new Span("Business Key Performance Indicators");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY);

        var startDate = new DatePicker("Start date", LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()));
        var endDate = new DatePicker("End date", LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()));
        startDate
                .addValueChangeListener(e -> endDate.setMin(e.getValue()));
        endDate.addValueChangeListener(
                e -> startDate.setMax(e.getValue()));

        var datePickerLayout = new FlexLayout(startDate, endDate);
        datePickerLayout.getStyle().set("gap", "1em");

        var subtitleLayout = new FlexLayout(subtitle, datePickerLayout);
        subtitleLayout.setWidth("95%");
        subtitleLayout.setFlexWrap(FlexWrap.WRAP);
        subtitleLayout.setFlexDirection(FlexDirection.ROW);
        subtitleLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        subtitleLayout.setAlignItems(Alignment.CENTER);

        var kpiLayout = new FlexLayout();
        kpiLayout.addClassName("kpi-layout");
        kpiLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        kpiLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        kpiLayout.setAlignItems(FlexComponent.Alignment.END);
        kpiLayout.setWidthFull();
        kpiLayout.getStyle().set("gap", "1rem");

        kpiLayout.add(
                createKpiCard("Vendas Hoje", "R$ 45.230,00", "+12%", "⬆️", "success"),
                createKpiCard("Pedidos Ativos", "127", "+8", "📦", "primary"),
                createKpiCard("Produtos em Estoque", "1.847", "-23", "📦", "warning"),
                createKpiCard("Faturamento Mensal", "R$ 890.450,00", "+18%", "💰", "success")
        );
        kpiLayout.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);

        var finalLayout = new VerticalLayout(subtitleLayout, kpiLayout);
        finalLayout.setSpacing(true);
        finalLayout.setAlignItems(FlexComponent.Alignment.START);
        return finalLayout;
    }

    private Component createKpiCard(String title, String value, String change, String iconString, String theme) {
        Span cardIcon = new Span(iconString);
        cardIcon.addClassNames(LumoUtility.FontSize.XLARGE);
        cardIcon.getStyle().set("color", getThemeColor(theme));

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
                LumoUtility.Margin.End.MEDIUM,
                LumoUtility.Margin.Bottom.MEDIUM
        );
        layout.setFlexGrow(1, content);
        layout.setMinWidth("250px");
        layout.setMaxWidth("350px");

        return layout;
    }

    private Component createMainChartsSection() {
        FlexLayout layout = new FlexLayout();

        layout.add(
                createSalesSummaryChart()
        );

        layout.setWidth("80%");
        layout.setHeight("50%"); // ou um tamanho controlado pelo layout pai
        layout.setAlignSelf(Alignment.CENTER);

        return layout;
    }

    private Component createWrapChartsSection() {
        var horizontalWrapChartsLayout = new FlexLayout();

        horizontalWrapChartsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        horizontalWrapChartsLayout.add(
                createTopProductsChart(),
                createOrderStatusChart(),
                createEmployeePerformanceChart()
        );

        horizontalWrapChartsLayout.setWidth("80%");
        horizontalWrapChartsLayout.setHeight("80%"); // ou um tamanho controlado pelo layout pai

        return horizontalWrapChartsLayout;
    }

    /**
     * Helper to configure ApexCharts for Lumo theme integration, especially dark mode.
     * This will set the chart's internal theme mode and try to use Lumo CSS variables for colors.
     *
     * @param builder The ApexChartsBuilder instance.
     */
    private void configureChartForLumoTheme(ApexChartsBuilder builder, boolean isDarkMode) {

        // Set ApexCharts theme mode based on the current Vaadin UI's theme.
        // This makes ApexCharts internally adjust text, grid lines, and backgrounds for dark/light mode.
        builder.withTheme(
                ThemeBuilder.get()
                        .withMode(isDarkMode ? Mode.DARK : Mode.LIGHT)
                        .build()
        );
    }


    /**
     * Creates an ApexCharts Line Chart for sales summary.
     *
     * @return An ApexCharts component displaying sales trend.
     */
    private Component createSalesSummaryChart() {
        // A more concise and safe way to check the theme
        boolean isDarkTheme = Lumo.DARK.equals(UI.getCurrent().getElement().getAttribute("theme"));

        DashboardService.SeriesData chartData = dashboardService.getSalesChartData(startDay, endDay);

        ApexChartsBuilder chartBuilder = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.LINE)
                        .withHeight("350px")
                        .build())
                .withTitle(TitleSubtitleBuilder.get()
                        .withText("Sales summary")
                        .build())
                .withSeries(new Series<>("Sales", chartData.data().toArray(new BigDecimal[0]))) // Use data from DTO
                .withXaxis(XAxisBuilder.get()
                        .withCategories(chartData.categories()) // Use categories from DTO
                        .build())
                .withYaxis(YAxisBuilder.get()
                        .withTitle(TitleBuilder.get().withText("Amount (R$)").build())
                        .build());


        // 3. Apply theme and wrap
        configureChartForLumoTheme(chartBuilder, isDarkTheme);
        return wrapChartInContainer(chartBuilder.build());
    }

    /**
     * Creates an ApexCharts Donut Chart for order status.
     *
     * @return An ApexCharts component displaying order status distribution.
     */
    private Component createOrderStatusChart() {

        boolean isDarkTheme = UI.getCurrent().getElement().getAttribute("theme") != null &&
                UI.getCurrent().getElement().getAttribute("theme").equals(Lumo.DARK);

        ApexChartsBuilder chartBuilder = ApexChartsBuilder.get();
        chartBuilder.withChart(
                ChartBuilder.get()
                        .withType(Type.DONUT)
                        .withHeight("350px")
                        .build()
        );

        chartBuilder.withTitle(
                TitleSubtitleBuilder.get()
                        .withText("Status dos Pedidos")
                        .build()
        );

        DashboardService.SeriesData seriesData = dashboardService.getOrderStatusChartData(startDay, endDay);

        chartBuilder.withLabels(seriesData.categories().toArray(String[]::new));

        Double[] seriesValues = seriesData.data()
                .stream()
                .map(BigDecimal::doubleValue)
                .toArray(Double[]::new);
        chartBuilder.withSeries(seriesValues);

        chartBuilder.withLegend(
                LegendBuilder.get()
                        .withPosition(Position.BOTTOM) // Place legend below the chart
                        .build()
        );

        configureChartForLumoTheme(chartBuilder, isDarkTheme); // Apply theme configuration
        com.github.appreciated.apexcharts.ApexCharts apexChart = chartBuilder.build();
        return wrapChartInContainer(apexChart);
    }

    /**
     * Creates an ApexCharts Bar Chart for top products sold.
     *
     * @return An ApexCharts component displaying top 5 products.
     */
    private Component createTopProductsChart() {

        boolean isDarkTheme = UI.getCurrent().getElement().getAttribute("theme") != null &&
                UI.getCurrent().getElement().getAttribute("theme").equals(Lumo.DARK);

        ApexChartsBuilder chartBuilder = ApexChartsBuilder.get();
        chartBuilder.withChart(
                ChartBuilder.get()
                        .withType(Type.BAR)
                        .withHeight("300px")
                        .build()
        );

        chartBuilder.withPlotOptions(
                PlotOptionsBuilder.get()
                        .withBar(
                                BarBuilder.get()
                                        .withHorizontal(true) // Horizontal bars for product names
                                        .build()
                        )
                        .build()
        );

        chartBuilder.withTitle(
                TitleSubtitleBuilder.get()
                        .withText("Top 5 Produtos Mais Vendidos")
                        .build()
        );

        DashboardService.SeriesData seriesData = dashboardService.getTopProductChartData(
                startDay,
                endDay,
                5);

        log.debug("{}", JsonUtil.toPrettyJson(seriesData));

        // Dummy data for top products
        chartBuilder.withSeries(
                new Series<>("Units Sold", seriesData.data().toArray(new BigDecimal[0]))
        );

        chartBuilder.withXaxis(
                XAxisBuilder.get()
                        .withCategories(seriesData.categories())
                        .build()
        );

        configureChartForLumoTheme(chartBuilder, isDarkTheme); // Apply theme configuration
        com.github.appreciated.apexcharts.ApexCharts apexChart = chartBuilder.build();
        return wrapChartInContainer(apexChart);
    }

    /**
     * Creates an ApexCharts Bar Chart for employee performance.
     *
     * @return An ApexCharts component displaying employee sales performance.
     */
    private Component createEmployeePerformanceChart() {

        boolean isDarkTheme = UI.getCurrent().getElement().getAttribute("theme") != null &&
                UI.getCurrent().getElement().getAttribute("theme").equals(Lumo.DARK);

        ApexChartsBuilder chartBuilder = ApexChartsBuilder.get();
        chartBuilder.withChart(
                ChartBuilder.get()
                        .withType(Type.BAR)
                        .withHeight("300px") // Removed fixed height
                        .build()
        );

        chartBuilder.withPlotOptions(
                PlotOptionsBuilder.get()
                        .withBar(
                                BarBuilder.get()
                                        .withHorizontal(true) // Horizontal bars for employee names
                                        .build()
                        )
                        .build()
        );

        chartBuilder.withTitle(
                TitleSubtitleBuilder.get()
                        .withText("Performance dos Vendedores (Goal %)")
                        .build()
        );

        // Dummy data for employee performance
        chartBuilder.withSeries(
                new Series<>("Sales (Goal %)", 80, 70, 60, 65, 75)
        );

        chartBuilder.withXaxis(
                XAxisBuilder.get()
                        .withCategories("João Silva", "Maria Santos", "Pedro Oliveira", "Ana Costa", "Carlos Lima")
                        .build()
        );

        chartBuilder.withYaxis(
                YAxisBuilder.get()
                        .withTitle(TitleBuilder.get().withText("Sales").build())
                        .build()
        );

        configureChartForLumoTheme(chartBuilder, isDarkTheme); // Apply theme configuration
        com.github.appreciated.apexcharts.ApexCharts apexChart = chartBuilder.build();
        return wrapChartInContainer(apexChart);
    }

    /**
     * Helper method to wrap an ApexCharts component in a styled container for consistent layout.
     */
    private VerticalLayout wrapChartInContainer(com.github.appreciated.apexcharts.ApexCharts chart) {
        VerticalLayout container = new VerticalLayout();
        container.addClassNames(
                LumoUtility.Background.CONTRAST_5,
                // LumoUtility.BorderRadius.LARGE,
                // LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.Margin.End.MEDIUM,
                LumoUtility.Margin.Bottom.MEDIUM
        );

        container.add(chart);
        return container;
    }


    /**
     * Creates a section containing tables, replacing the commercial 'Board' with a FlexLayout.
     * Uses Vaadin Grid for tabular data display.
     *
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
     *
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

    /**
     * Helper to create order data for the Grid
     */
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
     *
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

    /**
     * Helper to create product data for the Grid
     */
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
     *
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

    /**
     * Helper to create shipping data for the Grid
     */
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