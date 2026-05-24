package com.clarimire.entity;

import lombok.Data;

@Data
public class IssueReportRequest {
    private String reservoirName;
    private String description;
    private String severity;
    private String notes;
    private String photos; // JSON array string
    private Double latitude;
    private Double longitude;
    private String address;
    private String reporterName;
    private String reporterRole;
    private String reporterUsername;
}
