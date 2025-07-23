package com.devdam.memzo_extracter.ui.panel;

import com.devdam.memzo_extracter.model.SelfieDetail;
import com.devdam.memzo_extracter.service.CsvService;
import com.devdam.memzo_extracter.ui.util.BlurredModalOverlay;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// iText imports for PDF generation
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class UniqueEmailsPanel extends JPanel {
    
    private final CsvService csvService;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton saveButton;
    private JLabel lastSavedLabel;
    
    // In-memory storage for unique emails
    private Set<String> uniqueEmails = new LinkedHashSet<>();
    private Map<String, SelfieDetail> emailToRecordMap = new HashMap<>();
    
    // File path for db.csv
    private static final String DB_FILE_PATH = "db.csv";
    
    public UniqueEmailsPanel(CsvService csvService) {
        this.csvService = csvService;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Load existing data from db.csv on startup
        loadDatabaseFile();
        
        // Create UI components
        createTopPanel();
        createTablePanel();
        createBottomPanel();
        
        // Initialize table with current data
        refreshTable();
    }
    
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        
        // Left side - Info
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("🗄️ Unique Email Database");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        infoPanel.add(titleLabel);
        
        // Center - Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
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
        
        // Right side - Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        // Export buttons
        JButton exportCsvButton = new JButton("📊 Export CSV");
        exportCsvButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        exportCsvButton.setPreferredSize(new Dimension(115, 30));
        exportCsvButton.addActionListener(this::exportToCsv);
        
        JButton exportPdfButton = new JButton("� Export PDF");
        exportPdfButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        exportPdfButton.setPreferredSize(new Dimension(115, 30));
        exportPdfButton.addActionListener(this::exportToPdf);
        
        // Save button
        saveButton = new JButton("�💾 Save to Database");
        saveButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        saveButton.setPreferredSize(new Dimension(160, 35));
        saveButton.addActionListener(this::saveToDatabaseAsync);
        
        actionPanel.add(exportCsvButton);
        actionPanel.add(exportPdfButton);
        actionPanel.add(Box.createHorizontalStrut(10)); // Spacer
        actionPanel.add(saveButton);
        
        topPanel.add(infoPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(actionPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
    }
    
    private void createTablePanel() {
        // Create table model
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        tableModel.setColumnIdentifiers(new String[]{
            "Name", "Email", "Contact", "Date Added", "Source"
        });
        
        // Create table
        table = new JTable(tableModel);
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
        table.getColumnModel().getColumn(0).setPreferredWidth(150); // Name
        table.getColumnModel().getColumn(1).setPreferredWidth(200); // Email
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Contact
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // Date Added
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Source
        
        // Custom cell renderer for email column
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(new Color(230, 255, 230)); // Light green for emails
                } else {
                    c.setBackground(table.getSelectionBackground());
                }
                return c;
            }
        });
        
        // Initialize sorter for search functionality
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Status and info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        statusLabel = new JLabel("Ready - " + uniqueEmails.size() + " unique emails loaded");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        
        lastSavedLabel = new JLabel("");
        lastSavedLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        lastSavedLabel.setForeground(Color.GRAY);
        
        infoPanel.add(statusLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(lastSavedLabel);
        
        bottomPanel.add(infoPanel, BorderLayout.WEST);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void loadDatabaseFile() {
        File dbFile = new File(DB_FILE_PATH);
        if (!dbFile.exists()) {
            return; // No existing database file
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(dbFile))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // Skip header
                }
                
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // CSV parsing with quoted fields
                if (parts.length >= 2) {
                    String name = unescapeCsv(parts[0]);
                    String email = unescapeCsv(parts[1]);
                    String contact = parts.length > 2 ? unescapeCsv(parts[2]) : "";
                    String dateAdded = parts.length > 3 ? unescapeCsv(parts[3]) : "";
                    
                    if (email != null && !email.trim().isEmpty() && email.contains("@")) {
                        uniqueEmails.add(email.toLowerCase());
                        
                        // Create a record for this email
                        SelfieDetail record = new SelfieDetail();
                        record.setName(name);
                        record.setEmail(email);
                        record.setContact(contact);
                        record.setDate(dateAdded);
                        
                        emailToRecordMap.put(email.toLowerCase(), record);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading database file: " + e.getMessage());
        }
    }
    
    private String unescapeCsv(String value) {
        if (value == null) return "";
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\"\"", "\""); // Unescape quotes
        }
        return value;
    }
    
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\""); // Escape quotes
            return "\"" + value + "\"";
        }
        return value;
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0); // Clear existing rows
        
        for (String email : uniqueEmails) {
            SelfieDetail record = emailToRecordMap.get(email);
            if (record != null) {
                String name = toTitleCase(record.getName());
                String contact = record.getContact() != null ? record.getContact() : "";
                String dateAdded = record.getDate() != null ? record.getDate() : 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String source = "Database";
                
                tableModel.addRow(new Object[]{name, email, contact, dateAdded, source});
            }
        }
        
        statusLabel.setText("Ready - " + uniqueEmails.size() + " unique emails loaded");
    }
    
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
            if (words[i].length() > 0) {
                titleCase.append(Character.toUpperCase(words[i].charAt(0)));
                if (words[i].length() > 1) {
                    titleCase.append(words[i].substring(1));
                }
            }
        }
        
        return titleCase.toString();
    }
    
    private void filterTable() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }
    
    public void updateFromMainData(List<SelfieDetail> newData) {
        if (newData == null || newData.isEmpty()) {
            return;
        }
        
        int newEmailsAdded = 0;
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        for (SelfieDetail record : newData) {
            String email = record.getEmail();
            if (email != null && !email.trim().isEmpty() && email.contains("@")) {
                String emailKey = email.toLowerCase();
                if (!uniqueEmails.contains(emailKey)) {
                    uniqueEmails.add(emailKey);
                    
                    // Create a copy with current timestamp
                    SelfieDetail dbRecord = new SelfieDetail();
                    dbRecord.setName(record.getName());
                    dbRecord.setEmail(email);
                    dbRecord.setContact(record.getContact());
                    dbRecord.setDate(currentDateTime);
                    
                    emailToRecordMap.put(emailKey, dbRecord);
                    newEmailsAdded++;
                }
            }
        }
        
        if (newEmailsAdded > 0) {
            refreshTable();
            
            // Auto-save new emails in background
            final int emailsAdded = newEmailsAdded; // Make effectively final for lambda
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Added " + emailsAdded + " new unique emails - Auto-saving...");
                saveToDatabaseAsync(null);
            });
        }
    }
    
    private void saveToDatabaseAsync(ActionEvent e) {
        saveButton.setEnabled(false);
        statusLabel.setText("Saving to database...");
        
        CompletableFuture.runAsync(() -> {
            try {
                saveDatabaseFile();
                
                SwingUtilities.invokeLater(() -> {
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"));
                    lastSavedLabel.setText("Last saved: " + timestamp);
                    statusLabel.setText("Database saved successfully - " + uniqueEmails.size() + " unique emails");
                    saveButton.setEnabled(true);
                    
                    // Show success notification
                    BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                        "Database saved successfully!\n" + uniqueEmails.size() + " unique emails saved to " + DB_FILE_PATH,
                        "Save Success", JOptionPane.INFORMATION_MESSAGE);
                });
                
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Error saving database: " + ex.getMessage());
                    saveButton.setEnabled(true);
                    
                    BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                        "Error saving database: " + ex.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }
    
    private void saveDatabaseFile() throws IOException {
        File dbFile = new File(DB_FILE_PATH);
        
        try (FileWriter writer = new FileWriter(dbFile)) {
            // Write header
            writer.write("Name,Email,Contact,Date Added\n");
            
            // Write data
            for (String email : uniqueEmails) {
                SelfieDetail record = emailToRecordMap.get(email);
                if (record != null) {
                    writer.write(String.format("%s,%s,%s,%s\n",
                        escapeCsv(record.getName()),
                        escapeCsv(record.getEmail()),
                        escapeCsv(record.getContact()),
                        escapeCsv(record.getDate())
                    ));
                }
            }
        }
    }
    
    private void exportToCsv(ActionEvent e) {
        if (uniqueEmails.isEmpty()) {
            BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                "No unique emails to export. Please load some data first.", 
                "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Unique Emails CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.setSelectedFile(new File("unique_emails_export.csv"));
        
        if (BlurredModalOverlay.showFileChooserWithBlurredOverlay(this, fileChooser, false) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }
            
            try {
                exportToCsvFile(file);
                BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                    "CSV export completed successfully!\nFile saved: " + file.getAbsolutePath(), 
                    "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                    "Error exporting CSV: " + ex.getMessage(), 
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportToPdf(ActionEvent e) {
        if (uniqueEmails.isEmpty()) {
            BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                "No unique emails to export. Please load some data first.", 
                "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Unique Emails PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        fileChooser.setSelectedFile(new File("unique_emails_report.pdf"));
        
        if (BlurredModalOverlay.showFileChooserWithBlurredOverlay(this, fileChooser, false) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
            
            try {
                exportToPdfFile(file);
                BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                    "PDF export completed successfully!\nFile saved: " + file.getAbsolutePath(), 
                    "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                    "Error exporting PDF: " + ex.getMessage(), 
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportToCsvFile(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header with only Name, Email, Contact
            writer.write("Name,Email,Contact\n");
            
            // Write data
            for (String email : uniqueEmails) {
                SelfieDetail record = emailToRecordMap.get(email);
                if (record != null) {
                    writer.write(String.format("%s,%s,%s\n",
                        escapeCsv(toTitleCase(record.getName())),
                        escapeCsv(record.getEmail()),
                        escapeCsv(record.getContact())
                    ));
                }
            }
        }
    }
    
    private void exportToPdfFile(File file) throws Exception {
        Document document = new Document(PageSize.A4); // Portrait for 3 columns
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        
        // Add title
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        Paragraph title = new Paragraph("Unique Email Records Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Add generation date
        com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
        Paragraph dateInfo = new Paragraph("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")), dateFont);
        dateInfo.setAlignment(Element.ALIGN_CENTER);
        dateInfo.setSpacingAfter(20);
        document.add(dateInfo);
        
        // Add summary
        Paragraph summary = new Paragraph("Total Unique Email Records: " + uniqueEmails.size(), dateFont);
        summary.setSpacingAfter(15);
        document.add(summary);
        
        // Create table with 3 columns
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        
        // Set column widths
        float[] columnWidths = {3f, 4f, 2.5f}; // Name, Email, Contact
        table.setWidths(columnWidths);
        
        // Add headers
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
        String[] headers = {"Name", "Email", "Contact"};
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(10);
            table.addCell(cell);
        }
        
        // Add data rows
        com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
        for (String email : uniqueEmails) {
            SelfieDetail record = emailToRecordMap.get(email);
            if (record != null) {
                table.addCell(new PdfPCell(new Phrase(toTitleCase(record.getName()) != null ? toTitleCase(record.getName()) : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(record.getEmail() != null ? record.getEmail() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(record.getContact() != null ? record.getContact() : "", cellFont)));
            }
        }
        
        document.add(table);
        
        // Add footer
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph("Report generated by Memzo Extracter - Unique Email Database", cellFont);
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);
        
        document.close();
    }
    
    public int getUniqueEmailCount() {
        return uniqueEmails.size();
    }
    
    public Set<String> getUniqueEmails() {
        return new HashSet<>(uniqueEmails);
    }
}
