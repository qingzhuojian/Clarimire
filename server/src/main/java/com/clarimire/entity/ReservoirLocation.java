package com.clarimire.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReservoirLocation {
    private Integer id;
    private String reservoirName;
    private BigDecimal lat;
    private BigDecimal lng;
}
