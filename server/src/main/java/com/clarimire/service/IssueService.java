package com.clarimire.service;

import com.clarimire.entity.CheckinPolicy;
import com.clarimire.entity.IssueReport;
import com.clarimire.entity.ReservoirLocation;

import java.util.List;
import java.util.Map;

public interface IssueService {
    List<IssueReport> getList(String status, String issueType, Integer reporterId);

    IssueReport getById(Integer id);

    boolean create(IssueReport report);

    boolean update(IssueReport report);

    Map<String, Object> review(Integer id, String action, String adminRemark, Integer assigneeId, String operator);

    CheckinPolicy getPolicy();

    boolean updatePolicy(CheckinPolicy policy);

    List<ReservoirLocation> getReservoirLocations();
}
