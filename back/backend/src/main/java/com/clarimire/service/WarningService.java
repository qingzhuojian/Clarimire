package com.clarimire.service;

import com.clarimire.entity.WarningRecord;
import com.clarimire.entity.WarningThreshold;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface WarningService {
    PageInfo<WarningRecord> getWarnings(int page, int pageSize, String level, String status, String startDate, String endDate);
    WarningRecord getWarningById(Integer id);
    void updateWarning(WarningRecord warning);
    void deleteWarning(Integer id);
    boolean updateStatus(Integer id, String status);
    int generateWarningsFromMonitorData();
    int countTodayWarnings();
    List<WarningRecord> getLatestWarnings(int limit);

    WarningThreshold getThreshold();
    void updateThreshold(WarningThreshold threshold);
}
