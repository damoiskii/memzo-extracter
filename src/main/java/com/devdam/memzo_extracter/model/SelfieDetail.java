package com.devdam.memzo_extracter.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing selfie details extracted from CSV data.
 * Contains information about selfie images, user details, and associated metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfieDetail {
    private String image;
    private String name;
    private String email;
    private String contact;
    private String downloadRequests;
    private Integer photos;
    private String photosShared;
    private String photosDownload;
    private String date;
    
    // Utility method to parse date string to LocalDateTime
    public LocalDateTime getParsedDate() {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm");
            return LocalDateTime.parse(date, formatter);
        } catch (Exception e) {
            return null;
        }
    }
    
    // Utility method to check if this record has user information
    public boolean hasUserInfo() {
        return (name != null && !name.trim().isEmpty()) || 
               (email != null && !email.trim().isEmpty()) || 
               (contact != null && !contact.trim().isEmpty());
    }
    
    // Utility method to get the image filename from URL
    public String getImageFilename() {
        if (image == null || image.trim().isEmpty()) {
            return null;
        }

        String[] parts = image.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : null;
    }
}
