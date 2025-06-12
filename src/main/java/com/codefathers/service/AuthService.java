package com.codefathers.service;

import com.codefathers.factory.ServiceFactory;
import com.codefathers.model.entity.SystemUser;
import com.codefathers.repository.interfaces.SystemUserRepository;
import com.codefathers.util.AuthUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public class AuthService {

    private final SystemUserRepository userRepository;

    public AuthService(SystemUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<SystemUser> authenticate(String username, String password) {
        Optional<SystemUser> authenticatedUser = userRepository.findByUsername(username)
                .filter(user -> AuthUtil.PasswordUtil.verifyPassword(password, user.getPasswordHash()));

        // If authentication is successful, store the user in the session
        authenticatedUser.ifPresent(user -> {
            VaadinSession.getCurrent().setAttribute("user", user);
        });

        return authenticatedUser;
    }

    public void logout() {
        VaadinSession.getCurrent().setAttribute("user", null);
        VaadinSession.getCurrent().close(); // Invalidate the VaadinSession

        // Invalidate the underlying HTTP session if available
        HttpServletRequest request = (HttpServletRequest) VaadinServletRequest.getCurrent().getHttpServletRequest();
        try {
            if (request != null) {
                request.logout();
            }
        } catch (jakarta.servlet.ServletException e) {
            System.err.println("Error during HTTP session logout: " + e.getMessage());
        }

        UI.getCurrent().navigate("login");
    }

    public Optional<SystemUser> getCurrentUser() {
        return Optional.ofNullable((SystemUser) VaadinSession.getCurrent().getAttribute("user"));
    }

    public static boolean isLoggedIn() {
        return ServiceFactory.getAuthService().getCurrentUser().isPresent();
    }

    public boolean hasRole(String role) {
        return getCurrentUser()
                .map(SystemUser::getRole)
                .filter(userRole -> userRole.equalsIgnoreCase(role))
                .isPresent();
    }

}
