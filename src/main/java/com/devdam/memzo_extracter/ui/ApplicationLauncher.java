package com.devdam.memzo_extracter.ui;

import com.devdam.memzo_extracter.service.CsvService;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class ApplicationLauncher {
    
    public static void main(String[] args) {
        // Set system properties for better UI experience
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Memzo Extracter");
        System.setProperty("sun.java2d.uiScale", "1.0");
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Set FlatLaf look and feel
                UIManager.setLookAndFeel(new FlatLightLaf());
                
                // Create and show the application
                CsvService csvService = new CsvService();
                MainWindow mainWindow = new MainWindow(csvService);
                mainWindow.showWindow();
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error starting application: " + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
