package com.devdam.memzo_extracter.ui.panel;

import com.devdam.memzo_extracter.model.SelfieDetail;
import com.devdam.memzo_extracter.service.CsvService;
import com.devdam.memzo_extracter.ui.model.SelfieDetailsTableModel;
import com.devdam.memzo_extracter.ui.util.BlurredModalOverlay;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

// iText imports for PDF generation
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

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
        uploadButton.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 14));
        uploadButton.setPreferredSize(new Dimension(160, 40));
        uploadButton.addActionListener(this::uploadCsvFile);
        
        // Center - Export buttons
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton exportCsvButton = new JButton("📊 Export CSV");
        exportCsvButton.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 12));
        exportCsvButton.setPreferredSize(new Dimension(120, 30));
        exportCsvButton.addActionListener(this::exportToCsv);
        
        JButton exportPdfButton = new JButton("📄 Export PDF");
        exportPdfButton.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 12));
        exportPdfButton.setPreferredSize(new Dimension(120, 30));
        exportPdfButton.addActionListener(this::exportToPdf);
        
        exportPanel.add(exportCsvButton);
        exportPanel.add(exportPdfButton);
        
        // Right side - Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel searchLabel = new JLabel("🔍 Search:");
        searchLabel.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 12));
        
        searchField = new JTextField(20);
        searchField.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 12));
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
        panel.add(exportPanel, BorderLayout.CENTER);
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
        
        int result = BlurredModalOverlay.showFileChooserWithBlurredOverlay(this, fileChooser, true);
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
                    BlurredModalOverlay.showMessageDialogWithBlurredOverlay(DataPanel.this, 
                            String.format("Successfully loaded %d records!", data.size()),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                            
                } catch (Exception ex) {
                    statusLabel.setText("Error loading CSV file");
                    BlurredModalOverlay.showMessageDialogWithBlurredOverlay(DataPanel.this, 
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
    
    private void exportToCsv(ActionEvent e) {
        List<SelfieDetail> data = tableModel.getData();
        if (data == null || data.isEmpty()) {
            BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                "No data to export. Please load a CSV file first.", 
                "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save CSV File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.setSelectedFile(new File("selfie_details_export.csv"));
        
        if (BlurredModalOverlay.showFileChooserWithBlurredOverlay(this, fileChooser, false) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }
            
            try {
                exportToCsvFile(file, data);
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
        List<SelfieDetail> data = tableModel.getData();
        if (data == null || data.isEmpty()) {
            BlurredModalOverlay.showMessageDialogWithBlurredOverlay(this, 
                "No data to export. Please load a CSV file first.", 
                "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF Report");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        fileChooser.setSelectedFile(new File("selfie_details_report.pdf"));
        
        if (BlurredModalOverlay.showFileChooserWithBlurredOverlay(this, fileChooser, false) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
            
            try {
                exportToPdfFile(file, data);
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
    
    private void exportToCsvFile(File file, List<SelfieDetail> data) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.write("Email,Name,Contact,Date,Photos,Image,Download Requests,Photos Shared,Photos Download\n");
            
            // Write data
            for (SelfieDetail record : data) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    record.getEmail() != null ? record.getEmail() : "",
                    record.getName() != null ? record.getName() : "",
                    record.getContact() != null ? record.getContact() : "",
                    record.getDate() != null ? record.getDate() : "",
                    record.getPhotos() != null ? record.getPhotos().toString() : "0",
                    record.getImageFilename() != null ? record.getImageFilename() : "",
                    record.getDownloadRequests() != null ? record.getDownloadRequests() : "",
                    record.getPhotosShared() != null ? record.getPhotosShared() : "",
                    record.getPhotosDownload() != null ? record.getPhotosDownload() : ""
                ));
            }
        }
    }
    
    private void exportToPdfFile(File file, List<SelfieDetail> data) throws Exception {
        Document document = new Document(PageSize.A4.rotate()); // Landscape for better table fit
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        
        // Add title
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
        Paragraph title = new Paragraph("Selfie Details Report", titleFont);
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
        Paragraph summary = new Paragraph("Total Records: " + data.size(), dateFont);
        summary.setSpacingAfter(15);
        document.add(summary);
        
        // Create table
        PdfPTable table = new PdfPTable(9); // 9 columns
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        
        // Set column widths
        float[] columnWidths = {2f, 2f, 2f, 1.5f, 1f, 2f, 1.5f, 1.5f, 1.5f};
        table.setWidths(columnWidths);
        
        // Add headers
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
        String[] headers = {"Email", "Name", "Contact", "Date", "Photos", "Image", "Downloads", "Shared", "Downloaded"};
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
        
        // Add data rows
        com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
        for (SelfieDetail record : data) {
            table.addCell(new PdfPCell(new Phrase(record.getEmail() != null ? record.getEmail() : "", cellFont)));
            table.addCell(new PdfPCell(new Phrase(record.getName() != null ? record.getName() : "", cellFont)));
            table.addCell(new PdfPCell(new Phrase(record.getContact() != null ? record.getContact() : "", cellFont)));
            table.addCell(new PdfPCell(new Phrase(record.getDate() != null ? record.getDate() : "", cellFont)));
            table.addCell(new PdfPCell(new Phrase(record.getPhotos() != null ? record.getPhotos().toString() : "0", cellFont)));
            table.addCell(new PdfPCell(new Phrase(record.getImageFilename() != null ? record.getImageFilename() : "", cellFont)));
            table.addCell(new PdfPCell(new Phrase(record.getDownloadRequests() != null ? record.getDownloadRequests() : "", cellFont)));
            table.addCell(new PdfPCell(new Phrase(record.getPhotosShared() != null ? record.getPhotosShared() : "", cellFont)));
            table.addCell(new PdfPCell(new Phrase(record.getPhotosDownload() != null ? record.getPhotosDownload() : "", cellFont)));
        }
        
        document.add(table);
        
        // Add footer
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph("Report generated by Memzo Extracter", cellFont);
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);
        
        document.close();
    }
    
    public List<SelfieDetail> getCurrentData() {
        return tableModel.getData();
    }
}