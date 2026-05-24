package com.clarimire.controller;

import com.clarimire.entity.DashboardStats;
import com.clarimire.entity.WaterQualityTrend;
import com.clarimire.entity.WarningRecord;
import com.clarimire.service.DashboardService;
import com.clarimire.service.PatrolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private PatrolService patrolService;

    @GetMapping("/stats")
    public DashboardStats getStats() {
        return dashboardService.getStats();
    }

    @GetMapping("/water-quality/trend")
    public List<WaterQualityTrend> getWaterQualityTrend(
            @RequestParam(required = false) String reservoirId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return dashboardService.getWaterQualityTrend(reservoirId, startDate, endDate);
    }

    @GetMapping("/warnings/latest")
    public List<WarningRecord> getLatestWarnings(@RequestParam(defaultValue = "5") int limit) {
        return dashboardService.getLatestWarnings(limit);
    }

    @GetMapping("/patrol/stats")
    public Map<String, Object> getPatrolStats(@RequestParam(required = false) String date) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayCheckins", patrolService.countActiveInspectorsToday());
        stats.put("pendingTasks", patrolService.countPendingTasks());
        return stats;
    }
}
