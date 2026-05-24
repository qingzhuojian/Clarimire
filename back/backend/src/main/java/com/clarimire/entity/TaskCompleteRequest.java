package com.clarimire.entity;

import lombok.Data;

@Data
public class TaskCompleteRequest {
    private String processingResult;
    private String description;
}
