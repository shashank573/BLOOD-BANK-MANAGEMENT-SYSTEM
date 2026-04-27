package com.bloodbank.ui;

import com.bloodbank.core.AdminAuthenticator;
import com.bloodbank.core.DataStore;
import com.bloodbank.core.InvalidEligibilityException;

import javax.swing.*;
import java.awt.*;

public class DonorPanel extends JPanel {

    private final BloodBankApp app;

    private JTextField nameField, ageField, addressField, phoneField;
    private JTextField weightField, haemoglobinField, daysField;

    private JComboBox<String> bloodGroupBox, diseaseBox, medicationBox, foodBox;

    public DonorPanel(BloodBankApp app) {
        this.app = app;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton backButton = new JButton("<- Back to Menu");
        backButton.setBackground(new Color(179, 0, 0));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> app.showMainMenu());

        JLabel title = new JLabel("  DONOR HEALTH QUESTIONNAIRE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(179, 0, 0));

        headerPanel.add(backButton);
        headerPanel.add(title);

        return headerPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridLayout(12, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        nameField = addField(formPanel, "Name:");
        ageField = addField(formPanel, "Age:");
        addressField = addField(formPanel, "Address:");
        phoneField = addField(formPanel, "Phone Number:");

        bloodGroupBox = addComboBox(formPanel, "Blood Group:",
                new String[] { "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-" });

        daysField = addField(formPanel, "Days since last donation:");
        daysField.setText("90");

        diseaseBox = addComboBox(formPanel, "Any disease (HIV/AIDS):",
                new String[] { "No", "Yes" });

        medicationBox = addComboBox(formPanel, "Recent Medication (last 24 hrs):",
                new String[] { "No", "Yes" });

        weightField = addField(formPanel, "Weight (kg):");
        haemoglobinField = addField(formPanel, "Haemoglobin Level (g/dL):");

        foodBox = addComboBox(formPanel, "Food intake in last 6 hours:",
                new String[] { "Yes", "No" });

        formPanel.add(createSubmitButton());

        return formPanel;
    }

    private JTextField addField(JPanel panel, String label) {
        panel.add(new JLabel(label));
        JTextField field = new JTextField();
        panel.add(field);
        return field;
    }

    private JComboBox<String> addComboBox(JPanel panel, String label, String[] values) {
        panel.add(new JLabel(label));
        JComboBox<String> comboBox = new JComboBox<>(values);
        panel.add(comboBox);
        return comboBox;
    }

    private JButton createSubmitButton() {
        JButton submitButton = new JButton("Verify & Donate Life-saving Blood");
        submitButton.setBackground(new Color(179, 0, 0));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitButton.setFocusPainted(false);
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitButton.addActionListener(e -> handleDonation());
        return submitButton;
    }

    private void handleDonation() {
        try {
            validateBasicInputs();
            validateEligibility();

            if (!AdminAuthenticator.authenticate(this)) {
                return;
            }

            processSuccessfulDonation();
            showSuccessMessage();
            resetForm();

        } catch (InvalidEligibilityException ex) {
            showError("Eligibility Notice", "For your safety, we cannot accept your donation today:\n" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showError("Oops! Input Error", "Please double-check your details:\n" + ex.getMessage());
        } catch (Exception ex) {
            showError("System Error", "We encountered an unexpected issue. Please try again.");
        }
    }

    private void validateBasicInputs() {
        if (isEmpty(nameField) || isEmpty(addressField)) {
            throw new IllegalArgumentException("Fields cannot be empty.");
        }
    }

    private void validateEligibility() throws InvalidEligibilityException {
        int age = parseInteger(ageField, "Age must be a valid number.");
        int days = parseInteger(daysField, "Days must be a valid number.");

        boolean hasDisease = "Yes".equals(diseaseBox.getSelectedItem());
        boolean onMedication = "Yes".equals(medicationBox.getSelectedItem());
        boolean hasEaten = "Yes".equals(foodBox.getSelectedItem());

        if (age < 18 || days < 90 || hasDisease || onMedication || !hasEaten) {
            throw new InvalidEligibilityException(
                    "Not eligible under strict biological requirements.");
        }
    }

    private void processSuccessfulDonation() {
        String bloodGroup = (String) bloodGroupBox.getSelectedItem();

        DataStore.updateInventory(bloodGroup, 1, 1, 1);

        String donorRecord = String.format("%s,%s,%s,%s,%s,%s,%s",
                nameField.getText(),
                ageField.getText(),
                addressField.getText(),
                phoneField.getText(),
                bloodGroup,
                weightField.getText(),
                haemoglobinField.getText());

        DataStore.addDonor(donorRecord);
    }

    private void resetForm() {
        nameField.setText("");
        ageField.setText("");
        addressField.setText("");
        phoneField.setText("");
        weightField.setText("");
        haemoglobinField.setText("");
        daysField.setText("90");

        bloodGroupBox.setSelectedIndex(0);
        diseaseBox.setSelectedIndex(0);
        medicationBox.setSelectedIndex(0);
        foodBox.setSelectedIndex(0);

        app.showMainMenu();
    }

    private boolean isEmpty(JTextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private int parseInteger(JTextField field, String errorMessage) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void showSuccessMessage() {
        JOptionPane.showMessageDialog(
                this,
                "Thank you for your kindness! You are eligible to donate.\nYour contribution will help save a life today. Please proceed to the collection room.",
                "Donation Approved",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }
}