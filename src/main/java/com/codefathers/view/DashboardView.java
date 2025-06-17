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
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.legend.Position;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.config.theme.Mode;
import com.github.appreciated.apexcharts.config.yaxis.builder.TitleBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.stream.Stream;

@Slf4j
@Route("")
@PageTitle("Dashboard | Don Pharma")
public class DashboardView extends FlexLayout {

    private final KpiService kpiService;
    private final AuthService authService = ServiceFactory.getAuthService();
    private final DashboardService dashboardService = ServiceFactory.getDashboardService();
    private final FinancialService financialService = ServiceFactory.getFinancialService();

    // Layouts to hold the charts. We will clear and re-add charts to these.
    private final FlexLayout mainChartLayout = new FlexLayout();
    private final FlexLayout wrapChartsLayout = new FlexLayout();
    private final FlexLayout kpiLayout = new FlexLayout();

    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;

    public DashboardView() {
        this.kpiService = new KpiService(
                new OrderRepositoryImpl(),
                new ProductRepositoryImpl(),
                new StorageRepositoryImpl()
        );

        // Configure date pickers
        startDatePicker = new DatePicker("Start date", LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()));
        endDatePicker = new DatePicker("End date", LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()));
        startDatePicker.addValueChangeListener(e -> {
            endDatePicker.setMin(e.getValue());
            updateDashboard();
        });
        endDatePicker.addValueChangeListener(e -> {
            startDatePicker.setMax(e.getValue());
            updateDashboard();
        });

        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        addClassName("dashboard-view");

        add(createHeader());
        add(createKpiSection());
        add(createChartsContainer());

        // Initial load of all dynamic data
        updateDashboard();
    }

    private void updateDashboard() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        updateKpiCards(start, end);
        updateAllCharts(start, end);
    }

    private Component createHeader() {
        H2 title = new H2("\uD83D\uDC4B Welcome to Don Pharma");

        if (AuthService.isLoggedIn()) {
            SystemUser user = authService.getCurrentUser().orElseThrow(() -> new IllegalStateException("User is not logged in"));
            title.setText("\uD83D\uDC4B Welcome to Don Pharma, " + user.getUsername());
        }
        title.addClassNames(LumoUtility.Margin.Bottom.NONE, LumoUtility.Margin.Top.MEDIUM);

        HorizontalLayout headerContent = new HorizontalLayout(title);
        headerContent.setWidthFull();
        headerContent.setJustifyContentMode(JustifyContentMode.START);
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

        var datePickerLayout = new FlexLayout(startDatePicker, endDatePicker);
        datePickerLayout.getStyle().set("gap", "1em");

        var subtitleLayout = new FlexLayout(subtitle, datePickerLayout);
        subtitleLayout.setWidth("95%");
        subtitleLayout.setFlexWrap(FlexWrap.WRAP);
        subtitleLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        subtitleLayout.setAlignItems(Alignment.CENTER);

        // Configure KPI layout
        kpiLayout.addClassName("kpi-layout");
        kpiLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        kpiLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        kpiLayout.setAlignItems(FlexComponent.Alignment.END);
        kpiLayout.setWidthFull();
        kpiLayout.getStyle().set("gap", "1rem");

        var finalLayout = new VerticalLayout(subtitleLayout, kpiLayout);
        finalLayout.setSpacing(true);
        finalLayout.setAlignItems(FlexComponent.Alignment.START);
        return finalLayout;
    }

    private void updateKpiCards(LocalDate start, LocalDate end) {
        kpiLayout.removeAll();

        String vendasHoje = formatCurrency(kpiService.getTotalRevenue(start, end));
        String pedidosAtivos = String.valueOf(kpiService.getTotalOrders(start, end));
        String produtosEmEstoque = String.valueOf(kpiService.getTotalStockQuantity());
        String faturamentoMensal = formatCurrency(kpiService.getTotalRevenue(
                LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()),
                LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())));

        Component[] cards = {
                createKpiCard("Vendas Hoje", vendasHoje, "+12%", "⬆️", "success"),
                createKpiCard("Pedidos Ativos", pedidosAtivos, "+8", "📦", "primary"),
                createKpiCard("Produtos em Estoque", produtosEmEstoque, "-23", "📦", "warning"),
                createKpiCard("Faturamento Mensal", faturamentoMensal, "+18%", "💰", "success")
        };
        kpiLayout.add(cards);
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
                LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.LARGE, LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10, LumoUtility.Margin.End.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM
        );
        layout.setFlexGrow(1, content);
        layout.setMinWidth("250px");
        layout.setMaxWidth("350px");

        return layout;
    }

    private Component createChartsContainer() {
        // Main chart layout
        mainChartLayout.setWidth("80%");
        mainChartLayout.setHeight("50%");
        mainChartLayout.setAlignSelf(Alignment.CENTER);

        // Wrapped charts layout
        wrapChartsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        wrapChartsLayout.setWidth("80%");
        wrapChartsLayout.setHeight("80%");

        // Main container for all charts
        FlexLayout chartContainer = new FlexLayout(mainChartLayout, wrapChartsLayout);
        chartContainer.setSizeFull();
        chartContainer.setAlignItems(Alignment.CENTER);
        chartContainer.setFlexDirection(FlexDirection.COLUMN);
        return chartContainer;
    }

    private void updateAllCharts(LocalDate start, LocalDate end) {
        // Clear old charts
        mainChartLayout.removeAll();
        wrapChartsLayout.removeAll();

        // Add new, updated charts
        mainChartLayout.add(createSalesSummaryChart(start, end));

        wrapChartsLayout.add(
                createTopProductsChart(start, end),
                createOrderStatusChart(start, end),
                createEmployeePerformanceChart(start, end)
        );
    }

    private Component createSalesSummaryChart(LocalDate startDay, LocalDate endDay) {
        DashboardService.SeriesData chartData = dashboardService.getSalesChartData(startDay, endDay);
        ApexChartsBuilder chartBuilder = new ApexChartsBuilder()
                .withChart(ChartBuilder.get().withType(Type.LINE).withHeight("350px").build())
                .withTitle(TitleSubtitleBuilder.get().withText("Sales summary").build())
                .withSeries(new Series<>("Sales", chartData.data().toArray(new BigDecimal[0])))
                .withXaxis(XAxisBuilder.get().withCategories(chartData.categories()).build())
                .withYaxis(YAxisBuilder.get().withTitle(TitleBuilder.get().withText("Amount (R$)").build()).build());

        configureChartForLumoTheme(chartBuilder);
        return wrapChartInContainer(chartBuilder.build());
    }

    private Component createOrderStatusChart(LocalDate startDay, LocalDate endDay) {
        DashboardService.SeriesData seriesData = dashboardService.getOrderStatusChartData(startDay, endDay);
        Double[] seriesValues = seriesData.data().stream().map(BigDecimal::doubleValue).toArray(Double[]::new);

        ApexChartsBuilder chartBuilder = new ApexChartsBuilder()
                .withChart(ChartBuilder.get().withType(Type.DONUT).withHeight("350px").build())
                .withTitle(TitleSubtitleBuilder.get().withText("Status dos Pedidos").build())
                .withLabels(seriesData.categories().toArray(String[]::new))
                .withSeries(seriesValues)
                .withLegend(LegendBuilder.get().withPosition(Position.BOTTOM).build());

        configureChartForLumoTheme(chartBuilder);
        return wrapChartInContainer(chartBuilder.build());
    }

    private Component createTopProductsChart(LocalDate startDay, LocalDate endDay) {
        DashboardService.SeriesData seriesData = dashboardService.getTopProductChartData(startDay, endDay, 5);
        log.debug("{}", JsonUtil.toPrettyJson(seriesData));

        ApexChartsBuilder chartBuilder = new ApexChartsBuilder()
                .withChart(ChartBuilder.get().withType(Type.BAR).withHeight("300px").build())
                .withPlotOptions(PlotOptionsBuilder.get().withBar(BarBuilder.get().withHorizontal(true).build()).build())
                .withTitle(TitleSubtitleBuilder.get().withText("Top 5 Produtos Mais Vendidos").build())
                .withSeries(new Series<>("Units Sold", seriesData.data().toArray(new BigDecimal[0])))
                .withXaxis(XAxisBuilder.get().withCategories(seriesData.categories()).build());

        configureChartForLumoTheme(chartBuilder);
        return wrapChartInContainer(chartBuilder.build());
    }

    private Component createEmployeePerformanceChart(LocalDate startDay, LocalDate endDay) {
        DashboardService.SeriesData data = dashboardService.getSellersPerformanceChartData(startDay, endDay);
        ApexChartsBuilder chartBuilder = new ApexChartsBuilder()
                .withChart(ChartBuilder.get().withType(Type.BAR).withHeight("300px").build())
                .withPlotOptions(PlotOptionsBuilder.get().withBar(BarBuilder.get().withHorizontal(true).build()).build())
                .withTitle(TitleSubtitleBuilder.get().withText("Seller performance (Goal %)")
                        .build())
                .withSeries(new Series<>("Sales (Goal %)", data.data().toArray(new BigDecimal[0])))
                .withXaxis(XAxisBuilder.get().withCategories(data.categories()).build())
                .withYaxis(YAxisBuilder.get().withTitle(TitleBuilder.get().withText("Sales").build()).build());

        configureChartForLumoTheme(chartBuilder);
        return wrapChartInContainer(chartBuilder.build());
    }

    private void configureChartForLumoTheme(ApexChartsBuilder builder) {
        boolean isDarkMode = Lumo.DARK.equals(UI.getCurrent().getElement().getAttribute("theme"));
        builder.withTheme(ThemeBuilder.get().withMode(isDarkMode ? Mode.DARK : Mode.LIGHT).build());
    }

    private VerticalLayout wrapChartInContainer(ApexCharts chart) {
        VerticalLayout container = new VerticalLayout(chart);
        container.addClassNames(
                LumoUtility.Background.CONTRAST_5, LumoUtility.BorderRadius.LARGE, LumoUtility.Border.ALL,
                LumoUtility.BorderColor.CONTRAST_10, LumoUtility.Margin.End.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM
        );
        return container;
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "R$ 0,00";
        return java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR")).format(value);
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