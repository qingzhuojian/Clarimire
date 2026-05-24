package com.clarimire.entity;

import lombok.Data;

@Data
public class ProcessReportRequest {
    private String processingResult;
    private String description;
    private String assignedInspector;
    private String assignmentNote;
}
