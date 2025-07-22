package com.devdam.memzo_extracter.ui.panel;

import com.devdam.memzo_extracter.model.SelfieDetail;
import com.devdam.memzo_extracter.service.CsvService;
import com.devdam.memzo_extracter.ui.model.SelfieDetailsTableModel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class DataPanel extends JPanel {
    
    private final CsvService csvService;
    private final SelfieDetailsTableModel tableModel;
    private final JTable table;
    private JLabel statusLabel;
    private JTextField searchField;
    private TableRowSorter<SelfieDetailsTableModel> sorter;
    private Consumer<List<SelfieDetail>> dataUpdateCallback;
    
    public DataPanel(CsvService csvService) {
        this(csvService, null);
    }
    
    public DataPanel(CsvService csvService, Consumer<List<SelfieDetail>> dataUpdateCallback) {
        this.csvService = csvService;
        this.tableModel = new SelfieDetailsTableModel();
        this.dataUpdateCallback = dataUpdateCallback;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create top panel with upload button and search
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Create table
        table = createTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Create bottom panel with status
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Initialize sorter for search functionality
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Left side - Upload button
        JButton uploadButton = new JButton("📁 Upload CSV File");
        uploadButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        uploadButton.setPreferredSize(new Dimension(160, 40));
        uploadButton.addActionListener(this::uploadCsvFile);
        
        // Right side - Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel searchLabel = new JLabel("🔍 Search:");
        searchLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        searchField = new JTextField(20);
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        searchField.addActionListener(e -> filterTable());
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> filterTable());
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            searchField.setText("");
            sorter.setRowFilter(null);
        });
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        
        panel.add(uploadButton, BorderLayout.WEST);
        panel.add(searchPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JTable createTable() {
        JTable table = new JTable(tableModel);
        
        // Set table properties
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        table.setRowHeight(30);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(184, 207, 229));
        table.setSelectionForeground(Color.BLACK);
        
        // Set header properties
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(200); // Image URL
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Name
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Email
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Contact
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Download Requests
        table.getColumnModel().getColumn(5).setPreferredWidth(60);  // Photos
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Photos Shared
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Photos Download
        table.getColumnModel().getColumn(8).setPreferredWidth(120); // Date
        
        // Custom cell renderer for better appearance
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); // Photos column
        
        // Custom renderer for email column to highlight valid emails
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String email = (String) value;
                    if (email != null && !email.isEmpty() && email.contains("@")) {
                        c.setBackground(new Color(230, 255, 230)); // Light green for valid emails
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
        
        return table;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        statusLabel = new JLabel("Ready to load CSV file...");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        panel.add(statusLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    private void uploadCsvFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            loadCsvFile(selectedFile);
        }
    }
    
    private void loadCsvFile(File file) {
        SwingWorker<List<SelfieDetail>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SelfieDetail> doInBackground() throws Exception {
                statusLabel.setText("Loading CSV file...");
                return csvService.parseCsvFile(file);
            }
            
            @Override
            protected void done() {
                try {
                    List<SelfieDetail> data = get();
                    tableModel.setData(data);
                    statusLabel.setText(String.format("Loaded %d records from %s", 
                            data.size(), file.getName()));
                    
                    // Notify other panels of data update
                    if (dataUpdateCallback != null) {
                        dataUpdateCallback.accept(data);
                    }
                    
                    // Show success message
                    JOptionPane.showMessageDialog(DataPanel.this, 
                            String.format("Successfully loaded %d records!", data.size()),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                            
                } catch (Exception ex) {
                    statusLabel.setText("Error loading CSV file");
                    JOptionPane.showMessageDialog(DataPanel.this, 
                            "Error loading CSV file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void filterTable() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }
    
    // Add this public method to expose the upload functionality
    public void triggerFileUpload() {
        uploadCsvFile(null); // Call the existing private method
    }

    // Add this method to expose current data
    public List<SelfieDetail> getCurrentData() {
        return tableModel.getAllData(); // Assuming your table model has this method
    }
}
