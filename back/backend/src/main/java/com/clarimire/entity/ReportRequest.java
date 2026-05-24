package com.clarimire.entity;

import lombok.Data;

@Data
public class ReportRequest {
    private String reservoirName;
    private String description;
    private String photos; // JSON array string
    private String severity;
    private String reporterName;
    private String reporterRole;
    private Double latitude;
    private Double longitude;
    private String address;
}
