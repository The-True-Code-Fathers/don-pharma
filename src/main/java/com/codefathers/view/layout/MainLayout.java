package com.codefathers.view.layout;

import com.codefathers.view.DashboardView;
import com.codefathers.view.EmployeeView;
import com.codefathers.view.FinancialView;
import com.codefathers.view.OrderView;
import com.codefathers.view.PaymentView;
import com.codefathers.view.ProductsView;
import com.codefathers.view.PurchaseOrderView;
import com.codefathers.view.ShippingAreaView;
import com.codefathers.view.ShippingOrderView;
import com.codefathers.view.ShippingProviderView;
import com.codefathers.view.StorageView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Layout
public class MainLayout extends AppLayout {
    private Button themeToggleButton;
    private boolean darkModeEnabled = false;

    public MainLayout() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("⚕\uFE0F Don Pharma");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        themeToggleButton = new Button(VaadinIcon.SUN_O.create());
        themeToggleButton.addClickListener(event -> {
            setDarkMode(!darkModeEnabled);

        });

        themeToggleButton.getStyle().set("margin-right", "0.5em");
        themeToggleButton.getStyle().set("background", "transparent");

        setDarkMode(darkModeEnabled);

        Avatar avatarBasic = new Avatar();
                avatarBasic.getStyle().set("margin-right", "1em");

        Button avatarButton = new Button(avatarBasic);

        avatarButton.getStyle().set("background", "transparent");
        avatarButton.getStyle().set("border", "none");
        avatarButton.getStyle().set("padding", "0");
        avatarButton.getStyle().set("min-width", "unset");

        avatarButton.addClickListener(buttonClickEvent -> {
            openUserDialog();
        });

        HorizontalLayout navbarRightContext = new HorizontalLayout(avatarButton);
        navbarRightContext.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        navbarRightContext.setAlignItems(FlexComponent.Alignment.CENTER);
        navbarRightContext.setSpacing(true);

        HorizontalLayout navbarContent = new HorizontalLayout(title, navbarRightContext);
        navbarContent.setWidthFull();
        navbarContent.setAlignItems(FlexComponent.Alignment.CENTER);
        navbarContent.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navbarContent.setSpacing(true);

        addToNavbar(toggle, navbarContent);

        SideNav sideNav = new SideNav();

        var dashboardLink = new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD.create());
        var employeeLink = new SideNavItem("Employees", EmployeeView.class, VaadinIcon.GROUP.create());
        var productLink = new SideNavItem("Products", ProductsView.class, VaadinIcon.STOCK.create());
        var orderLink = new SideNavItem("Sales Order", OrderView.class, VaadinIcon.CLIPBOARD.create());
        var shippingLink = new SideNavItem("Shipping");
        var goalLink = new SideNavItem("Goals");

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

        var financialLink = new SideNavItem("Financial", FinancialView.class, VaadinIcon.MONEY.create());

        var paymentLink = new SideNavItem("Payment", PaymentView.class, VaadinIcon.CASH.create());

        goalLink = new SideNavItem("Goals", GoalsView.class, VaadinIcon.ANGLE_DOUBLE_UP.create());
        
        sideNav.addItem(
                dashboardLink,
                employeeLink,
                paymentLink,
                financialLink,
                productLink,
                shippingLink,
                orderLink,
                purchaseOrderLink,
                storageLink,
                goalLink
        );

        Scroller scroller = new Scroller(sideNav);
        scroller.setClassName(LumoUtility.Padding.SMALL);
        addToDrawer(scroller);
    }

    private void setDarkMode(boolean enabled) {
        this.darkModeEnabled = enabled;

        UI.getCurrent().getPage().executeJs(
                "document.documentElement.setAttribute('theme', $0);" +
                        "localStorage.setItem('dark-mode-enabled', $1);",
                enabled ? "dark" : "light",
                enabled
        );

        themeToggleButton.setIcon(enabled ? VaadinIcon.MOON_O.create() : VaadinIcon.SUN_O.create());
    }

//    @Override
//    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
////        UI.getCurrent().getPage().executeJs(
////                "localStorage.removeItem('dark-mode-enabled');"
////        );
//
//        UI.getCurrent().getPage().executeJs(
//                "const localSetting = localStorage.getItem('dark-mode-enabled');" +
//                        "const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;" +
//                        "const useDark = localSetting === null ? prefersDark : localSetting === 'true';" +
//                        "document.documentElement.setAttribute('theme', useDark ? 'dark' : 'light');" +
//                        "return useDark;"
//        ).then(Boolean.class, this::setDarkMode);
//    }

    private void openUserDialog() {
        Dialog userDialog = new Dialog();
        userDialog.setHeaderTitle("Configurações do Usuário");
        userDialog.setCloseOnEsc(true);
        userDialog.setCloseOnOutsideClick(true);

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);
        dialogContent.setSpacing(true);
        dialogContent.setWidth("250px");
        dialogContent.setAlignItems(FlexComponent.Alignment.CENTER);

        Div themeSection = new Div();
        themeSection.getStyle().set("padding", "8px 0");

        HorizontalLayout themeLayout = new HorizontalLayout();
        themeLayout.setWidthFull();
        themeLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        themeLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Span themeLabel = new Span("Tema");
        themeLabel.getStyle().set("font-weight", "500");

        Button themeToggle = new Button();
        themeToggle.setIcon(darkModeEnabled ? VaadinIcon.MOON_O.create() : VaadinIcon.SUN_O.create());
        themeToggle.setText(darkModeEnabled ? "Escuro" : "Claro");

        themeToggle.addClickListener(event -> {
            setDarkMode(!darkModeEnabled);

            themeToggle.setIcon(darkModeEnabled ? VaadinIcon.MOON_O.create() : VaadinIcon.SUN_O.create());
            themeToggle.setText(darkModeEnabled ? "Escuro" : "Claro");

            UI.getCurrent().getPage().executeJs("localStorage.setItem('dark-mode-enabled', $0);", darkModeEnabled);
        });
        themeToggle.getStyle().set("background", "transparent");
        themeToggle.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        themeToggle.getStyle().set("border-radius", "6px");

        themeLayout.add(themeLabel, themeToggle);
        themeSection.add(themeLayout);

        Hr separator = new Hr();
        separator.getStyle().set("margin", "8px 0");

        Button logoutButton = new Button("Sair", VaadinIcon.SIGN_OUT.create());
        logoutButton.addClickListener(event -> {
            userDialog.close();
            handleLogout();
        });

        logoutButton.setWidth("150px");  // largura fixa
        HorizontalLayout buttonWrapper = new HorizontalLayout(logoutButton);
        buttonWrapper.setWidthFull();
        buttonWrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        logoutButton.getStyle().set("background", "transparent");
        logoutButton.getStyle().set("border", "1px solid var(--lumo-error-color)");
        logoutButton.getStyle().set("color", "var(--lumo-error-color)");
        logoutButton.getStyle().set("border-radius", "6px");

        logoutButton.getElement().addEventListener("mouseenter", e -> {
            logoutButton.getStyle().set("background", "var(--lumo-error-color)");
            logoutButton.getStyle().set("color", "white");
        });

        logoutButton.getElement().addEventListener("mouseleave", e -> {
            logoutButton.getStyle().set("background", "transparent");
            logoutButton.getStyle().set("color", "var(--lumo-error-color)");
        });

        dialogContent.add(themeSection, separator, buttonWrapper);
        userDialog.add(dialogContent);
        userDialog.open();
    }

    private void handleLogout() {
        UI.getCurrent().getSession().close();
        UI.getCurrent().navigate("login");
    }
}