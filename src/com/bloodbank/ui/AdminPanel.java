package com.bloodbank.ui;

import com.bloodbank.core.DataStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JPanel {

    private DefaultTableModel inventoryTableModel;
    private DefaultTableModel donorTableModel;
    private DefaultTableModel receiverTableModel;

    private final BloodBankApp application;

    public AdminPanel(BloodBankApp app) {
        this.application = app;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTabbedSection(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("ADMINISTRATOR DASHBOARD");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(179, 0, 0));

        JButton logoutButton = new JButton("Logout to Menu");
        logoutButton.setBackground(new Color(50, 50, 50));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> handleLogout());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        return headerPanel;
    }

    private JPanel dashboardPanel;

    private JTabbedPane createTabbedSection() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Dashboard", createDashboardTab());
        tabbedPane.addTab("Full Inventory", createInventoryTab());
        tabbedPane.addTab("Donor Records", createDonorTab());
        tabbedPane.addTab("Receiver Records", createReceiverTab());

        return tabbedPane;
    }

    private JScrollPane createDashboardTab() {
        dashboardPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return new JScrollPane(dashboardPanel);
    }

    private JScrollPane createInventoryTab() {
        inventoryTableModel = new DefaultTableModel(
                new String[] { "Blood Group", "RBC Units", "Plasma Units", "Platelet Units" }, 0);

        JTable table = new JTable(inventoryTableModel);
        return new JScrollPane(table);
    }

    private JScrollPane createDonorTab() {
        donorTableModel = new DefaultTableModel(
                new String[] { "Name", "Age", "Address", "Phone", "Group", "Weight", "Haemoglobin" }, 0);

        JTable table = new JTable(donorTableModel);
        return new JScrollPane(table);
    }

    private JScrollPane createReceiverTab() {
        receiverTableModel = new DefaultTableModel(
                new String[] { "Name/Hospital", "Age/ID", "Address", "Phone", "Group Required", "Component",
                        "Units Issued" },
                0);

        JTable table = new JTable(receiverTableModel);
        return new JScrollPane(table);
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton refreshButton = new JButton("Refresh Records");
        refreshButton.setBackground(new Color(0, 102, 204));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> refreshData());

        footerPanel.add(refreshButton);

        return footerPanel;
    }

    private void handleLogout() {
        application.showMainMenu();
    }

    public void refreshData() {
        clearAllTables();
        if (dashboardPanel != null) {
            dashboardPanel.removeAll();
        }

        loadInventoryData();
        loadDonorData();
        loadReceiverData();

        if (dashboardPanel != null) {
            dashboardPanel.revalidate();
            dashboardPanel.repaint();
        }
    }

    private void clearAllTables() {
        inventoryTableModel.setRowCount(0);
        donorTableModel.setRowCount(0);
        receiverTableModel.setRowCount(0);
    }

    private void loadInventoryData() {
        List<String> lines = DataStore.readAllLines("inventory.txt");
        if (lines == null)
            return;

        for (String line : lines) {
            String[] parts = safeSplit(line);

            if (parts.length == 4) {
                inventoryTableModel.addRow(new Object[] {
                        parts[0], parts[1], parts[2], parts[3]
                });

                if (dashboardPanel != null) {
                    int rbcUnits = 0;
                    try {
                        rbcUnits = Integer.parseInt(parts[1]);
                    } catch (Exception ignored) {
                    }

                    JPanel pBarPanel = new JPanel(new BorderLayout(10, 0));
                    JLabel label = new JLabel(String.format("%-10s RBC Stock:", parts[0]));
                    label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    label.setPreferredSize(new Dimension(120, 30));

                    JProgressBar pBar = new JProgressBar(0, 100);
                    pBar.setValue(rbcUnits);
                    pBar.setStringPainted(true);
                    pBar.setForeground(new Color(179, 0, 0));

                    pBarPanel.add(label, BorderLayout.WEST);
                    pBarPanel.add(pBar, BorderLayout.CENTER);
                    dashboardPanel.add(pBarPanel);
                }
            }
        }
    }

    private void loadDonorData() {
        List<String> lines = DataStore.readAllLines("donors.txt");
        if (lines == null)
            return;

        for (String line : lines) {
            String[] parts = safeSplit(line);

            if (parts.length >= 7) {
                donorTableModel.addRow(new Object[] {
                        parts[0], parts[1], parts[2],
                        parts[3], parts[4], parts[5], parts[6]
                });
            }
        }
    }

    private void loadReceiverData() {
        List<String> lines = DataStore.readAllLines("receivers.txt");
        if (lines == null)
            return;

        for (String line : lines) {
            String[] parts = safeSplit(line);

            if (parts.length >= 7) {
                receiverTableModel.addRow(new Object[] {
                        parts[0], parts[1], parts[2],
                        parts[3], parts[4], parts[5], parts[6]
                });
            }
        }
    }

    private String[] safeSplit(String line) {
        if (line == null || line.trim().isEmpty()) {
            return new String[0];
        }
        return line.split(",");
    }
}