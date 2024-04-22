package com.nedap.university;

import com.nedap.university.Server;

public class Main {

    private static boolean keepAlive = true;
    private static boolean running = false;

    // PORT nr can be adjusted
    private static String[] PORT = new String[]{ "8080"};

    private Main() {}

    public static void main(String[] args) {
        running = true;
        System.out.println("Hello, you've started Renske's Pi Server!");
        Server.main(PORT);

        initShutdownHook();

        while (keepAlive) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Stopped");
        running = false;
    }

    private static void initShutdownHook() {
        final Thread shutdownThread = new Thread() {
            @Override
            public void run() {
                keepAlive = false;
                while (running) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }
}
