package com.clarimire.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SectionMonitor {
    private Integer id;
    private String monitorPointName;
    private String reservoirName;
    private Integer year;
    private Integer month;
    private BigDecimal ammoniaNitrogen;
    private BigDecimal permanganateIndex;
    private BigDecimal cod;
    private BigDecimal flowRate;
    private BigDecimal waterDepth;
    private BigDecimal totalNitrogen;
    private BigDecimal totalPhosphorus;

    // 兼容旧字段名
    private BigDecimal oxygen;
    private BigDecimal potassiumPermanganate;
    private BigDecimal flow;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
