package com.clarimire.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Reservoir {
    private Integer id;
    private String reservoirName;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal capacity;
    private String status;
    private String constructionDate;
    private String lastMaintenanceDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
