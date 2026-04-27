package com.bloodbank.ui;

import com.bloodbank.core.AdminAuthenticator;
import com.bloodbank.core.DataStore;

import javax.swing.*;
import java.awt.*;

public class ReceiverPanel extends JPanel {

    private final BloodBankApp app;
    private final boolean hospitalMode;

    private JTextField nameField, ageField, addressField, phoneField, unitsField;
    private JComboBox<String> bloodGroupBox, componentBox;
    private JLabel searchStatusLabel;

    public ReceiverPanel(BloodBankApp app, boolean isHospital) {
        this.app = app;
        this.hospitalMode = isHospital;

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createTopSection(), BorderLayout.NORTH);
        add(createFormSection(), BorderLayout.CENTER);
    }

    private JPanel createTopSection() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(createHeaderPanel());
        container.add(createSearchPanel());

        return container;
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton backButton = new JButton("<- Back to Menu");
        backButton.setBackground(new Color(179, 0, 0));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> app.showMainMenu());

        String titleText = hospitalMode ? "HOSPITAL BULK REQUEST" : "RECEIVER REQUEST";
        JLabel title = new JLabel("  " + titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(179, 0, 0));

        panel.add(backButton);
        panel.add(title);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Search Active Inventory"));

        JComboBox<String> searchGroupBox = new JComboBox<>(getBloodGroups());
        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(0, 102, 204));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        searchStatusLabel = new JLabel("Select a blood group and click search to view real-time availability.");
        searchStatusLabel.setForeground(Color.BLUE);

        searchButton.addActionListener(e -> handleSearch(searchGroupBox));

        panel.add(new JLabel("Select Blood Group:"));
        panel.add(searchGroupBox);
        panel.add(searchButton);
        panel.add(searchStatusLabel);

        return panel;
    }

    private JPanel createFormSection() {
        JPanel panel = new JPanel(new GridLayout(8, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        nameField = new JTextField();
        ageField = new JTextField();
        addressField = new JTextField();
        phoneField = new JTextField();

        bloodGroupBox = new JComboBox<>(getBloodGroups());
        componentBox = new JComboBox<>(new String[] { "RBC", "Plasma", "Platelets" });

        addField(panel, "Name (Patient/Hospital):", nameField);
        addField(panel, "Age/ID:", ageField);
        addField(panel, "Address:", addressField);
        addField(panel, "Phone Number:", phoneField);
        addField(panel, "Blood Group Required:", bloodGroupBox);
        addField(panel, "Blood Component Required:", componentBox);

        if (hospitalMode) {
            unitsField = new JTextField("1");
            addField(panel, "Units Required (Bulk):", unitsField);
        }

        JButton submitButton = new JButton("Submit Blood Request");
        submitButton.setBackground(new Color(179, 0, 0));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitButton.setFocusPainted(false);
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitButton.addActionListener(e -> handleSubmit());

        panel.add(submitButton);

        return panel;
    }

    private void addField(JPanel panel, String label, JComponent field) {
        panel.add(new JLabel(label));
        panel.add(field);
    }

    private String[] getBloodGroups() {
        return new String[] { "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-" };
    }

    private void handleSearch(JComboBox<String> searchBox) {
        String selectedGroup = (String) searchBox.getSelectedItem();
        if (selectedGroup != null) {
            searchStatusLabel.setText(DataStore.searchAvailability(selectedGroup));
        }
    }

    private void handleSubmit() {
        try {
            int unitsRequested = resolveUnits();

            if (!AdminAuthenticator.authenticate(this)) {
                return;
            }

            String group = (String) bloodGroupBox.getSelectedItem();
            String component = (String) componentBox.getSelectedItem();

            if (group == null || component == null) {
                showError("Invalid selection.");
                return;
            }

            boolean success = DataStore.requestInventory(group, component, unitsRequested);

            if (success) {
                saveReceiverData(group, component, unitsRequested);
                showSuccess();
                resetForm();
                app.showMainMenu();
            } else {
                showFailure();
            }

        } catch (NumberFormatException ex) {
            showError("Invalid form data.");
        } catch (Exception ex) {
            showError("Unexpected error occurred.");
        }
    }

    private int resolveUnits() {
        if (!hospitalMode) {
            return 1;
        }

        int units = Integer.parseInt(unitsField.getText().trim());

        if (units <= 0) {
            throw new NumberFormatException();
        }

        return units;
    }

    private void saveReceiverData(String group, String component, int units) {
        String record = String.format("%s,%s,%s,%s,%s,%s,%d",
                nameField.getText().trim(),
                ageField.getText().trim(),
                addressField.getText().trim(),
                phoneField.getText().trim(),
                group,
                component,
                units);

        DataStore.addReceiver(record);
    }

    private void showSuccess() {
        JOptionPane.showMessageDialog(
                this,
                "Request approved! Please collect the blood units at the counter.\nWishing you or your patient a speedy recovery!",
                "Blood Issued Successfully",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showFailure() {
        String message = hospitalMode ? "We're sorry, but we don't have enough stock to fulfill this bulk request." 
                                      : "We're currently low on this blood group. Please check again soon.";

        JOptionPane.showMessageDialog(
                this,
                message,
                "Stock Unavailable",
                JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void resetForm() {
        nameField.setText("");
        ageField.setText("");
        addressField.setText("");
        phoneField.setText("");

        if (hospitalMode && unitsField != null) {
            unitsField.setText("1");
        }

        bloodGroupBox.setSelectedIndex(0);
        componentBox.setSelectedIndex(0);

        searchStatusLabel.setText("Select a blood group and click search to view real-time availability.");
    }
}