package com.clarimire.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WarningRecord {
    private Integer id;
    private String warningType;
    private String warningLevel; // low/medium/high/critical
    private Integer reservoirId;
    private String reservoirName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private BigDecimal indicatorValue;
    private BigDecimal thresholdValue;
    private String status; // pending/processed/dismissed

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
