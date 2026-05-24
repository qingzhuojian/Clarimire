package com.clarimire.service;

import com.clarimire.entity.PatrolRecord;
import com.github.pagehelper.PageInfo;

public interface PatrolService {
    void createRecord(PatrolRecord record);
    PatrolRecord getRecordById(Integer id);
    PageInfo<PatrolRecord> getRecords(int page, int pageSize, String status, String inspector, String startDate, String endDate);
    void updateRecord(PatrolRecord record);
    void deleteRecord(Integer id);
    boolean assignTask(Integer recordId, String inspector, String note);
    boolean updateStatus(Integer id, String status, String situationDescription);
    int countPendingTasks();
    int countActiveInspectorsToday();
}
