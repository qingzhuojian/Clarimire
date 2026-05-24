package com.clarimire.service.impl;

import com.clarimire.entity.DashboardStats;
import com.clarimire.entity.WaterQualityTrend;
import com.clarimire.entity.WarningRecord;
import com.clarimire.mapper.*;
import com.clarimire.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ReservoirMapper reservoirMapper;

    @Autowired
    private SectionMonitorMapper sectionMonitorMapper;

    @Autowired
    private WarningRecordMapper warningRecordMapper;

    @Autowired
    private PatrolRecordMapper patrolRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private InspectionTaskMapper taskMapper;

    @Autowired
    private IssueReportMapper reportMapper;

    @Override
    public DashboardStats getStats() {
        // 监测点数 (监测断面数量)
        int monitorPointCount = sectionMonitorMapper.countDistinctPoints();

        // 今日预警数
        int todayWarningCount = warningRecordMapper.countTodayWarnings();

        // 待处理任务数 (巡查记录 + 巡查任务 + 问题上报)
        int pendingPatrolCount = patrolRecordMapper.countByStatus("pending");
        int pendingTaskCount = taskMapper != null ? taskMapper.countByStatus("pending") : 0;
        int pendingReportCount = reportMapper != null ? reportMapper.countByStatus("pending") : 0;
        int pendingTaskTotal = pendingPatrolCount + pendingTaskCount + pendingReportCount;

        // 活跃巡查员数 (24小时内有活动的巡查员)
        int activeInspectorCount = userMapper.countTodayActiveInspectors();

        return new DashboardStats(monitorPointCount, todayWarningCount, pendingTaskTotal, activeInspectorCount);
    }

    @Override
    public List<WaterQualityTrend> getWaterQualityTrend(String reservoirId, String startDate, String endDate) {
        return sectionMonitorMapper.findTrendByReservoir(reservoirId, startDate, endDate);
    }

    @Override
    public List<WarningRecord> getLatestWarnings(int limit) {
        return warningRecordMapper.findLatest(limit);
    }
}
