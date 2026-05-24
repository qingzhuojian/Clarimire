package com.clarimire.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SimulationConfig {
    private Integer id;
    private String configName;
    private BigDecimal diffusionRadius;
    private BigDecimal decayCoefficient;
    private BigDecimal windSpeed;
    private Integer windDirection;
    private BigDecimal waterFlowRate;
    private Integer simulationDuration;
    private Boolean isDefault;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
