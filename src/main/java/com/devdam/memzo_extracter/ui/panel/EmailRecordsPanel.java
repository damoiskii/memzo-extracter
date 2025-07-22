package com.devdam.memzo_extracter.ui.panel;

import com.devdam.memzo_extracter.model.SelfieDetail;
import com.devdam.memzo_extracter.service.CsvService;
import com.devdam.memzo_extracter.ui.model.EmailRecordsTableModel;
import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// iText 5 imports
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;

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
        
        // Export panel - Updated to include both CSV and PDF
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton exportButton = new JButton("📤 Export");
        exportButton.setPreferredSize(new Dimension(120, 30));
        
        JPopupMenu exportMenu = new JPopupMenu();
        
        JMenuItem csvExport = new JMenuItem("📊 Export as CSV");
        csvExport.addActionListener(this::showExportDialog);
        exportMenu.add(csvExport);
        
        JMenuItem pdfExport = new JMenuItem("📄 Export as PDF");
        pdfExport.addActionListener(this::showPdfExportDialog);
        exportMenu.add(pdfExport);
        
        exportButton.addActionListener(e -> {
            exportMenu.show(exportButton, 0, exportButton.getHeight());
        });
        
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

    // Add this method to update the record count label
    private void updateRecordCount(int count) {
        recordCountLabel.setText("Records: " + count);
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
    
    private void showPdfExportDialog(ActionEvent e) {
        if (currentData == null || currentData.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No data to export. Please load a CSV file first.", 
                "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get filtered data
        List<SelfieDetail> dataToExport = getFilteredData();
        
        if (dataToExport.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No records match the current filters.", 
                "Export Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create field selection dialog
        PdfExportDialog dialog = new PdfExportDialog(
            SwingUtilities.getWindowAncestor(this), 
            dataToExport
        );
        dialog.setVisible(true);
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
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
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
    
    // Inner class for PDF export dialog
    private static class PdfExportDialog extends JDialog {
        private final List<SelfieDetail> data;
        private final Map<String, JCheckBox> fieldCheckboxes;
        private final JTextField titleField;
        private final JCheckBox includeStatsCheckbox;
        
        public PdfExportDialog(Window parent, List<SelfieDetail> data) {
            super(parent, "PDF Export Options", ModalityType.APPLICATION_MODAL);
            this.data = data;
            this.fieldCheckboxes = new LinkedHashMap<>();
            this.titleField = new JTextField("Email Records Report", 20);
            this.includeStatsCheckbox = new JCheckBox("Include Statistics Summary", true);
            
            initializeDialog();
        }
        
        private void initializeDialog() {
            setLayout(new BorderLayout(10, 10));
            setSize(400, 500);
            setLocationRelativeTo(getParent());
            
            // Title panel
            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            titlePanel.setBorder(BorderFactory.createTitledBorder("Report Title"));
            titlePanel.add(new JLabel("Title:"));
            titlePanel.add(titleField);
            add(titlePanel, BorderLayout.NORTH);
            
            // Fields selection panel
            JPanel fieldsPanel = createFieldsPanel();
            add(new JScrollPane(fieldsPanel), BorderLayout.CENTER);
            
            // Options and buttons panel
            JPanel bottomPanel = createBottomPanel();
            add(bottomPanel, BorderLayout.SOUTH);
        }
        
        private JPanel createFieldsPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createTitledBorder("Select Fields to Include"));
            
            String[] fieldNames = {"Email", "Name", "Phone", "Date", "Photos", "Image URL"};
            String[] fieldKeys = {"email", "name", "contact", "date", "photos", "image"};
            
            for (int i = 0; i < fieldNames.length; i++) {
                JCheckBox checkbox = new JCheckBox(fieldNames[i], true);
                fieldCheckboxes.put(fieldKeys[i], checkbox);
                panel.add(checkbox);
            }
            
            // Select All / Deselect All buttons
            JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton selectAll = new JButton("Select All");
            JButton deselectAll = new JButton("Deselect All");
            
            selectAll.addActionListener(e -> fieldCheckboxes.values().forEach(cb -> cb.setSelected(true)));
            deselectAll.addActionListener(e -> fieldCheckboxes.values().forEach(cb -> cb.setSelected(false)));
            
            selectPanel.add(selectAll);
            selectPanel.add(deselectAll);
            panel.add(selectPanel);
            
            return panel;
        }
        
        private JPanel createBottomPanel() {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            
            // Options panel
            JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            optionsPanel.add(includeStatsCheckbox);
            panel.add(optionsPanel, BorderLayout.CENTER);
            
            // Buttons panel
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton exportButton = new JButton("📄 Export PDF");
            JButton cancelButton = new JButton("Cancel");
            
            exportButton.addActionListener(this::exportToPdf);
            cancelButton.addActionListener(e -> dispose());
            
            buttonsPanel.add(cancelButton);
            buttonsPanel.add(exportButton);
            panel.add(buttonsPanel, BorderLayout.SOUTH);
            
            return panel;
        }
        
        private void exportToPdf(ActionEvent e) {
            // Check if at least one field is selected
            boolean anyFieldSelected = fieldCheckboxes.values().stream()
                .anyMatch(JCheckBox::isSelected);
                
            if (!anyFieldSelected) {
                JOptionPane.showMessageDialog(this, 
                    "Please select at least one field to export.", 
                    "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Choose file location
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save PDF Report");
            fileChooser.setSelectedFile(new File("email_records_report.pdf"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("PDF files (*.pdf)", "pdf"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    file = new File(file.getAbsolutePath() + ".pdf");
                }
                
                try {
                    createPdfReport(file);
                    JOptionPane.showMessageDialog(this, 
                        "PDF report exported successfully!\nLocation: " + file.getAbsolutePath(), 
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, 
                        "Error creating PDF: " + ex.getMessage(), 
                        "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        private void createPdfReport(File file) throws Exception {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            
            // Add title
            String title = titleField.getText().trim();
            if (title.isEmpty()) title = "Email Records Report";
            
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            Paragraph titleParagraph = new Paragraph(title, titleFont);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            titleParagraph.setSpacingAfter(20);
            document.add(titleParagraph);
            
            // Add generation date
            com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
            Paragraph dateParagraph = new Paragraph("Report generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")), dateFont);
            dateParagraph.setAlignment(Element.ALIGN_CENTER);
            dateParagraph.setSpacingAfter(15);
            document.add(dateParagraph);
            
            // Add statistics summary if requested
            if (includeStatsCheckbox.isSelected()) {
                addStatisticsSummary(document);
            }
            
            // Add data table
            addDataTable(document);
            
            // Add footer
            com.itextpdf.text.Font footerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            Paragraph footer = new Paragraph("Total records in this report: " + data.size(), footerFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setSpacingBefore(20);
            document.add(footer);
            
            document.close();
        }
        
        private void addStatisticsSummary(Document document) throws DocumentException {
            // Add statistics section
            com.itextpdf.text.Font statsFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
            Paragraph statsTitle = new Paragraph("Statistics Summary", statsFont);
            statsTitle.setSpacingBefore(20);
            statsTitle.setSpacingAfter(10);
            document.add(statsTitle);
            
            // Create stats table
            PdfPTable statsTable = new PdfPTable(2);
            statsTable.setWidthPercentage(50);
            statsTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            
            com.itextpdf.text.Font labelFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font valueFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            
            // Add stats data
            addStatsRow(statsTable, "Total Records", String.valueOf(data.size()), labelFont, valueFont);
            
            long withValidEmail = data.stream()
                .filter(record -> record.getEmail() != null && !record.getEmail().trim().isEmpty())
                .count();
            addStatsRow(statsTable, "Records with Email", String.valueOf(withValidEmail), labelFont, valueFont);
            
            long withPhone = data.stream()
                .filter(record -> record.getContact() != null && !record.getContact().trim().isEmpty())
                .count();
            addStatsRow(statsTable, "Records with Phone", String.valueOf(withPhone), labelFont, valueFont);
            
            int totalPhotos = data.stream()
                .filter(record -> record.getPhotos() != null)
                .mapToInt(SelfieDetail::getPhotos)
                .sum();
            addStatsRow(statsTable, "Total Photos", String.valueOf(totalPhotos), labelFont, valueFont);
            
            document.add(statsTable);
            document.add(Chunk.NEWLINE);
        }
        
        private void addStatsRow(PdfPTable table, String label, String value, com.itextpdf.text.Font labelFont, com.itextpdf.text.Font valueFont) {
            PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
            labelCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(labelCell);
            
            PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
            valueCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(valueCell);
        }
        
        private void addDataTable(Document document) throws DocumentException {
            // Count selected fields
            List<String> selectedFields = fieldCheckboxes.entrySet().stream()
                .filter(entry -> entry.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            
            if (selectedFields.isEmpty()) return;
            
            // Add data section title
            com.itextpdf.text.Font dataFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
            Paragraph dataTitle = new Paragraph("Email Records Data", dataFont);
            dataTitle.setSpacingBefore(20);
            dataTitle.setSpacingAfter(10);
            document.add(dataTitle);
            
            // Create data table
            PdfPTable dataTable = new PdfPTable(selectedFields.size());
            dataTable.setWidthPercentage(100);
            dataTable.setSpacingBefore(10);
            
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9);
            
            // Add headers
            for (String field : selectedFields) {
                String headerText = getFieldDisplayName(field);
                PdfPCell headerCell = new PdfPCell(new Phrase(headerText, headerFont));
                headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setPadding(8);
                dataTable.addCell(headerCell);
            }
            
            // Add data rows
            for (SelfieDetail record : data) {
                for (String field : selectedFields) {
                    String value = getFieldValue(record, field);
                    PdfPCell cell = new PdfPCell(new Phrase(value, cellFont));
                    cell.setPadding(5);
                    dataTable.addCell(cell);
                }
            }
            
            document.add(dataTable);
        }
        
        private String getFieldDisplayName(String field) {
            switch (field) {
                case "email": return "Email";
                case "name": return "Name";
                case "contact": return "Phone";
                case "date": return "Date";
                case "photos": return "Photos";
                case "image": return "Image URL";
                default: return field;
            }
        }
        
        private String getFieldValue(SelfieDetail record, String field) {
            switch (field) {
                case "email":
                    return record.getEmail() != null ? record.getEmail() : "";
                case "name":
                    return record.getName() != null ? record.getName() : "";
                case "contact":
                    return record.getContact() != null ? record.getContact() : "";
                case "date":
                    return record.getDate() != null ? record.getDate() : "";
                case "photos":
                    return record.getPhotos() != null ? record.getPhotos().toString() : "0";
                case "image":
                    return record.getImage() != null ? record.getImage() : "";
                default:
                    return "";
            }
        }
    }
    
}
