package com.clarimire.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class IssueReport {
    private Integer id;
    private String title;
    private String issueType;
    private String status;
    private Integer reporterId;
    private String reporterName;
    private String reservoirName;
    private BigDecimal lat;
    private BigDecimal lng;
    private String description;
    private String photos;
    private Integer assignedTo;
    private String assignedName;
    private Integer patrolTaskId;
    private String adminRemark;
    private Date createTime;
    private Date updateTime;
}
