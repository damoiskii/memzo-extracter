package com.devdam.memzo_extracter.service;

import com.devdam.memzo_extracter.model.SelfieDetail;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {
    
    public List<SelfieDetail> parseCsvFile(File file) throws IOException {
        List<SelfieDetail> selfieDetails = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             CSVParser csvParser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .parse(isr)) {
            
            for (CSVRecord record : csvParser) {
                SelfieDetail detail = SelfieDetail.builder()
                        .image(getStringValue(record, "image"))
                        .name(getStringValue(record, "name"))
                        .email(getStringValue(record, "email"))
                        .contact(getStringValue(record, "contact"))
                        .downloadRequests(getStringValue(record, "download_requests"))
                        .photos(getIntegerValue(record, "photos"))
                        .photosShared(getStringValue(record, "photos_shared"))
                        .photosDownload(getStringValue(record, "photos_download"))
                        .date(getStringValue(record, "date"))
                        .build();
                
                selfieDetails.add(detail);
            }
        }
        
        return selfieDetails;
    }
    
    private String getStringValue(CSVRecord record, String columnName) {
        try {
            String value = record.get(columnName);
            return (value == null || value.trim().isEmpty()) ? null : value.trim();
        } catch (Exception e) {
            return null;
        }
    }
    
    private Integer getIntegerValue(CSVRecord record, String columnName) {
        try {
            String value = record.get(columnName);
            if (value == null || value.trim().isEmpty()) {
                return 0;
            }
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }
    
    public int getTotalRecords(List<SelfieDetail> data) {
        return data.size();
    }
    
    public int getCompleteRegistrations(List<SelfieDetail> data) {
        return (int) data.stream()
                .filter(detail -> detail.getName() != null && !detail.getName().isEmpty() &&
                                detail.getEmail() != null && !detail.getEmail().isEmpty() &&
                                detail.getContact() != null && !detail.getContact().isEmpty())
                .count();
    }
    
    public int getRecordsWithEmail(List<SelfieDetail> data) {
        return (int) data.stream()
                .filter(detail -> detail.getEmail() != null && !detail.getEmail().isEmpty())
                .count();
    }
    
    public int getRecordsWithPhotos(List<SelfieDetail> data) {
        return (int) data.stream()
                .filter(detail -> detail.getPhotos() != null && detail.getPhotos() > 0)
                .count();
    }
    
    public double getAveragePhotos(List<SelfieDetail> data) {
        return data.stream()
                .filter(detail -> detail.getPhotos() != null)
                .mapToInt(SelfieDetail::getPhotos)
                .average()
                .orElse(0.0);
    }
}
