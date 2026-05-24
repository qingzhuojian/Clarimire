package com.clarimire.controller;

import com.clarimire.entity.WarningRecord;
import com.clarimire.entity.WarningThreshold;
import com.clarimire.service.WarningService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/warnings")
@CrossOrigin(origins = "*")
public class WarningController {

    @Autowired
    private WarningService warningService;

    @GetMapping
    public PageInfo<WarningRecord> list(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int pageSize,
                                        @RequestParam(required = false) String level,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate) {
        return warningService.getWarnings(page, pageSize, level, status, startDate, endDate);
    }

    @GetMapping("/{id}")
    public WarningRecord getById(@PathVariable Integer id) {
        return warningService.getWarningById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody WarningRecord warning) {
        warning.setId(id);
        warningService.updateWarning(warning);
        return ResponseEntity.ok(Map.of("message", "更新成功"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        boolean success = warningService.updateStatus(id, status);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "状态更新成功"));
        }
        return ResponseEntity.status(400).body(Map.of("message", "更新失败"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        warningService.deleteWarning(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateWarnings() {
        int count = warningService.generateWarningsFromMonitorData();
        return ResponseEntity.ok(Map.of("message", "生成" + count + "条预警记录"));
    }
}
