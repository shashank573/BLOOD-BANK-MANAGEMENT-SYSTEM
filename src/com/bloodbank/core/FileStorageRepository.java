package com.bloodbank.core;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileStorageRepository {

    private static final String INVENTORY_FILE = "inventory.txt";
    private static final String DONOR_FILE = "donors.txt";
    private static final String RECEIVER_FILE = "receivers.txt";

    private static final String[] BLOOD_GROUPS = {
            "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"
    };

    public synchronized void initializeStorage() {
        initializeInventoryFile();
        ensureFileExists(DONOR_FILE);
        ensureFileExists(RECEIVER_FILE);
    }

    private void initializeInventoryFile() {
        File file = new File(INVENTORY_FILE);
        if (file.exists()) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String group : BLOOD_GROUPS) {
                writer.write(formatInventoryLine(group));
                writer.newLine();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Unable to initialize inventory storage", ex);
        }
    }

    private void ensureFileExists(String fileName) {
        File file = new File(fileName);

        if (file.exists()) {
            return;
        }

        try {
            if (!file.createNewFile()) {
                throw new IOException("File creation failed");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Unable to create file: " + fileName, ex);
        }
    }

    public synchronized void appendLine(String fileName, String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(content);
            writer.newLine();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write to file: " + fileName, ex);
        }
    }

    public synchronized List<String> readAll(String fileName) {
        List<String> lines = new ArrayList<>();

        File file = new File(fileName);
        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                if (!currentLine.trim().isEmpty()) {
                    lines.add(currentLine);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read file: " + fileName, ex);
        }

        return lines;
    }

    public synchronized void overwriteInventory(List<String> updatedLines) {
        if (updatedLines == null) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(INVENTORY_FILE))) {
            for (String line : updatedLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to overwrite inventory data", ex);
        }
    }

    private String formatInventoryLine(String bloodGroup) {
        return bloodGroup + ",0,0,0";
    }

    public String getInventoryFile() {
        return INVENTORY_FILE;
    }

    public String getDonorFile() {
        return DONOR_FILE;
    }

    public String getReceiverFile() {
        return RECEIVER_FILE;
    }
}