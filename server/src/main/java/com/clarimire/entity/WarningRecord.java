package com.clarimire.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class WarningRecord {
    private Integer id;
    private String reservoirName;
    private String warningType;
    private String warningLevel;
    private String indicator;
    private BigDecimal currentValue;
    private BigDecimal thresholdValue;
    private String description;
    private String suggestion;
    private Date createTime;
}
