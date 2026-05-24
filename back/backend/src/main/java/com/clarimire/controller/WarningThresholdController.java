package com.clarimire.controller;

import com.clarimire.entity.WarningThreshold;
import com.clarimire.service.WarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/warning-thresholds")
@CrossOrigin(origins = "*")
public class WarningThresholdController {

    @Autowired
    private WarningService warningService;

    @GetMapping
    public WarningThreshold getThreshold() {
        return warningService.getThreshold();
    }

    @PutMapping
    public ResponseEntity<?> updateThreshold(@RequestBody WarningThreshold threshold) {
        warningService.updateThreshold(threshold);
        return ResponseEntity.ok(Map.of("message", "阈值更新成功"));
    }
}
