package com.devdam.memzo_extracter.ui;

import com.devdam.memzo_extracter.model.SelfieDetail;
import com.devdam.memzo_extracter.service.CsvService;
import com.devdam.memzo_extracter.ui.panel.DataPanel;
import com.devdam.memzo_extracter.ui.panel.EmailRecordsPanel;
import com.devdam.memzo_extracter.ui.panel.StatsPanel;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class MainWindow extends JFrame {
    
    private final DataPanel dataPanel;
    private final StatsPanel statsPanel;
    private final EmailRecordsPanel emailRecordsPanel;
    private JTabbedPane tabbedPane;
    private boolean isDarkTheme = false;
    
    public MainWindow(CsvService csvService) {
        // Initialize panels
        dataPanel = new DataPanel(csvService, this::updateAllPanels);
        statsPanel = new StatsPanel(csvService);
        emailRecordsPanel = new EmailRecordsPanel(csvService);
        
        initializeUI();
        setupEventListeners();
    }
    
    private void initializeUI() {
        setTitle("Memzo Extracter - CSV Data Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Set application icon
        try {
            // You can replace this with your own icon
            setIconImage(createDefaultIcon());
        } catch (Exception e) {
            // Continue without icon if there's an error
        }
        
        // Create menu bar
        setJMenuBar(createMenuBar());
        
        // Create main content
        createMainContent();
        
        // Set minimum size
        setMinimumSize(new Dimension(1000, 600));
    }
    
    private void createMainContent() {
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Add tabs with icons
        tabbedPane.addTab("📊 Data Management", dataPanel);
        tabbedPane.addTab("📈 Statistics", statsPanel);
        tabbedPane.addTab("📧 Email Records", emailRecordsPanel);
        
        // Create status bar
        JPanel statusBar = createStatusBar();
        
        // Add components to frame
        add(tabbedPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        
        JMenuItem uploadItem = new JMenuItem("Upload CSV...");
        uploadItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        uploadItem.addActionListener(e -> {
            tabbedPane.setSelectedIndex(0); // Switch to data tab
            dataPanel.triggerFileUpload(); // Now we can call the exposed method
        });
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(uploadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // View menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        
        JMenuItem toggleThemeItem = new JMenuItem("Toggle Theme");
        toggleThemeItem.setAccelerator(KeyStroke.getKeyStroke("ctrl T"));
        toggleThemeItem.addActionListener(this::toggleTheme);
        
        JMenuItem refreshStatsItem = new JMenuItem("Refresh Statistics");
        refreshStatsItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        refreshStatsItem.addActionListener(e -> refreshStatistics());
        
        viewMenu.add(toggleThemeItem);
        viewMenu.add(refreshStatsItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(this::showAboutDialog);
        
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        
        JLabel statusLabel = new JLabel("Ready - Upload a CSV file to get started");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        
        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        versionLabel.setForeground(Color.GRAY);
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(versionLabel, BorderLayout.EAST);
        
        return statusBar;
    }
    
    private void setupEventListeners() {
        // Listen for tab changes to refresh statistics
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) { // Statistics tab
                refreshStatistics();
            }
        });
    }
    
    private void toggleTheme(ActionEvent e) {
        try {
            if (isDarkTheme) {
                UIManager.setLookAndFeel(new FlatLightLaf());
                isDarkTheme = false;
            } else {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                isDarkTheme = true;
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Error changing theme: " + ex.getMessage(),
                    "Theme Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshStatistics() {
        // Get current data from data panel and update statistics
        try {
            List<SelfieDetail> currentData = dataPanel.getCurrentData();
            if (currentData != null && !currentData.isEmpty()) {
                statsPanel.updateStats(currentData);
                emailRecordsPanel.updateData(currentData);
            }
        } catch (Exception ex) {
            // Handle case where getCurrentData might not be available yet
            System.out.println("No data available for statistics refresh");
        }
    }
    
    private void showAboutDialog(ActionEvent e) {
        String message = """
                <html>
                <div style='text-align: center; font-family: sans-serif;'>
                <h2>Memzo Extracter</h2>
                <p><b>Version:</b> 1.0.0</p>
                <p><b>Description:</b> A beautiful CSV data management application</p>
                <br>
                <p><b>Features:</b></p>
                <ul style='text-align: left;'>
                <li>📁 CSV file upload and parsing</li>
                <li>📊 Beautiful data table with search and sorting</li>
                <li>📈 Comprehensive statistics and charts</li>
                <li>🎨 Modern UI with theme switching</li>
                <li>⚡ Fast and responsive performance</li>
                </ul>
                <br>
                <p>Built with Java Swing, FlatLaf, and JFreeChart</p>
                </div>
                </html>
                """;
        
        JOptionPane.showMessageDialog(this, message, "About Memzo Extracter", 
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    private Image createDefaultIcon() {
        // Create a simple default icon
        int size = 32;
        Image image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(52, 152, 219));
        g2d.fillRoundRect(2, 2, size - 4, size - 4, 8, 8);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "M";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        return image;
    }
    
    public void updateAllPanels(List<SelfieDetail> data) {
        statsPanel.updateStats(data);
        emailRecordsPanel.updateData(data);
    }
    
    public void showWindow() {
        setVisible(true);
        // Focus on the window
        toFront();
        requestFocus();
    }
}
