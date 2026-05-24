package com.clarimire.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class IssueReport {
    private Integer id;
    private String reservoirName;
    private String description;
    private String severity; // low/medium/high/critical
    private String notes;
    private String photos; // JSON数组
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private String reporterName;
    private String reporterRole; // admin/inspector/public
    private String reporterUsername;
    private String status; // pending/processing/completed
    private String assignedInspector;
    private String assignmentNote;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignmentTime;

    private String processingResult;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
