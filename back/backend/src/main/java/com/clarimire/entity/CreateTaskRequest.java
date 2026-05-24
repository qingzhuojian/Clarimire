package com.clarimire.entity;

import lombok.Data;

@Data
public class CreateTaskRequest {
    private String title;
    private String description;
    private String reservoirName;
    private Double latitude;
    private Double longitude;
    private Integer assigneeId;
    private String assigneeName;
    private String deadline;
}
