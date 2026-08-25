package com.clarimire.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class WaterSituation {
    private Integer id;
    private String reservoirName;
    private Date date;
    private BigDecimal waterLevel;
    private BigDecimal storage;
    private BigDecimal avgInflow;
    private BigDecimal avgOutflow;
    private BigDecimal yoyIncrease;
    private BigDecimal totalCapacity;
    private BigDecimal floodLevel;
    private Date createTime;
    private Date updateTime;
}
