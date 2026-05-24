package com.clarimire.entity;

import lombok.Data;

@Data
public class ReportToTaskRequest {
    private Integer assigneeId;
    private String assigneeName;
    private String note;
}
