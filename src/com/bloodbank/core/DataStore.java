package com.bloodbank.core;

import java.util.List;

public class DataStore {

    private static final FileStorageRepository storage = new FileStorageRepository();

    static {
        storage.initializeStorage();
    }

    public static void addDonor(String donorDetails) {
        if (isInvalidInput(donorDetails))
            return;
        storage.appendLine(storage.getDonorFile(), donorDetails);
    }

    public static void addReceiver(String receiverDetails) {
        if (isInvalidInput(receiverDetails))
            return;
        storage.appendLine(storage.getReceiverFile(), receiverDetails);
    }

    public static List<String> readAllLines(String fileName) {
        return storage.readAll(fileName);
    }

    public static void updateInventory(String bloodGroup, int rbcUnits, int plasmaUnits, int plateletUnits) {
        List<String> inventory = storage.readAll(storage.getInventoryFile());
        if (inventory == null || inventory.isEmpty())
            return;

        for (int i = 0; i < inventory.size(); i++) {
            String[] record = parseLine(inventory.get(i));
            if (isMatchingBloodGroup(record, bloodGroup)) {
                int updatedRbc = parseInt(record[1]) + rbcUnits;
                int updatedPlasma = parseInt(record[2]) + plasmaUnits;
                int updatedPlatelets = parseInt(record[3]) + plateletUnits;

                inventory.set(i, buildLine(bloodGroup, updatedRbc, updatedPlasma, updatedPlatelets));
                break;
            }
        }

        storage.overwriteInventory(inventory);
    }

    public static String searchAvailability(String bloodGroup) {
        List<String> inventory = storage.readAll(storage.getInventoryFile());
        if (inventory == null)
            return "No stock info found.";

        for (String line : inventory) {
            String[] record = parseLine(line);
            if (isMatchingBloodGroup(record, bloodGroup)) {
                return formatStockMessage(record);
            }
        }

        return "No stock info found.";
    }

    public static boolean requestInventory(String bloodGroup, String component, int requiredUnits) {
        List<String> inventory = storage.readAll(storage.getInventoryFile());
        if (inventory == null || requiredUnits <= 0)
            return false;

        boolean fulfilled = false;

        for (int i = 0; i < inventory.size(); i++) {
            String[] record = parseLine(inventory.get(i));
            if (!isMatchingBloodGroup(record, bloodGroup))
                continue;

            int rbc = parseInt(record[1]);
            int plasma = parseInt(record[2]);
            int platelets = parseInt(record[3]);

            String comp = component == null ? "" : component.toLowerCase();

            switch (comp) {
                case "rbc" -> {
                    if (rbc >= requiredUnits) {
                        rbc -= requiredUnits;
                        fulfilled = true;
                    }
                }
                case "plasma" -> {
                    if (plasma >= requiredUnits) {
                        plasma -= requiredUnits;
                        fulfilled = true;
                    }
                }
                case "platelets" -> {
                    if (platelets >= requiredUnits) {
                        platelets -= requiredUnits;
                        fulfilled = true;
                    }
                }
                default -> {
                }
            }

            if (fulfilled) {
                inventory.set(i, buildLine(bloodGroup, rbc, plasma, platelets));
                break;
            }
        }

        if (fulfilled) {
            storage.overwriteInventory(inventory);
        }

        return fulfilled;
    }

    private static String[] parseLine(String line) {
        return line != null ? line.split(",") : new String[0];
    }

    private static boolean isMatchingBloodGroup(String[] record, String bloodGroup) {
        return record.length >= 4 && record[0].equalsIgnoreCase(bloodGroup);
    }

    private static String buildLine(String bloodGroup, int rbc, int plasma, int platelets) {
        return String.join(",", bloodGroup,
                String.valueOf(rbc),
                String.valueOf(plasma),
                String.valueOf(platelets));
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String formatStockMessage(String[] record) {
        return String.format(
                "Current Stock: %s RBC | %s Plasma | %s Platelets",
                record[1], record[2], record[3]);
    }

    private static boolean isInvalidInput(String input) {
        return input == null || input.trim().isEmpty();
    }
}