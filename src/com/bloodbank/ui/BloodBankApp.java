package com.bloodbank.ui;

import com.bloodbank.core.AdminAuthenticator;
import com.bloodbank.core.InventoryMonitorThread;

import javax.swing.*;
import java.awt.*;

public class BloodBankApp extends JFrame {

    private JPanel rootPanel;
    private CardLayout viewSwitcher;

    private static final String MENU_VIEW = "MENU";
    private static final String DONOR_VIEW = "DONOR";
    private static final String RECEIVER_VIEW = "RECEIVER";
    private static final String HOSPITAL_VIEW = "HOSPITAL";
    private static final String ADMIN_VIEW = "ADMIN";

    public BloodBankApp() {
        configureFrame();
        initializeBackgroundServices();
        initializeUI();
    }

    private void configureFrame() {
        setTitle("Blood Bank Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initializeBackgroundServices() {
        InventoryMonitorThread monitorThread = new InventoryMonitorThread();
        monitorThread.start();
    }

    private void initializeUI() {
        viewSwitcher = new CardLayout();
        rootPanel = new JPanel(viewSwitcher);

        registerViews();

        add(rootPanel);
        showMainMenu();
    }

    private void registerViews() {
        rootPanel.add(buildMainMenuPanel(), MENU_VIEW);
        rootPanel.add(new DonorPanel(this), DONOR_VIEW);
        rootPanel.add(new ReceiverPanel(this, false), RECEIVER_VIEW);
        rootPanel.add(new ReceiverPanel(this, true), HOSPITAL_VIEW);
        rootPanel.add(new AdminPanel(this), ADMIN_VIEW);
    }

    private JPanel buildMainMenuPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 250));

        GridBagConstraints gbc = createDefaultConstraints();

        addTitle(panel, gbc);
        addMenuButtons(panel, gbc);

        return panel;
    }

    private GridBagConstraints createDefaultConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        return gbc;
    }

    private void addTitle(JPanel panel, GridBagConstraints gbc) {
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 30, 10);
        JLabel title = new JLabel("Blood Bank Menu System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(179, 0, 0));
        panel.add(title, gbc);
        gbc.insets = new Insets(10, 10, 10, 10);
    }

    private void addMenuButtons(JPanel panel, GridBagConstraints gbc) {
        gbc.gridy = 1;
        panel.add(createButton("1. Register to Donate Blood", e -> switchView(DONOR_VIEW)), gbc);

        gbc.gridy = 2;
        panel.add(createButton("2. Request Blood for Patient", e -> switchView(RECEIVER_VIEW)), gbc);

        gbc.gridy = 3;
        panel.add(createButton("3. Hospital Bulk Request", e -> switchView(HOSPITAL_VIEW)), gbc);

        gbc.gridy = 4;
        panel.add(createButton("4. Administrator Portal", e -> handleAdminAccess()), gbc);

        gbc.gridy = 5;
        panel.add(createButton("5. Exit Application", e -> exitApplication()), gbc);
    }

    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(179, 0, 0));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(10, 20, 10, 20));
        return button;
    }

    private void switchView(String viewName) {
        viewSwitcher.show(rootPanel, viewName);
    }

    private void handleAdminAccess() {
        try {
            boolean authenticated = AdminAuthenticator.authenticate(this);

            if (!authenticated) {
                return;
            }

            refreshAdminPanel();
            switchView(ADMIN_VIEW);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to open admin panel. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAdminPanel() {
        for (Component component : rootPanel.getComponents()) {
            if (component instanceof AdminPanel adminPanel) {
                adminPanel.refreshData();
            }
        }
    }

    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit the Blood Bank System?",
                "Exit Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public void showMainMenu() {
        switchView(MENU_VIEW);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        SwingUtilities.invokeLater(() -> {
            BloodBankApp app = new BloodBankApp();
            app.setVisible(true);
        });
    }
}