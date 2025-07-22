package com.devdam.memzo_extracter.ui.model;

import com.devdam.memzo_extracter.model.SelfieDetail;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmailRecordsTableModel extends AbstractTableModel {
    
    private static final String[] COLUMN_NAMES = {
        "ID", "Email", "Phone", "Date", "Name", "Photos"
    };
    
    private List<SelfieDetail> data = new ArrayList<>();
    
    @Override
    public int getRowCount() {
        return data.size();
    }
    
    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // ID
                return Integer.class;
            case 5: // Photos
                return Integer.class;
            default:
                return String.class;
        }
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= data.size()) {
            return null;
        }
        
        SelfieDetail record = data.get(rowIndex);
        
        switch (columnIndex) {
            case 0: // ID (using row index + 1 as ID)
                return rowIndex + 1;
            case 1: // Email
                return record.getEmail();
            case 2: // Phone/Contact
                return record.getContact();
            case 3: // Date
                if (record.getParsedDate() != null) {
                    return record.getParsedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                }
                return record.getDate();
            case 4: // Name
                return record.getName();
            case 5: // Photos
                return record.getPhotos();
            default:
                return null;
        }
    }
    
    public void updateData(List<SelfieDetail> newData) {
        this.data = newData != null ? new ArrayList<>(newData) : new ArrayList<>();
        fireTableDataChanged();
    }
    
    public List<SelfieDetail> getData() {
        return new ArrayList<>(data);
    }
    
    public SelfieDetail getRecordAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= data.size()) {
            return null;
        }
        return data.get(rowIndex);
    }
}
