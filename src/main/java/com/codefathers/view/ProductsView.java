package com.codefathers.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;


@Route("/products")
public class ProductsView extends VerticalLayout {

    public ProductsView() {
        add(new H1("Product List"));
    }

}
