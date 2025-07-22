package com.devdam.memzo_extracter;

import com.devdam.memzo_extracter.service.CsvService;
import com.devdam.memzo_extracter.ui.MainWindow;
import com.formdev.flatlaf.FlatLightLaf;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication
public class MemzoExtracterApplication {

	public static void main(String[] args) {
		// Set system properties for better UI experience
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("apple.awt.application.name", "Memzo Extracter");
		System.setProperty("java.awt.headless", "false");
		
		// Start Spring Boot application
		ConfigurableApplicationContext context = SpringApplication.run(MemzoExtracterApplication.class, args);
		
		// Launch UI
		SwingUtilities.invokeLater(() -> {
			try {
				// Set FlatLaf look and feel
				UIManager.setLookAndFeel(new FlatLightLaf());
				
				// Get CSV service from Spring context
				CsvService csvService = context.getBean(CsvService.class);
				
				// Create and show the application
				MainWindow mainWindow = new MainWindow(csvService);
				mainWindow.showWindow();
				
				System.out.println("✅ Memzo Extracter UI started successfully!");
				
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Error starting UI: " + e.getMessage(),
						"UI Error", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		});
	}
}
