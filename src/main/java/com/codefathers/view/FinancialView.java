package com.codefathers.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.codefathers.model.entity.Employee;
import com.codefathers.repository.implementations.OrderItemRepositoryImpl;
import com.codefathers.repository.implementations.OrderRepositoryImpl;
import com.codefathers.repository.implementations.PaymentRepositoryImpl;
import com.codefathers.repository.implementations.PurchaseOrderItemRepositoryImpl;
import com.codefathers.repository.implementations.ShippingOrderRepositoryImpl;
import com.codefathers.service.FinancialService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

@PageTitle("Financial")
@Route("financial")
public class FinancialView extends VerticalLayout {

    private Div summaryCards;
    private Grid<CategoryData> inflowsGrid;
    private Grid<CategoryData> outflowsGrid;
    private Grid<PerformerData> performersGrid;
    private Div kpiSection;

    private Div transactionDetails;
    private IntegerField limitField;
    private Button refreshButton;
    FinancialService financialService;
    private final NumberFormat currencyFormatter;

    public FinancialView() {
        this.financialService = new FinancialService(new PaymentRepositoryImpl(), new PurchaseOrderItemRepositoryImpl(),
                new OrderItemRepositoryImpl(), new ShippingOrderRepositoryImpl(), new OrderRepositoryImpl());
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        setupLayout();
        createHeader();
        createControls();
        createSummaryCards();
        createTransactionDetails();

        refreshData();
    }

