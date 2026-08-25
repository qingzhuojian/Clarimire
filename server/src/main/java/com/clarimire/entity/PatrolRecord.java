package com.clarimire.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PatrolRecord {
    private Integer id;
    private Integer taskId;
    private Integer userId;
    private String username;
    private String realName;
    private String reservoirName;
    private BigDecimal lat;
    private BigDecimal lng;
    private String locationZone;
    private String checkinMode;
    private Integer distanceM;
    private String photos;
    private String remark;
    private Date createTime;
}
