package com.devdam.memzo_extracter.ui.model;

import com.devdam.memzo_extracter.model.SelfieDetail;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class SelfieDetailsTableModel extends AbstractTableModel {
    
    private final String[] columnNames = {
        "Image URL", "Name", "Email", "Contact", "Download Requests", 
        "Photos", "Photos Shared", "Photos Download", "Date"
    };
    
    private List<SelfieDetail> data = new ArrayList<>();
    
    public void setData(List<SelfieDetail> data) {
        this.data = data != null ? data : new ArrayList<>();
        fireTableDataChanged();
    }
    
    public void addData(SelfieDetail detail) {
        this.data.add(detail);
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }
    
    public void clearData() {
        this.data.clear();
        fireTableDataChanged();
    }
    
    public List<SelfieDetail> getData() {
        return new ArrayList<>(data);
    }
    
    public SelfieDetail getRowData(int row) {
        if (row >= 0 && row < data.size()) {
            return data.get(row);
        }
        return null;
    }
    
    @Override
    public int getRowCount() {
        return data.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= data.size()) {
            return null;
        }
        
        SelfieDetail detail = data.get(rowIndex);
        
        return switch (columnIndex) {
            case 0 -> detail.getImage();
            case 1 -> detail.getName();
            case 2 -> detail.getEmail();
            case 3 -> detail.getContact();
            case 4 -> detail.getDownloadRequests();
            case 5 -> detail.getPhotos();
            case 6 -> detail.getPhotosShared();
            case 7 -> detail.getPhotosDownload();
            case 8 -> detail.getDate();
            default -> null;
        };
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 5 -> Integer.class; // Photos column
            default -> String.class;
        };
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Make table read-only
    }
}
