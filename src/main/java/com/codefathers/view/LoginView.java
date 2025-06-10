package com.codefathers.view;

import com.codefathers.model.entity.SystemUser;
import com.codefathers.repository.SystemUserRepositoryImpl;
import com.codefathers.service.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.Optional;

@Route(value = "login", autoLayout = false)
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final LoginForm loginForm = new LoginForm();

    // TODO: handle this via dependency injection
    private final AuthService authService = new AuthService(new SystemUserRepositoryImpl());

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        loginForm.addLoginListener(this::authenticate);

        add(loginForm);
    }

    private void authenticate(AbstractLogin.LoginEvent loginEvent) {
        String username = loginEvent.getUsername();
        String password = loginEvent.getPassword();

        Optional<SystemUser> user = authService.authenticate(username, password);

        if (user.isPresent()) {
            UI.getCurrent().navigate(MainView.class); // Go to Main View
            return;
        }
        // Login failed
        loginForm.setError(true);
        Notification.show("Invalid credentials", 3000, Notification.Position.MIDDLE);
    }

}