    private void setupLayout() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("financial-view");
    }

    private void createHeader() {
        H1 title = new H1("💰 Relatório Financeiro");
        title.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.Margin.Bottom.MEDIUM);
        add(title);
    }

    private void createControls() {
        refreshButton = new Button("🔄 Atualizar", e -> refreshData());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout controlsLayout = new HorizontalLayout(refreshButton);
        controlsLayout.setAlignItems(FlexComponent.Alignment.END);
        controlsLayout.addClassName(LumoUtility.Padding.Bottom.MEDIUM);

        add(controlsLayout);
    }

    private void createSummaryCards() {
        summaryCards = new Div();
        summaryCards.addClassName("summary-cards");
        summaryCards.getStyle()
                .set("display", "flex")
                .set("gap", "20px")
                .set("margin-bottom", "20px")
                .set("flex-wrap", "wrap");

        add(summaryCards);
    }

    private void createTransactionDetails() {
        H2 detailsTitle = new H2("💰 Detalhamento de Entradas e Saídas");
        add(detailsTitle);

        transactionDetails = new Div();
        transactionDetails.addClassName("transaction-details");
        transactionDetails.getStyle()
                .set("display", "flex")
                .set("gap", "30px")
                .set("margin-bottom", "30px")
                .set("flex-wrap", "wrap");

        add(transactionDetails);
    }

    private void refreshData() {

        BigDecimal totalInflows = financialService.getTotalInflows();
        BigDecimal totalOutflows = financialService.getTotalOutflows();
        BigDecimal netCashFlow = financialService.getNetCashFlow();
        BigDecimal paymentsTotal = financialService.getPaymentsTotal();
        BigDecimal purchaseTotal = financialService.getPurchaseTotal();
        BigDecimal shippingTotal = financialService.getShippingTotal();

        // 2. Atualiza as seções da UI com os dados obtidos
        updateSummaryCards(totalInflows, totalOutflows, netCashFlow);
        updateTransactionDetails(totalInflows, paymentsTotal, purchaseTotal, shippingTotal);
    }

    private void updateSummaryCards(BigDecimal totalInflows, BigDecimal totalOutflows, BigDecimal netCashFlow) {
        summaryCards.removeAll();

        Component inflowCard = createSummaryCard("💰 Total Entradas", formatCurrency(totalInflows), "success-card");
        Component outflowCard = createSummaryCard("💸 Total Saídas", formatCurrency(totalOutflows), "error-card");

        String flowStatus = netCashFlow.compareTo(BigDecimal.ZERO) >= 0 ? "📈" : "📉";
        String flowClass = netCashFlow.compareTo(BigDecimal.ZERO) >= 0 ? "success-card" : "error-card";
        Component cashFlowCard = createSummaryCard(flowStatus + " Fluxo de Caixa", formatCurrency(netCashFlow),
                flowClass);

        summaryCards.add(inflowCard, outflowCard, cashFlowCard);
    }

    private void updateTransactionDetails(BigDecimal totalInflows, BigDecimal paymentsTotal, BigDecimal purchaseTotal,
            BigDecimal shippingTotal) {
        transactionDetails.removeAll();

        VerticalLayout inputsSection = createTransactionSection("📈 ENTRADAS DE DINHEIRO", "success-section");
        Component ordersDetail = createDetailItem("🛒 Vendas de Produtos", formatCurrency(totalInflows));
        inputsSection.add(ordersDetail);

        VerticalLayout outputsSection = createTransactionSection("📉 SAÍDAS DE DINHEIRO", "error-section");
        Component paymentsDetail = createDetailItem("💳 Pagamentos Diversos", formatCurrency(paymentsTotal));
        Component purchasesDetail = createDetailItem("🛍️ Compras de Produtos", formatCurrency(purchaseTotal));
        Component shippingDetail = createDetailItem("🚚 Custos de Frete", formatCurrency(shippingTotal));
        outputsSection.add(paymentsDetail, purchasesDetail, shippingDetail);

        transactionDetails.add(inputsSection, outputsSection);
    }

    private VerticalLayout createTransactionSection(String title, String className) {
        VerticalLayout section = new VerticalLayout();
        section.setWidth("45%");
        section.addClassName(className);
        section.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("box-shadow", "0 2px 8px var(--lumo-shade-10pct)");

        H3 sectionTitle = new H3(title);
        sectionTitle.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "20px")
                .set("font-size", "18px")
                .set("font-weight", "700");

        if ("success-section".equals(className)) {
            sectionTitle.addClassName(LumoUtility.TextColor.SUCCESS);
            section.getStyle().set("border-left", "4px solid var(--lumo-success-color)");
        } else if ("error-section".equals(className)) {
            sectionTitle.addClassName(LumoUtility.TextColor.ERROR);
            section.getStyle().set("border-left", "4px solid var(--lumo-error-color)");
        }

        section.add(sectionTitle);
        return section;
    }

    private Component createDetailItem(String title, String amount) {
        Div item = new Div();
        item.addClassName("detail-item");
        item.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "8px")
                .set("padding", "15px")
                .set("margin-bottom", "10px")
                .set("border-left", "3px solid var(--lumo-primary-color)");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("display", "block")
                .set("font-weight", "600")
                .set("font-size", "14px")
                .set("color", "var(--lumo-body-text-color)")
                .set("margin-bottom", "5px");

        Span amountSpan = new Span(amount);
        amountSpan.getStyle()
                .set("display", "block")
                .set("font-size", "20px")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)")
                .set("margin-bottom", "5px");

        item.add(titleSpan, amountSpan);
        return item;
    }

    private Component createSummaryCard(String title, String value, String className) {
        Div card = new Div();
        card.addClassName("summary-card");
        card.addClassName(className);
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("padding", "20px")
                .set("min-width", "200px")
                .set("text-align", "center")
                .set("box-shadow", "0 2px 4px var(--lumo-shade-10pct)");

        if ("success-card".equals(className)) {
            card.getStyle().set("border-left", "4px solid var(--lumo-success-color)");
        } else if ("error-card".equals(className)) {
            card.getStyle().set("border-left", "4px solid var(--lumo-error-color)");
        }

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("display", "block")
                .set("font-size", "14px")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "8px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("display", "block")
                .set("font-size", "24px")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-body-text-color)");

        card.add(titleSpan, valueSpan);
        return card;
    }

    private void updateCategoryGrids() {
        // Dados de entrada
        List<CategoryData> inflowData = new ArrayList<>();
        inflowData.add(new CategoryData("Vendas", financialService.getTotalInflows(), "0%"));

        // Dados de saída
        List<CategoryData> outflowData = new ArrayList<>();
        BigDecimal totalOutflows = financialService.getTotalOutflows();

        if (totalOutflows.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal paymentsTotal = financialService.getPaymentsTotal();
            BigDecimal purchaseTotal = financialService.getPurchaseTotal();
            BigDecimal shippingTotal = financialService.getShippingTotal();

            outflowData.add(new CategoryData("Pagamentos", paymentsTotal,
                    calculatePercentage(paymentsTotal, totalOutflows)));
            outflowData.add(new CategoryData("Compras", purchaseTotal,
                    calculatePercentage(purchaseTotal, totalOutflows)));
            outflowData.add(new CategoryData("Frete", shippingTotal,
                    calculatePercentage(shippingTotal, totalOutflows)));
        }

        inflowsGrid.setItems(inflowData);
        outflowsGrid.setItems(outflowData);
    }

    private String formatCurrency(BigDecimal amount) {
        return currencyFormatter.format(amount);
    }

    private String calculatePercentage(BigDecimal amount, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0)
            return "0%";

        BigDecimal percentage = amount
                .divide(total, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return String.format("%.1f%%", percentage.doubleValue());
    }

    // Classes de dados para os grids
    public static class CategoryData {
        private String name;
        private BigDecimal amount;
        private String percentage;

        public CategoryData(String name, BigDecimal amount, String percentage) {
            this.name = name;
            this.amount = amount;
            this.percentage = percentage;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public String getPercentage() {
            return percentage;
        }
    }

    public static class PerformerData {
        private String name;
        private String performance;

        public PerformerData(String name, String performance) {
            this.name = name;
            this.performance = performance;
        }

        public String getName() {
            return name;
        }

        public String getPerformance() {
            return performance;
        }
    }

}
