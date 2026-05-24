package com.clarimire.service;

import com.clarimire.entity.DashboardStats;
import com.clarimire.entity.WaterQualityTrend;
import com.clarimire.entity.WarningRecord;
import java.util.List;

public interface DashboardService {
    DashboardStats getStats();
    List<WaterQualityTrend> getWaterQualityTrend(String reservoirId, String startDate, String endDate);
    List<WarningRecord> getLatestWarnings(int limit);
}
