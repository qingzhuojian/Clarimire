package com.clarimire.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PatrolRecord {
    private Integer id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime date;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalDateTime time;

    private String reservoirName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private String inspector;
    private String inspectorUsername;
    private String status; // pending/processing/completed
    private Boolean hasIssue;
    private String issueType;
    private String issueSeverity; // low/medium/high/critical
    private String description;
    private Boolean hasPhoto;
    private String photoUrls;
    private String reporterName;
    private String reporterRole; // admin/inspector/public
    private String assignedInspector;
    private String assignmentNote;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignmentTime;

    private String situationDescription;
    private String processingResult; // resolved/improved/ongoing/reported

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
