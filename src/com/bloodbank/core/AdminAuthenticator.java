package com.bloodbank.core;

import javax.swing.*;
import java.awt.*;

public class AdminAuthenticator {

    private static final String ADMIN_ID = "shashank";
    private static final String ADMIN_PASSWORD = "123456";

    public static boolean authenticate(Component parent) {
        LoginForm loginForm = buildLoginForm();

        int result = displayLoginDialog(parent, loginForm.getContainer());
        if (result != JOptionPane.OK_OPTION) {
            return false;
        }

        String userId = loginForm.getUserId();
        String password = loginForm.getPassword();

        return processAuthentication(userId, password, parent);
    }

    private static LoginForm buildLoginForm() {
        JTextField userIdField = new JTextField(12);
        JPasswordField passwordField = new JPasswordField(12);

        JPanel container = new JPanel(new GridLayout(2, 2, 6, 6));
        container.add(new JLabel("Admin ID:"));
        container.add(userIdField);
        container.add(new JLabel("Password:"));
        container.add(passwordField);

        return new LoginForm(container, userIdField, passwordField);
    }

    private static int displayLoginDialog(Component parent, JPanel content) {
        return JOptionPane.showConfirmDialog(
                parent,
                content,
                "Admin Authentication Required",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
    }

    private static boolean processAuthentication(String userId, String password, Component parent) {
        if (isInputInvalid(userId, password)) {
            showErrorDialog(parent, "Credentials cannot be empty");
            return false;
        }

        if (!credentialsMatch(userId, password)) {
            showErrorDialog(parent, "Unauthorized access");
            return false;
        }

        return true;
    }

    private static boolean isInputInvalid(String userId, String password) {
        return userId == null || password == null ||
                userId.trim().isEmpty() || password.trim().isEmpty();
    }

    private static boolean credentialsMatch(String userId, String password) {
        return ADMIN_ID.equals(userId) && ADMIN_PASSWORD.equals(password);
    }

    private static void showErrorDialog(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "Access Denied",
                JOptionPane.ERROR_MESSAGE);
    }

    private static class LoginForm {
        private final JPanel container;
        private final JTextField userIdField;
        private final JPasswordField passwordField;

        LoginForm(JPanel container, JTextField userIdField, JPasswordField passwordField) {
            this.container = container;
            this.userIdField = userIdField;
            this.passwordField = passwordField;
        }

        JPanel getContainer() {
            return container;
        }

        String getUserId() {
            return userIdField.getText();
        }

        String getPassword() {
            return new String(passwordField.getPassword());
        }
    }
}