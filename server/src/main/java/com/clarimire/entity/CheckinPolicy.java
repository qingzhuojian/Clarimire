package com.clarimire.entity;

import lombok.Data;
import java.util.Date;

@Data
public class CheckinPolicy {
    private Integer id;
    private Integer coreRadiusM;
    private Integer bufferRadiusM;
    private Integer remoteEnabled;
    private Integer demoMode;
    private Date updateTime;
}
