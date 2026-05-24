package com.clarimire.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WarningThreshold {
    private Integer id;
    private BigDecimal codThreshold;
    private BigDecimal ammoniaNitrogenThreshold;
    private BigDecimal totalPhosphorusThreshold;
    private BigDecimal totalNitrogenThreshold;
    private BigDecimal permanganateThreshold;
    private BigDecimal floodLimitWaterLevel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
