package com.clarimire.entity;

import lombok.Data;

@Data
public class TaskFeedback {
    private Integer id;
    private Integer taskId;
    private String content;
    private String photos; // JSON array
    private String inspector;
    private String inspectorUsername;
    private String createdAt;
}
