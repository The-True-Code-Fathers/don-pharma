package com.codefathers.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout  {

    public MainView() {
        // Configure main layout
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Create header
        HorizontalLayout header = createHeader();

        // Create main content area
        HorizontalLayout contentLayout = createContentLayout();

        // Add components to main view
        add(header, contentLayout);
        expand(contentLayout);
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        H1 title = new H1("MyApp");
        title.getStyle().set("margin", "0");
        header.add(title);

        return header;
    }

    private HorizontalLayout createContentLayout() {
        HorizontalLayout contentLayout = new HorizontalLayout();
        contentLayout.setSizeFull();
        contentLayout.setSpacing(false);

        // Create sidebar
        VerticalLayout sidebar = createSidebar();

        // Create main content area
        Div content = new Div();
        content.setSizeFull();
        content.add(new Span("Main content area"));
        content.getStyle().set("padding", "var(--lumo-space-l)");

        contentLayout.add(sidebar, content);
        contentLayout.setFlexGrow(1, content);
        return contentLayout;
    }

    private VerticalLayout createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setWidth("250px");
        sidebar.setHeightFull();
        sidebar.setPadding(true);
        sidebar.setSpacing(false);
        sidebar.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border-right", "1px solid var(--lumo-contrast-10pct)");

        // Add navigation items
        String[] menuItems = {"Dashboard", "Orders", "Customers", "Products", "Documents", "Tasks", "Analytics"};

        for (String item : menuItems) {
            Button navButton = new Button(item);
            navButton.setWidthFull();
            navButton.getStyle()
                    .set("justify-content", "left")
                    .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                    .set("border-radius", "0")
                    .set("cursor", "pointer");

            navButton.addClickListener(e -> {
                Notification.show("Navigating to: " + item);
                // Add actual navigation logic here
            });

            sidebar.add(navButton);
        }

        return sidebar;
    }

}
