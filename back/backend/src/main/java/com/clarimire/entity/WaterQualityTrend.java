package com.clarimire.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WaterQualityTrend {
    private String reservoirName;
    private String date;
    private BigDecimal ammoniaNitrogen;
    private BigDecimal permanganateIndex;
    private BigDecimal cod;
    private BigDecimal totalNitrogen;
    private BigDecimal totalPhosphorus;
}
