package com.clarimire.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class WaterReservoir {
    private Integer id;
    private String reservoirName;
    private String shortName;
    private String location;
    private BigDecimal capacity;
    private BigDecimal floodLevel;
    private BigDecimal maxLevel;
    private BigDecimal avgLevel;
    private BigDecimal normalLevel;
    private String type;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
