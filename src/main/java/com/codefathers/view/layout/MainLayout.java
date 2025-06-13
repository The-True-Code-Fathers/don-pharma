package com.codefathers.view.layout;

import com.codefathers.view.*;
import com.codefathers.view.PurchaseOrderView;
import com.codefathers.view.StorageView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Layout
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private Button themeToggleButton;
    private boolean darkModeEnabled = false;

    public MainLayout() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("⚕\uFE0F Don Pharma");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        // Create the theme toggle button
        themeToggleButton = new Button(VaadinIcon.SUN_O.create()); // Default to sun icon (light theme)
        themeToggleButton.addClickListener(event -> toggleTheme());
        themeToggleButton.getStyle().set("margin-right", "0.5em");
        themeToggleButton.getStyle().set("background", "transparent");

        Avatar avatarBasic = new Avatar();
        avatarBasic.getStyle().set("margin-right", "0.5em");

        Button avatarButton = new Button(avatarBasic);
        avatarButton.addClickListener(event -> {
            System.out.println("Avatar button clicked!"); // Placeholder action for demonstration
        });
        // Apply styles to make the button transparent and borderless, so it visually
        // appears as just the avatar itself, but remains clickable as a button.
        avatarButton.getStyle().set("background", "transparent");
        avatarButton.getStyle().set("border", "none");
        avatarButton.getStyle().set("padding", "0");
        avatarButton.getStyle().set("min-width", "unset"); // Prevent button from forcing a minimum width

        HorizontalLayout navbarRightContext = new HorizontalLayout(themeToggleButton, avatarButton);
        navbarRightContext.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        navbarRightContext.setAlignItems(FlexComponent.Alignment.CENTER);
        navbarRightContext.setSpacing(true);

        // Arrange title and toggle button in a horizontal layout for the navbar
        HorizontalLayout navbarContent = new HorizontalLayout(title, navbarRightContext);
        navbarContent.setWidthFull();
        navbarContent.setAlignItems(FlexComponent.Alignment.CENTER);
        navbarContent.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navbarContent.setSpacing(true); // Add spacing if needed between title and button

        addToNavbar(toggle, navbarContent); // Add the toggle and the combined content to the navbar

        SideNav sideNav = new SideNav();

        var dashboardLink = new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD.create());
        var employeeLink = new SideNavItem("Employees", EmployeeView.class, VaadinIcon.GROUP.create());
        var productLink = new SideNavItem("Products", ProductsView.class, VaadinIcon.STOCK.create());
        var orderLink = new SideNavItem("Order", OrderView.class, VaadinIcon.CLIPBOARD.create());
        var shippingLink = new SideNavItem("Shipping");

        shippingLink.setPrefixComponent(VaadinIcon.TRUCK.create());
        shippingLink.addItem(new SideNavItem("Shipping Area", ShippingAreaView.class,
                VaadinIcon.GLOBE.create()));

        shippingLink.addItem(new SideNavItem("Shipping Order",
                ShippingOrderView.class, VaadinIcon.FILE_O.create()));

        shippingLink.addItem(new SideNavItem("Shipping Provider",
                ShippingProviderView.class, VaadinIcon.USER.create()));

        var storageLink = new SideNavItem("Storage",
                StorageView.class, VaadinIcon.PACKAGE.create());

        var purchaseOrderLink = new SideNavItem("Purchase Order",
                PurchaseOrderView.class, VaadinIcon.CART.create());

        sideNav.addItem(
                dashboardLink,
                employeeLink,
                productLink,
                shippingLink,
                orderLink,
                storageLink,
                purchaseOrderLink
        );

        Scroller scroller = new Scroller(sideNav);
        scroller.setClassName(LumoUtility.Padding.SMALL);
        addToDrawer(scroller);

    }

    private void toggleTheme() {
        darkModeEnabled = !darkModeEnabled;
        var js = "document.documentElement.setAttribute('theme', $0)";
        getElement().executeJs(js, darkModeEnabled ? Lumo.DARK : Lumo.LIGHT);
        themeToggleButton.setIcon(darkModeEnabled ? VaadinIcon.MOON_O.create() : VaadinIcon.SUN_O.create());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

    }
}