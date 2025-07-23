package com.devdam.memzo_extracter.ui.panel;

import com.devdam.memzo_extracter.model.SelfieDetail;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

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

public class NewEmailsPanel extends JPanel {
    
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Storage for new emails (emails in CSV but not in unique database)
    private List<SelfieDetail> newEmails = new ArrayList<>();
    private Set<String> uniqueDatabaseEmails = new LinkedHashSet<>();
    
    // File path for db.csv (same as UniqueEmailsPanel)
    private static final String DB_FILE_PATH = "db.csv";
    
    public NewEmailsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Load existing unique emails from db.csv
        loadUniqueDatabase();
        
        // Create UI components
        createTopPanel();
        createTablePanel();
        createBottomPanel();
        
        // Initialize table
        refreshTable();
    }
    
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        
        // Left side - Info
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("🆕 New Emails");
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
        
        // Right side - Export buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        JButton exportCsvButton = new JButton("📊 Export CSV");
        exportCsvButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        exportCsvButton.setPreferredSize(new Dimension(115, 30));
        exportCsvButton.addActionListener(this::exportToCsv);
        
        JButton exportPdfButton = new JButton("📄 Export PDF");
        exportPdfButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        exportPdfButton.setPreferredSize(new Dimension(115, 30));
        exportPdfButton.addActionListener(this::exportToPdf);
        
        JButton refreshButton = new JButton("🔄 Refresh");
        refreshButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        refreshButton.setPreferredSize(new Dimension(100, 30));
        refreshButton.addActionListener(e -> {
            loadUniqueDatabase();
            // Data will be updated when CSV is uploaded through the updateNewEmails method
            refreshTable();
        });
        
        actionPanel.add(refreshButton);
        actionPanel.add(Box.createHorizontalStrut(10)); // Spacer
        actionPanel.add(exportCsvButton);
        actionPanel.add(exportPdfButton);
        
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
            "Name", "Email", "Contact", "Date", "Photos", "Downloads"
        });
        
        // Create table
        table = new JTable(tableModel);
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        table.setRowHeight(30);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(255, 235, 156)); // Light orange for new emails
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
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // Date
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Photos
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Downloads
        
        // Custom cell renderer for email column to highlight new emails
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(new Color(255, 248, 220)); // Light yellow for new emails
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
        
        statusLabel = new JLabel("Ready - " + newEmails.size() + " new emails found");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        
        JLabel infoLabelText = new JLabel("📝 Tip: These are emails from uploaded CSV that are not yet in the unique database");
        infoLabelText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        infoLabelText.setForeground(Color.GRAY);
        
        infoPanel.add(statusLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(infoLabelText);
        
        bottomPanel.add(infoPanel, BorderLayout.WEST);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void loadUniqueDatabase() {
        uniqueDatabaseEmails.clear();
        
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
                    String email = unescapeCsv(parts[1]);
                    if (email != null && !email.trim().isEmpty() && email.contains("@")) {
                        uniqueDatabaseEmails.add(email.toLowerCase());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading unique database file: " + e.getMessage());
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
        
        for (SelfieDetail record : newEmails) {
            String name = toTitleCase(record.getName());
            String email = record.getEmail();
            String contact = record.getContact() != null ? record.getContact() : "";
            String date = record.getDate() != null ? record.getDate() : "";
            String photos = record.getPhotos() != null ? record.getPhotos().toString() : "0";
            String downloads = record.getDownloadRequests() != null ? record.getDownloadRequests() : "0";
            
            tableModel.addRow(new Object[]{name, email, contact, date, photos, downloads});
        }
        
        statusLabel.setText("Ready - " + newEmails.size() + " new emails found");
    }
    
    private String toTitleCase(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        String[] words = input.trim().toLowerCase().split("\\s+");
        StringBuilder titleCase = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                titleCase.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    titleCase.append(word.substring(1));
                }
                titleCase.append(" ");
            }
        }
        
        return titleCase.toString().trim();
    }
    
    private void filterTable() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }
    
    public void updateNewEmails(List<SelfieDetail> csvData) {
        newEmails.clear();
        
        if (csvData == null || csvData.isEmpty()) {
            refreshTable();
            return;
        }
        
        Set<String> processedEmails = new HashSet<>(); // To avoid duplicates in new emails list
        
        for (SelfieDetail record : csvData) {
            String email = record.getEmail();
            if (email != null && !email.trim().isEmpty() && email.contains("@")) {
                String emailKey = email.toLowerCase();
                
                // Check if this email is NOT in the unique database and hasn't been processed yet
                if (!uniqueDatabaseEmails.contains(emailKey) && !processedEmails.contains(emailKey)) {
                    newEmails.add(record);
                    processedEmails.add(emailKey);
                }
            }
        }
        
        refreshTable();
    }
    
    private void exportToCsv(ActionEvent e) {
        if (newEmails.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No new emails to export!", 
                "Export Warning", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("new_emails_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new java.io.File(file.getAbsolutePath() + ".csv");
            }
            
            exportToCsvFile(file);
        }
    }
    
    private void exportToPdf(ActionEvent e) {
        if (newEmails.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No new emails to export!", 
                "Export Warning", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("new_emails_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new java.io.File(file.getAbsolutePath() + ".pdf");
            }
            
            exportToPdfFile(file);
        }
    }
    
    private void exportToCsvFile(java.io.File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write header
            writer.println("Name,Email,Contact,Date,Photos,Downloads");
            
            // Write data - ensure name is first, emails are unique, and names are in title case
            Set<String> exportedEmails = new HashSet<>();
            
            for (SelfieDetail record : newEmails) {
                String email = record.getEmail();
                if (email != null && !email.trim().isEmpty() && !exportedEmails.contains(email.toLowerCase())) {
                    exportedEmails.add(email.toLowerCase());
                    
                    String name = toTitleCase(record.getName());
                    String contact = record.getContact() != null ? record.getContact() : "";
                    String date = record.getDate() != null ? record.getDate() : "";
                    String photos = record.getPhotos() != null ? record.getPhotos().toString() : "0";
                    String downloads = record.getDownloadRequests() != null ? record.getDownloadRequests() : "0";
                    
                    // Name column is first, format properly
                    writer.printf("%s,%s,%s,%s,%s,%s%n", 
                        escapeCsv(name), 
                        escapeCsv(email), 
                        escapeCsv(contact), 
                        escapeCsv(date), 
                        escapeCsv(photos), 
                        escapeCsv(downloads));
                }
            }
            
            JOptionPane.showMessageDialog(this, 
                "New emails exported successfully to:\n" + file.getAbsolutePath(), 
                "Export Successful", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error exporting to CSV: " + ex.getMessage(), 
                "Export Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportToPdfFile(java.io.File file) {
        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            
            // Add title
            Paragraph title = new Paragraph("New Emails Report");
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Add timestamp
            Paragraph timestamp = new Paragraph("Generated on: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm:ss")));
            timestamp.setAlignment(Element.ALIGN_CENTER);
            timestamp.setSpacingAfter(20);
            document.add(timestamp);
            
            // Create table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 4, 2.5f, 3, 1.5f, 2});
            
            // Add headers
            String[] headers = {"Name", "Email", "Contact", "Date", "Photos", "Downloads"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header));
                cell.setBackgroundColor(new BaseColor(240, 240, 240));
                cell.setPadding(8);
                table.addCell(cell);
            }
            
            // Add data - ensure name is first, emails are unique, and names are in title case
            Set<String> exportedEmails = new HashSet<>();
            
            for (SelfieDetail record : newEmails) {
                String email = record.getEmail();
                if (email != null && !email.trim().isEmpty() && !exportedEmails.contains(email.toLowerCase())) {
                    exportedEmails.add(email.toLowerCase());
                    
                    String name = toTitleCase(record.getName());
                    String contact = record.getContact() != null ? record.getContact() : "";
                    String date = record.getDate() != null ? record.getDate() : "";
                    String photos = record.getPhotos() != null ? record.getPhotos().toString() : "0";
                    String downloads = record.getDownloadRequests() != null ? record.getDownloadRequests() : "0";
                    
                    // Name column is first
                    table.addCell(createDataCell(name));
                    table.addCell(createDataCell(email));
                    table.addCell(createDataCell(contact));
                    table.addCell(createDataCell(date));
                    table.addCell(createDataCell(photos));
                    table.addCell(createDataCell(downloads));
                }
            }
            
            document.add(table);
            
            // Add summary
            Paragraph summary = new Paragraph("\nTotal new emails: " + 
                newEmails.stream()
                    .map(r -> r.getEmail())
                    .filter(email -> email != null && !email.trim().isEmpty())
                    .map(String::toLowerCase)
                    .collect(java.util.stream.Collectors.toSet()).size());
            summary.setSpacingBefore(20);
            document.add(summary);
            
            document.close();
            
            JOptionPane.showMessageDialog(this, 
                "New emails exported successfully to:\n" + file.getAbsolutePath(), 
                "Export Successful", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error exporting to PDF: " + ex.getMessage(), 
                "Export Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private PdfPCell createDataCell(String content) {
        PdfPCell cell = new PdfPCell(new Phrase(content != null ? content : ""));
        cell.setPadding(5);
        return cell;
    }
}
