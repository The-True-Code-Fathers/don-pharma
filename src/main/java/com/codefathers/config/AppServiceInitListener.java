package com.codefathers.config;

import com.codefathers.service.AuthService;
import com.codefathers.util.DataInitializer;
import com.codefathers.util.HibernateUtil;
import com.codefathers.view.LoginView;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@SuppressWarnings("serial")
public class AppServiceInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {

        System.out.println("Calling HibernateUtil.getSessionFactory() to ensure initialization...");
        try {
            HibernateUtil.getSessionFactory();
        } catch (ExceptionInInitializerError e) {
            // This catches the specific error thrown by HibernateUtil if buildSessionFactory() fails
            System.err.println("!!! Critical: Hibernate SessionFactory failed to initialize. Application cannot proceed. !!!");
            // You might want to throw a RuntimeException here to prevent the application from starting in a bad state.
            // For now, we'll just log.
        } catch (Exception e) {
            System.err.println("!!! An unexpected error occurred during Hibernate SessionFactory initialization !!!");
        }

        // This should only happen AFTER Hibernate is confirmed to be initialized
        System.out.println("Calling DataInitializer.initialize() to set up initial data...");
        try {
            DataInitializer.initialize();
            System.out.println("DataInitializer finished.");
        } catch (Exception e) {
            System.err.println("!!! Error during DataInitializer.initialize() !!!");
            e.printStackTrace();
        }

        event.getSource().addUIInitListener(uiInitEvent -> {
            uiInitEvent.getUI().addBeforeEnterListener(this::beforeEnter);
        });
    }

    /**
     * This method is called before every navigation in the application.
     * It enforces authentication for all views except those marked with @AnonymousAllowed.
     */
    private void beforeEnter(BeforeEnterEvent event) {
        // Check if the target view (the one the user is trying to navigate to)
        // has the @AnonymousAllowed annotation.
        boolean isAnonymousAllowed = event.getNavigationTarget().isAnnotationPresent(AnonymousAllowed.class);

        // Case 1: User is NOT logged in AND the target view is NOT allowed for anonymous users.
        if (!AuthService.isLoggedIn() && !isAnonymousAllowed) {
            event.forwardTo(LoginView.class);
            System.out.println("--- AppServiceInitListener: Authentication Required. Rerouting to LoginView ---");

        }
        // Case 2: User IS logged in AND the target view IS allowed for anonymous users.
        // Case 3: If logged in AND target is NOT anonymous (i.e., protected view), allow navigation.
        // Case 4: If NOT logged in AND target IS anonymous (i.e., LoginView), allow navigation.

    }
}
