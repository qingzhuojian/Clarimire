package com.clarimire.entity;

import lombok.Data;

@Data
public class TaskFeedbackRequest {
    private String content;
    private String photos; // JSON array string
    private String inspector;
    private String inspectorUsername;
}
