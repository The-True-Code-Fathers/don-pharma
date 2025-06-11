package com.codefathers.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Dashboard 🚀");

        HorizontalLayout kpisRow1 = new HorizontalLayout(
                createKpiCard("Receita Total", "R$ 500.000,00"),
                createKpiCard("Produtos em Estoque", "1.200 unidades"),
                createKpiCard("Pedidos de Venda", "82 este mês"),
                createKpiCard("Pedidos de Compra", "24 este mês")
        );
        kpisRow1.setSpacing(true);
        kpisRow1.setWidthFull();

        HorizontalLayout kpisRow2 = new HorizontalLayout(
                createKpiCard("Funcionários", "35 ativos"),
                createKpiCard("Provedores de Frete", "5 cadastrados"),
                createKpiCard("Itens Vendidos", "3.450 unidades"),
                createKpiCard("Lucro Estimado", "R$ 175.000,00")
        );
        kpisRow2.setSpacing(true);
        kpisRow2.setWidthFull();

        add(kpisRow1, kpisRow2);
    }

    private Component createKpiCard(String title, String value) {
        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-weight", "600");

        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("font-size", "1.5em").set("font-weight", "bold");

        VerticalLayout card = new VerticalLayout(titleSpan, valueSpan);
        card.setPadding(true);
        card.setWidth("100%");
        card.getStyle()
                .set("border", "1px solid #e0e0e0")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.05)")
                .set("padding", "1em");

        return card;
    }
}
