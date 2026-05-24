package com.clarimire.service.impl;

import com.clarimire.entity.PatrolRecord;
import com.clarimire.mapper.PatrolRecordMapper;
import com.clarimire.service.PatrolService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class PatrolServiceImpl implements PatrolService {

    @Autowired
    private PatrolRecordMapper patrolRecordMapper;

    @Override
    public void createRecord(PatrolRecord record) {
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }
        if (record.getUpdatedAt() == null) {
            record.setUpdatedAt(LocalDateTime.now());
        }
        patrolRecordMapper.insert(record);
    }

    @Override
    public PatrolRecord getRecordById(Integer id) {
        return patrolRecordMapper.findById(id);
    }

    @Override
    public PageInfo<PatrolRecord> getRecords(int page, int pageSize, String status, String inspector,
                                            String startDate, String endDate) {
        PageHelper.startPage(page, pageSize);
        return new PageInfo<>(patrolRecordMapper.findAll(status, inspector, startDate, endDate));
    }

    @Override
    public void updateRecord(PatrolRecord record) {
        record.setUpdatedAt(LocalDateTime.now());
        patrolRecordMapper.update(record);
    }

    @Override
    public void deleteRecord(Integer id) {
        patrolRecordMapper.deleteById(id);
    }

    @Override
    public boolean assignTask(Integer recordId, String inspector, String note) {
        PatrolRecord record = patrolRecordMapper.findById(recordId);
        if (record == null) {
            return false;
        }
        record.setAssignedInspector(inspector);
        record.setAssignmentNote(note);
        record.setAssignmentTime(LocalDateTime.now());
        record.setStatus("processing");
        record.setUpdatedAt(LocalDateTime.now());
        patrolRecordMapper.update(record);
        return true;
    }

    @Override
    public boolean updateStatus(Integer id, String status, String situationDescription) {
        PatrolRecord record = patrolRecordMapper.findById(id);
        if (record == null) {
            return false;
        }
        record.setStatus(status);
        if (situationDescription != null) {
            record.setSituationDescription(situationDescription);
        }
        if ("completed".equals(status)) {
            record.setCompletionTime(LocalDateTime.now());
            record.setProcessingResult("resolved");
        }
        record.setUpdatedAt(LocalDateTime.now());
        patrolRecordMapper.update(record);
        return true;
    }

    @Override
    public int countPendingTasks() {
        return patrolRecordMapper.countByStatus("pending");
    }

    @Override
    public int countActiveInspectorsToday() {
        return patrolRecordMapper.countActiveInspectorsToday();
    }
}
