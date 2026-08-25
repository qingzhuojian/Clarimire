package com.clarimire.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class SectionMonitor {
    private Integer id;
    private String monitorPointName;
    private String reservoirName;
    private Integer year;
    private Integer month;
    private BigDecimal oxygen;
    private BigDecimal potassiumPermanganate;
    private BigDecimal cod;
    private BigDecimal flow;
    private BigDecimal waterDepth;
    private BigDecimal totalNitrogen;
    private BigDecimal totalPhosphorus;
    private BigDecimal ammoniaNitrogen;
    private Date createTime;
    private Date updateTime;
}
