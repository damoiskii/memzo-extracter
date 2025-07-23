package com.devdam.memzo_extracter.ui.panel;

import com.devdam.memzo_extracter.model.SelfieDetail;
import com.devdam.memzo_extracter.service.CsvService;
import com.devdam.memzo_extracter.ui.model.EmailRecordsTableModel;
import com.devdam.memzo_extracter.ui.util.BlurredModalOverlay;
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
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

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
            BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                "No data to export. Please load a CSV file first.", 
                "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Apply the same filtering logic as the table display
        List<SelfieDetail> filteredData = getFilteredData();
        if (filteredData.isEmpty()) {
            BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                "No records match the current filters.", 
                "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show field selection dialog
        ExportFieldsDialog dialog = new ExportFieldsDialog(
            SwingUtilities.getWindowAncestor(this), filteredData);
        BlurredModalOverlay.showDialogWithBlurredOverlay(SwingUtilities.getWindowAncestor(this), dialog);
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
            "Name", "Email", "Contact", "Date", "Row ID",
            "Photos", "Image", "Download Requests", "Photos Shared", "Photos Download"
        };
        
        // Dialog size constants
        private static final int DIALOG_WIDTH = 600;
        private static final int DIALOG_HEIGHT = 500;

        public ExportFieldsDialog(Window parent, List<SelfieDetail> data) {
            super(parent, "Select Fields to Export", ModalityType.APPLICATION_MODAL);
            this.dataToExport = removeDuplicatesByEmail(data);
            this.fieldCheckboxes = new JCheckBox[fieldNames.length];
            
            initializeDialog();
        }
        
        private void initializeDialog() {
            setLayout(new BorderLayout(10, 10));
            setSize(DIALOG_WIDTH, DIALOG_HEIGHT);  // Increased width to accommodate all buttons
            setLocationRelativeTo(getParent());
            
            // Create field selection panel
            JPanel fieldsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            fieldsPanel.setBorder(BorderFactory.createTitledBorder("Select Fields to Export"));
            
            for (int i = 0; i < fieldNames.length; i++) {
                fieldCheckboxes[i] = new JCheckBox(fieldNames[i]);
                // Pre-select name, email and contact by default
                if (fieldNames[i].equals("Name") || fieldNames[i].equals("Email") || fieldNames[i].equals("Contact")) {
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
            
            JButton exportCsvButton = new JButton("📊 Export CSV");
            exportCsvButton.addActionListener(e -> performExport("csv"));
            
            JButton exportPdfButton = new JButton("📄 Export PDF");
            exportPdfButton.addActionListener(e -> performExport("pdf"));
            
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());
            
            buttonsPanel.add(selectAllButton);
            buttonsPanel.add(selectNoneButton);
            buttonsPanel.add(Box.createHorizontalStrut(20));
            buttonsPanel.add(exportCsvButton);
            buttonsPanel.add(exportPdfButton);
            buttonsPanel.add(cancelButton);
            
            add(buttonsPanel, BorderLayout.SOUTH);
        }
        
        private void performExport(String format) {
            // Check if any fields are selected
            boolean anySelected = false;
            for (JCheckBox cb : fieldCheckboxes) {
                if (cb.isSelected()) {
                    anySelected = true;
                    break;
                }
            }
            
            if (!anySelected) {
                BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                    "Please select at least one field to export.", 
                    "Export Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Show file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Export File");
            
            String extension = format.equals("pdf") ? "pdf" : "csv";
            String description = format.equals("pdf") ? "PDF Files" : "CSV Files";
            String defaultFileName = format.equals("pdf") ? "email_records_export.pdf" : "email_records_export.csv";
            
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(description, extension));
            fileChooser.setSelectedFile(new File(defaultFileName));
            
            if (BlurredModalOverlay.showFileChooserWithBlurredOverlay(this, fileChooser, false) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith("." + extension)) {
                    file = new File(file.getAbsolutePath() + "." + extension);
                }
                
                try {
                    if (format.equals("pdf")) {
                        exportToPdf(file);
                    } else {
                        exportToCsv(file);
                    }
                    BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                        "Export completed successfully!\nFile saved: " + file.getAbsolutePath(), 
                        "Export Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (Exception ex) {
                    BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                        "Error exporting file: " + ex.getMessage(), 
                        "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        private void exportToCsv(File file) throws IOException {
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
        
        private void exportToPdf(File file) throws Exception {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            
            try {
                PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
                document.open();
                
                // Add title
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Email Records Export Report");
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);
                
                // Add export date
                com.itextpdf.text.Paragraph exportDate = new com.itextpdf.text.Paragraph(
                    "Export Date: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                exportDate.setSpacingAfter(10);
                document.add(exportDate);
                
                // Add record count
                com.itextpdf.text.Paragraph recordCount = new com.itextpdf.text.Paragraph(
                    "Total Records: " + dataToExport.size());
                recordCount.setSpacingAfter(20);
                document.add(recordCount);
                
                // Count selected columns
                int selectedColumns = 0;
                for (JCheckBox cb : fieldCheckboxes) {
                    if (cb.isSelected()) selectedColumns++;
                }
                
                // Create table
                PdfPTable table = new PdfPTable(selectedColumns);
                table.setWidthPercentage(100);
                
                // Add headers
                for (int i = 0; i < fieldCheckboxes.length; i++) {
                    if (fieldCheckboxes[i].isSelected()) {
                        PdfPCell headerCell = new PdfPCell(new Phrase(fieldNames[i]));
                        headerCell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                        headerCell.setPadding(8);
                        table.addCell(headerCell);
                    }
                }
                
                // Add data rows
                for (SelfieDetail record : dataToExport) {
                    for (int i = 0; i < fieldCheckboxes.length; i++) {
                        if (fieldCheckboxes[i].isSelected()) {
                            String value = getFieldValue(record, i);
                            PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : ""));
                            cell.setPadding(5);
                            table.addCell(cell);
                        }
                    }
                }
                
                document.add(table);
                
            } finally {
                document.close();
            }
        }
        
        private String getFieldValue(SelfieDetail record, int fieldIndex) {
            switch (fieldIndex) {
                case 0: return toTitleCase(record.getName()); // Name (with title case)
                case 1: return record.getEmail(); // Email
                case 2: return record.getContact(); // Contact
                case 3: return record.getDate(); // Date
                case 4: return String.valueOf(fieldIndex + 1); // Row ID (simple counter)
                case 5: return record.getPhotos() != null ? record.getPhotos().toString() : ""; // Photos
                case 6: return record.getImageFilename(); // Image
                case 7: return record.getDownloadRequests(); // Download Requests
                case 8: return record.getPhotosShared(); // Photos Shared
                case 9: return record.getPhotosDownload(); // Photos Download
                default: return "";
            }
        }
        
        /**
         * Removes duplicate records based on email address, keeping the first occurrence
         */
        private List<SelfieDetail> removeDuplicatesByEmail(List<SelfieDetail> data) {
            if (data == null) return null;
            
            Set<String> seenEmails = new HashSet<>();
            List<SelfieDetail> uniqueRecords = new ArrayList<>();
            
            for (SelfieDetail record : data) {
                String email = record.getEmail();
                if (email != null && !email.trim().isEmpty()) {
                    String normalizedEmail = email.trim().toLowerCase();
                    if (!seenEmails.contains(normalizedEmail)) {
                        seenEmails.add(normalizedEmail);
                        uniqueRecords.add(record);
                    }
                }
            }
            
            return uniqueRecords;
        }
        
        /**
         * Converts a string to title case (first letter of each word capitalized)
         */
        private String toTitleCase(String input) {
            if (input == null || input.trim().isEmpty()) {
                return input;
            }
            
            String[] words = input.trim().toLowerCase().split("\\s+");
            StringBuilder titleCase = new StringBuilder();
            
            for (int i = 0; i < words.length; i++) {
                if (i > 0) {
                    titleCase.append(" ");
                }
                
                String word = words[i];
                if (word.length() > 0) {
                    titleCase.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) {
                        titleCase.append(word.substring(1));
                    }
                }
            }
            
            return titleCase.toString();
        }
    }
}
