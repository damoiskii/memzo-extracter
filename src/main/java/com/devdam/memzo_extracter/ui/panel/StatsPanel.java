package com.devdam.memzo_extracter.ui.panel;

import com.devdam.memzo_extracter.model.SelfieDetail;
import com.devdam.memzo_extracter.service.CsvService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StatsPanel extends JPanel {
    
    private final CsvService csvService;
    private final JLabel totalRecordsLabel;
    private final JLabel completeRegistrationsLabel;
    private final JLabel emailRecordsLabel;
    private final JLabel photosRecordsLabel;
    private final JLabel averagePhotosLabel;
    private final JLabel completionRateLabel;
    private final JPanel chartsPanel;
    
    public StatsPanel(CsvService csvService) {
        this.csvService = csvService;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create stats cards panel
        JPanel statsCardsPanel = createStatsCardsPanel();
        add(statsCardsPanel, BorderLayout.NORTH);
        
        // Create charts panel
        chartsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        chartsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Data Visualization", 
                0, 0, new Font(Font.SANS_SERIF, Font.BOLD, 14)));
        add(chartsPanel, BorderLayout.CENTER);
        
        // Initialize labels
        totalRecordsLabel = new JLabel("0");
        completeRegistrationsLabel = new JLabel("0");
        emailRecordsLabel = new JLabel("0");
        photosRecordsLabel = new JLabel("0");
        averagePhotosLabel = new JLabel("0.0");
        completionRateLabel = new JLabel("0%");
        
        // Add labels to stats cards
        addStatsToPanel(statsCardsPanel);
        
        // Show initial empty state
        updateStats(null);
    }
    
    private JPanel createStatsCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 15, 15));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Statistics Overview", 
                0, 0, new Font(Font.SANS_SERIF, Font.BOLD, 14)));
        panel.setPreferredSize(new Dimension(0, 200)); // Increased height from 180 to 200
        
        return panel;
    }
    
    private void addStatsToPanel(JPanel panel) {
        // Total Records Card
        panel.add(createStatCard("📊 Total Records", totalRecordsLabel, new Color(52, 152, 219)));
        
        // Complete Registrations Card
        panel.add(createStatCard("✅ Complete Registrations", completeRegistrationsLabel, new Color(46, 204, 113)));
        
        // Email Records Card
        panel.add(createStatCard("📧 Records with Email", emailRecordsLabel, new Color(155, 89, 182)));
        
        // Photos Records Card
        panel.add(createStatCard("📸 Records with Photos", photosRecordsLabel, new Color(230, 126, 34)));
        
        // Average Photos Card
        panel.add(createStatCard("📈 Average Photos", averagePhotosLabel, new Color(26, 188, 156)));
        
        // Completion Rate Card
        panel.add(createStatCard("🎯 Completion Rate", completionRateLabel, new Color(231, 76, 60)));
    }
    
    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(8, 8)); // Increased spacing
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12))); // Adjusted padding
        card.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11)); // Slightly smaller title
        titleLabel.setForeground(new Color(60, 60, 60));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Value - reduced font size and ensured proper sizing
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20)); // Reduced from 24 to 20
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // Ensure the value label has enough space
        JPanel valuePanel = new JPanel(new BorderLayout());
        valuePanel.add(valueLabel, BorderLayout.CENTER);
        valuePanel.setBackground(Color.WHITE);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);
        
        return card;
    }
    
    public void updateStats(List<SelfieDetail> data) {
        if (data == null || data.isEmpty()) {
            // Reset all stats to zero
            totalRecordsLabel.setText("0");
            completeRegistrationsLabel.setText("0");
            emailRecordsLabel.setText("0");
            photosRecordsLabel.setText("0");
            averagePhotosLabel.setText("0.0");
            completionRateLabel.setText("0%");
            
            // Clear charts
            chartsPanel.removeAll();
            chartsPanel.add(createEmptyChartPanel("No data available"));
            chartsPanel.add(createEmptyChartPanel("Load CSV to see charts"));
            chartsPanel.revalidate();
            chartsPanel.repaint();
            return;
        }
        
        // Calculate statistics
        int totalRecords = csvService.getTotalRecords(data);
        int completeRegistrations = csvService.getCompleteRegistrations(data);
        int emailRecords = csvService.getRecordsWithEmail(data);
        int photosRecords = csvService.getRecordsWithPhotos(data);
        double averagePhotos = csvService.getAveragePhotos(data);
        double completionRate = totalRecords > 0 ? (double) completeRegistrations / totalRecords * 100 : 0;
        
        // Update labels
        totalRecordsLabel.setText(String.valueOf(totalRecords));
        completeRegistrationsLabel.setText(String.valueOf(completeRegistrations));
        emailRecordsLabel.setText(String.valueOf(emailRecords));
        photosRecordsLabel.setText(String.valueOf(photosRecords));
        averagePhotosLabel.setText(String.format("%.1f", averagePhotos));
        completionRateLabel.setText(String.format("%.1f%%", completionRate));
        
        // Update charts
        updateCharts(data, totalRecords, completeRegistrations, emailRecords, photosRecords);
    }
    
    private void updateCharts(List<SelfieDetail> data, int totalRecords, int completeRegistrations, 
                            int emailRecords, int photosRecords) {
        chartsPanel.removeAll();
        
        // Create pie chart for data completeness
        JFreeChart pieChart = createDataCompletenessChart(totalRecords, completeRegistrations, emailRecords);
        ChartPanel pieChartPanel = new ChartPanel(pieChart);
        pieChartPanel.setPreferredSize(new Dimension(400, 300));
        
        // Create bar chart for photos distribution
        JFreeChart barChart = createPhotosDistributionChart(data);
        ChartPanel barChartPanel = new ChartPanel(barChart);
        barChartPanel.setPreferredSize(new Dimension(400, 300));
        
        chartsPanel.add(pieChartPanel);
        chartsPanel.add(barChartPanel);
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }
    
    private JFreeChart createDataCompletenessChart(int totalRecords, int completeRegistrations, int emailRecords) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        
        dataset.setValue("Complete Registration", completeRegistrations);
        dataset.setValue("With Email Only", emailRecords - completeRegistrations);
        dataset.setValue("Incomplete", totalRecords - emailRecords);
        
        JFreeChart chart = ChartFactory.createPieChart(
                "Data Completeness",
                dataset,
                true,
                true,
                false
        );
        
        chart.getPlot().setBackgroundPaint(Color.WHITE);
        
        // Customize pie chart colors
        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setSectionPaint("Complete Registration", new Color(46, 204, 113)); // Green
        plot.setSectionPaint("With Email Only", new Color(52, 152, 219));      // Blue
        plot.setSectionPaint("Incomplete", new Color(231, 76, 60));             // Red
        
        return chart;
    }
    
    private JFreeChart createPhotosDistributionChart(List<SelfieDetail> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Count photos distribution
        int zeroPhotos = 0;
        int onePhoto = 0;
        int multiplePhotos = 0;
        
        for (SelfieDetail detail : data) {
            Integer photos = detail.getPhotos();
            if (photos == null || photos == 0) {
                zeroPhotos++;
            } else if (photos == 1) {
                onePhoto++;
            } else {
                multiplePhotos++;
            }
        }
        
        dataset.addValue(zeroPhotos, "Records", "0 Photos");
        dataset.addValue(onePhoto, "Records", "1 Photo");
        dataset.addValue(multiplePhotos, "Records", "2+ Photos");
        
        JFreeChart chart = ChartFactory.createBarChart(
                "Photos Distribution",
                "Photo Count",
                "Number of Records",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        chart.getPlot().setBackgroundPaint(Color.WHITE);
        return chart;
    }
    
    private JPanel createEmptyChartPanel(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        label.setForeground(Color.GRAY);
        panel.add(label, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEtchedBorder());
        return panel;
    }
}
