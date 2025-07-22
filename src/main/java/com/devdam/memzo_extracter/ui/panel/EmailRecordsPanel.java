package com.devdam.memzo_extracter.ui.panel;

import com.devdam.memzo_extracter.model.SelfieDetail;
import com.devdam.memzo_extracter.service.CsvService;
import com.devdam.memzo_extracter.ui.model.EmailRecordsTableModel;
import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class EmailRecordsPanel extends JPanel {
    
    private final CsvService csvService;
    private final EmailRecordsTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<EmailRecordsTableModel> sorter;
    private final JTextField searchField;
    private final DatePicker dateFromPicker;
    private final DatePicker dateToPicker;
    private final JLabel recordCountLabel;
    private List<SelfieDetail> currentData;
    
    public EmailRecordsPanel(CsvService csvService) {
        this.csvService = csvService;
        this.tableModel = new EmailRecordsTableModel();
        this.table = new JTable(tableModel);
        this.sorter = new TableRowSorter<>(tableModel);
        this.searchField = new JTextField(20);
        
        // Initialize date pickers
        this.dateFromPicker = new DatePicker();
        this.dateToPicker = new DatePicker();
        this.dateToPicker.setDate(LocalDate.now()); // Set default to today's date
        this.recordCountLabel = new JLabel("Records: 0");
        
        initializeUI();
        setupTableProperties();
        setupFiltering();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create top panel with filters and export
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Create table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Create status panel
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Email Records Management"));
        
        // Filters panel
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtersPanel.add(new JLabel("Search:"));
        filtersPanel.add(searchField);
        filtersPanel.add(Box.createHorizontalStrut(20));
        filtersPanel.add(new JLabel("Date From:"));
        filtersPanel.add(dateFromPicker);
        filtersPanel.add(new JLabel("To:"));
        filtersPanel.add(dateToPicker);
        
        JButton filterButton = new JButton("Apply Filters");
        filterButton.addActionListener(this::applyFilters);
        filtersPanel.add(filterButton);
        
        JButton clearButton = new JButton("Clear Filters");
        clearButton.addActionListener(this::clearFilters);
        filtersPanel.add(clearButton);
        
        // Export panel
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportButton = new JButton("📤 Export Selected Fields");
        exportButton.addActionListener(this::showExportDialog);
        exportPanel.add(exportButton);
        
        panel.add(filtersPanel, BorderLayout.CENTER);
        panel.add(exportPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(0, 400));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(recordCountLabel);
        
        // Add date picker hint
        JLabel hintLabel = new JLabel("Use date pickers to filter by date range");
        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.ITALIC, 11f));
        hintLabel.setForeground(Color.GRAY);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(hintLabel);
        
        return panel;
    }
    
    private void setupTableProperties() {
        table.getTableHeader().setReorderingAllowed(true);
        table.setRowHeight(25);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(200); // Email
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Contact
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Date
        table.getColumnModel().getColumn(4).setPreferredWidth(150); // Name
        table.getColumnModel().getColumn(5).setPreferredWidth(80);  // Photos
    }
    
    private void setupFiltering() {
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(null); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(null); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(null); }
        });
    }
    
    private void applyFilters(ActionEvent e) {
        if (currentData == null) return;
        
        List<SelfieDetail> filteredData = getFilteredData();
        tableModel.updateData(filteredData);
        recordCountLabel.setText("Records: " + filteredData.size());
    }
    
    private List<SelfieDetail> getFilteredData() {
        if (currentData == null) return List.of();
        
        String searchText = searchField.getText().toLowerCase().trim();
        
        // Get dates from date pickers
        LocalDate dateFrom = dateFromPicker.getDate();
        LocalDate dateTo = dateToPicker.getDate();
        
        return currentData.stream()
            .filter(record -> {
                // Search filter
                if (!searchText.isEmpty()) {
                    String searchableText = (
                        (record.getEmail() != null ? record.getEmail() : "") + " " +
                        (record.getContact() != null ? record.getContact() : "") + " " +
                        (record.getName() != null ? record.getName() : "")
                    ).toLowerCase();
                    if (!searchableText.contains(searchText)) {
                        return false;
                    }
                }
                
                // Date filter - only apply if at least one date is selected
                if (dateFrom != null || dateTo != null) {
                    LocalDateTime recordDateTime = record.getParsedDate();
                    if (recordDateTime != null) {
                        LocalDate recordDate = recordDateTime.toLocalDate();
                        if (dateFrom != null) {
                            if (recordDate.isBefore(dateFrom)) {
                                return false;
                            }
                        }
                        if (dateTo != null) {
                            if (recordDate.isAfter(dateTo)) {
                                return false;
                            }
                        }
                    } else {
                        // If record has no valid date but date filters are active, exclude it
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }
    
    private void clearFilters(ActionEvent e) {
        searchField.setText("");
        dateFromPicker.setDate(null);
        dateToPicker.setDate(null);
        
        if (currentData != null) {
            tableModel.updateData(currentData);
            updateRecordCount(currentData.size());
        }
    }
    
    private void showExportDialog(ActionEvent e) {
        if (currentData == null || currentData.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No data to export. Please load a CSV file first.", 
                "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Apply the same filtering logic as the table display
        List<SelfieDetail> filteredData = getFilteredData();
        if (filteredData.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No records match the current filters.", 
                "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show field selection dialog
        ExportFieldsDialog dialog = new ExportFieldsDialog(
            SwingUtilities.getWindowAncestor(this), filteredData);
        dialog.setVisible(true);
    }
    
    private void updateRecordCount(int count) {
        recordCountLabel.setText("Records: " + count);
    }
    
    public void updateData(List<SelfieDetail> allData) {
        if (allData == null) {
            currentData = null;
            tableModel.updateData(null);
            updateRecordCount(0);
            return;
        }
        
        // Filter to only include records with valid email addresses
        currentData = allData.stream()
            .filter(record -> record.getEmail() != null && 
                            !record.getEmail().trim().isEmpty())
            .collect(Collectors.toList());
        
        tableModel.updateData(currentData);
        updateRecordCount(currentData.size());
    }
    
    // Inner class for export dialog
    private class ExportFieldsDialog extends JDialog {
        private final List<SelfieDetail> dataToExport;
        private final JCheckBox[] fieldCheckboxes;
        private final String[] fieldNames = {
            "Row ID", "Email", "Contact", "Date", "Name", 
            "Photos", "Image", "Download Requests", "Photos Shared", "Photos Download"
        };
        
        public ExportFieldsDialog(Window parent, List<SelfieDetail> data) {
            super(parent, "Select Fields to Export", ModalityType.APPLICATION_MODAL);
            this.dataToExport = data;
            this.fieldCheckboxes = new JCheckBox[fieldNames.length];
            
            initializeDialog();
        }
        
        private void initializeDialog() {
            setLayout(new BorderLayout(10, 10));
            setSize(400, 350);
            setLocationRelativeTo(getParent());
            
            // Create field selection panel
            JPanel fieldsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            fieldsPanel.setBorder(BorderFactory.createTitledBorder("Select Fields to Export"));
            
            for (int i = 0; i < fieldNames.length; i++) {
                fieldCheckboxes[i] = new JCheckBox(fieldNames[i]);
                // Pre-select email and contact by default
                if (fieldNames[i].equals("Email") || fieldNames[i].equals("Contact")) {
                    fieldCheckboxes[i].setSelected(true);
                }
                fieldsPanel.add(fieldCheckboxes[i]);
            }
            
            JScrollPane scrollPane = new JScrollPane(fieldsPanel);
            add(scrollPane, BorderLayout.CENTER);
            
            // Create buttons panel
            JPanel buttonsPanel = new JPanel(new FlowLayout());
            JButton selectAllButton = new JButton("Select All");
            selectAllButton.addActionListener(e -> {
                for (JCheckBox cb : fieldCheckboxes) {
                    cb.setSelected(true);
                }
            });
            
            JButton selectNoneButton = new JButton("Select None");
            selectNoneButton.addActionListener(e -> {
                for (JCheckBox cb : fieldCheckboxes) {
                    cb.setSelected(false);
                }
            });
            
            JButton exportButton = new JButton("Export");
            exportButton.addActionListener(this::performExport);
            
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());
            
            buttonsPanel.add(selectAllButton);
            buttonsPanel.add(selectNoneButton);
            buttonsPanel.add(Box.createHorizontalStrut(20));
            buttonsPanel.add(exportButton);
            buttonsPanel.add(cancelButton);
            
            add(buttonsPanel, BorderLayout.SOUTH);
        }
        
        private void performExport(ActionEvent e) {
            // Check if any fields are selected
            boolean anySelected = false;
            for (JCheckBox cb : fieldCheckboxes) {
                if (cb.isSelected()) {
                    anySelected = true;
                    break;
                }
            }
            
            if (!anySelected) {
                JOptionPane.showMessageDialog(this, 
                    "Please select at least one field to export.", 
                    "Export Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Show file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Export File");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
            fileChooser.setSelectedFile(new File("email_records_export.csv"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }
                
                try {
                    exportToFile(file);
                    JOptionPane.showMessageDialog(this, 
                        "Export completed successfully!\nFile saved: " + file.getAbsolutePath(), 
                        "Export Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Error exporting file: " + ex.getMessage(), 
                        "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        private void exportToFile(File file) throws IOException {
            try (FileWriter writer = new FileWriter(file)) {
                // Write header
                boolean first = true;
                for (int i = 0; i < fieldCheckboxes.length; i++) {
                    if (fieldCheckboxes[i].isSelected()) {
                        if (!first) writer.write(",");
                        writer.write("\"" + fieldNames[i] + "\"");
                        first = false;
                    }
                }
                writer.write("\n");
                
                // Write data
                for (SelfieDetail record : dataToExport) {
                    first = true;
                    for (int i = 0; i < fieldCheckboxes.length; i++) {
                        if (fieldCheckboxes[i].isSelected()) {
                            if (!first) writer.write(",");
                            
                            String value = getFieldValue(record, i);
                            writer.write("\"" + (value != null ? value.replace("\"", "\"\"") : "") + "\"");
                            first = false;
                        }
                    }
                    writer.write("\n");
                }
            }
        }
        
        private String getFieldValue(SelfieDetail record, int fieldIndex) {
            switch (fieldIndex) {
                case 0: return String.valueOf(fieldIndex + 1); // Row ID (simple counter)
                case 1: return record.getEmail();
                case 2: return record.getContact();
                case 3: return record.getDate();
                case 4: return record.getName();
                case 5: return record.getPhotos() != null ? record.getPhotos().toString() : "";
                case 6: return record.getImageFilename();
                case 7: return record.getDownloadRequests();
                case 8: return record.getPhotosShared();
                case 9: return record.getPhotosDownload();
                default: return "";
            }
        }
    }
}
