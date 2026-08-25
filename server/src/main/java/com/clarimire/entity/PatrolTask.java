package com.clarimire.entity;

import lombok.Data;
import java.util.Date;

@Data
public class PatrolTask {
    private Integer id;
    private String title;
    private String taskType;
    private String status;
    private String reservoirName;
    private Integer assigneeId;
    private String assigneeName;
    private String createdBy;
    private Date dueTime;
    private String description;
    private Integer warningRecordId;
    private Date createTime;
    private Date updateTime;
}
