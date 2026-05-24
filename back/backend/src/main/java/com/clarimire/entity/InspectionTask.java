package com.clarimire.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InspectionTask {
    private Integer id;
    private String title;
    private String description;
    private String reservoirName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status; // pending/processing/completed

    private Integer creatorId;
    private String creatorName;

    private Integer assigneeId;
    private String assigneeName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
