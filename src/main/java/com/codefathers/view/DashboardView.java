package com.codefathers.view;

import com.codefathers.factory.ServiceFactory;
import com.codefathers.model.entity.SystemUser;
import com.codefathers.repository.implementations.OrderRepositoryImpl;
import com.codefathers.repository.implementations.ProductRepositoryImpl;
import com.codefathers.repository.implementations.StorageRepositoryImpl;
import com.codefathers.service.AuthService;
import com.codefathers.service.DashboardService;
import com.codefathers.service.FinancialService;
import com.codefathers.service.KpiService;
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

    private KpiService kpiService;
    private final AuthService authService = ServiceFactory.getAuthService();
    private final DashboardService dashboardService = ServiceFactory.getDashboardService();
    private final FinancialService financialService = ServiceFactory.getFinancialService();

    private LocalDate startDay = LocalDate.of(2024, 1, 1);
    private LocalDate endDay = LocalDate.of(2026, 1, 1);

    public DashboardView() {
        this.kpiService = new KpiService(
                new OrderRepositoryImpl(),   // ou como for a implementação
                new ProductRepositoryImpl(),
                new StorageRepositoryImpl()
        );
        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        addClassName("dashboard-view");
        // Header
        add(createHeader());

        // KPI Cards
        add(createKpiSection());

        // Charts Section
        FlexLayout chartContainer = new FlexLayout();
            chartContainer.setSizeFull();
            chartContainer.setAlignItems(Alignment.CENTER);
            chartContainer.setFlexDirection(FlexDirection.COLUMN);
            chartContainer.add(createMainChartsSection(), createWrapChartsSection());
        add(chartContainer);

    }

    private void datePickerListener() {

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
        startDate.addValueChangeListener(e -> endDate.setMin(e.getValue()));
        endDate.addValueChangeListener(e -> startDate.setMax(e.getValue()));

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

        // Criar os cards, inicialmente com as datas padrões
        Runnable updateKpis = () -> {
            kpiLayout.removeAll();

            LocalDate start = startDate.getValue();
            LocalDate end = endDate.getValue();

            String vendasHoje = formatCurrency(kpiService.getTotalRevenue(start, end));
            String pedidosAtivos = String.valueOf(kpiService.getTotalOrders(start, end));
            String produtosEmEstoque = String.valueOf(kpiService.getTotalStockQuantity());
            // Para faturamento mensal, você pode usar a receita total no mês atual, exemplo:
            String faturamentoMensal = formatCurrency(kpiService.getTotalRevenue(
                    LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()),
                    LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())));

            kpiLayout.add(
                    createKpiCard("Vendas Hoje", vendasHoje, "+12%", "⬆️", "success"),
                    createKpiCard("Pedidos Ativos", pedidosAtivos, "+8", "📦", "primary"),
                    createKpiCard("Produtos em Estoque", produtosEmEstoque, "-23", "📦", "warning"),
                    createKpiCard("Faturamento Mensal", faturamentoMensal, "+18%", "💰", "success")
            );
        };

        // Atualiza os KPIs ao mudar datas
        startDate.addValueChangeListener(e -> updateKpis.run());
        endDate.addValueChangeListener(e -> updateKpis.run());

        updateKpis.run(); // Atualiza na inicialização

        var finalLayout = new VerticalLayout(subtitleLayout, kpiLayout);
        finalLayout.setSpacing(true);
        finalLayout.setAlignItems(FlexComponent.Alignment.START);
        return finalLayout;
    }

    // Método auxiliar para formatar BigDecimal em moeda BRL
    private String formatCurrency(BigDecimal value) {
        if (value == null) return "R$ 0,00";
        return java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR")).format(value);
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
                        .withText("Seller performance (Goal %)")
                        .build()
        );

        DashboardService.SeriesData data = dashboardService.getSellersPerformanceChartData(startDay, endDay);

        chartBuilder.withSeries(
                new Series<>("Sales (Goal %)", data.data().toArray(new BigDecimal[0]))
        );

        chartBuilder.withXaxis(
                XAxisBuilder.get()
                        .withCategories(data.categories())
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
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.Margin.End.MEDIUM,
                LumoUtility.Margin.Bottom.MEDIUM
        );

        container.add(chart);
        return container;
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