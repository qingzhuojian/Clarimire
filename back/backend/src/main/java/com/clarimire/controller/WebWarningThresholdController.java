package com.clarimire.controller;

import com.clarimire.entity.WarningThreshold;
import com.clarimire.mapper.WarningThresholdMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/web/warning-thresholds")
@CrossOrigin(origins = "*")
public class WebWarningThresholdController {

    @Autowired
    private WarningThresholdMapper thresholdMapper;

    @GetMapping
    public WarningThreshold get() {
        WarningThreshold threshold = thresholdMapper.findCurrent();
        if (threshold == null) {
            threshold = new WarningThreshold();
            threshold.setId(1);
            threshold.setCodThreshold(java.math.BigDecimal.valueOf(40.0000));
            threshold.setAmmoniaNitrogenThreshold(java.math.BigDecimal.valueOf(1.5000));
            threshold.setTotalPhosphorusThreshold(java.math.BigDecimal.valueOf(0.2000));
            threshold.setTotalNitrogenThreshold(java.math.BigDecimal.valueOf(2.0000));
            threshold.setPermanganateThreshold(java.math.BigDecimal.valueOf(10.0000));
            threshold.setFloodLimitWaterLevel(java.math.BigDecimal.valueOf(592.00));
            thresholdMapper.insert(threshold);
        }
        return threshold;
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody WarningThreshold threshold) {
        thresholdMapper.update(threshold);
        return ResponseEntity.ok(Map.of("message", "阈值更新成功"));
    }
}
