package com.clarimire.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class WarningRule {
    private Integer id;
    private String ruleName;
    private String indicator;
    private BigDecimal yellowThreshold;
    private BigDecimal orangeThreshold;
    private BigDecimal redThreshold;
    private Integer enabled;
    private Date createTime;
    private Date updateTime;
}
