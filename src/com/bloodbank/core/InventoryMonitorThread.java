package com.bloodbank.core;

public class InventoryMonitorThread extends Thread {
    private volatile boolean running = true;

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(10000);
                System.out.println("[Background Thread] Monitoring System: Inventory check active in background...");
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted.");
                running = false;
            }
        }
    }
}