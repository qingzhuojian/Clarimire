package com.clarimire.entity;

import lombok.Data;
import java.util.List;

@Data
public class DashboardStats {
    private int monitorPointCount;
    private int todayWarningCount;
    private int pendingTaskCount;
    private int activeInspectorCount;
    private int totalReservoirs;
    private int todayPatrolCount;
    private int pendingReportCount;
    private int completedTaskCount;

    public DashboardStats() {}

    public DashboardStats(int monitorPointCount, int todayWarningCount, int pendingTaskCount, int activeInspectorCount) {
        this.monitorPointCount = monitorPointCount;
        this.todayWarningCount = todayWarningCount;
        this.pendingTaskCount = pendingTaskCount;
        this.activeInspectorCount = activeInspectorCount;
    }
}
