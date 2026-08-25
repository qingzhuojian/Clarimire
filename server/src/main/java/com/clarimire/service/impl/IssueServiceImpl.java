package com.clarimire.service.impl;

import com.clarimire.entity.*;
import com.clarimire.mapper.CheckinPolicyMapper;
import com.clarimire.mapper.IssueReportMapper;
import com.clarimire.mapper.PatrolTaskMapper;
import com.clarimire.mapper.UserMapper;
import com.clarimire.service.IssueService;
import com.clarimire.util.ReservoirNameUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IssueServiceImpl implements IssueService {

    @Autowired
    private IssueReportMapper issueReportMapper;

    @Autowired
    private PatrolTaskMapper patrolTaskMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CheckinPolicyMapper checkinPolicyMapper;

    @Override
    public List<IssueReport> getList(String status, String issueType, Integer reporterId) {
        return issueReportMapper.findList(status, issueType, reporterId);
    }

    @Override
    public IssueReport getById(Integer id) {
        return issueReportMapper.findById(id);
    }

    @Override
    public boolean create(IssueReport report) {
        if (report.getStatus() == null) {
            report.setStatus("pending");
        }
        // 巡查员当场处理：直接闭环
        if ("resolved".equals(report.getStatus()) || "onsite".equals(report.getStatus())) {
            report.setStatus("resolved");
        }
        if (report.getIssueType() == null) {
            report.setIssueType("public");
        }
        if (report.getReservoirName() != null) {
            report.setReservoirName(ReservoirNameUtil.normalize(report.getReservoirName()));
        }
        return issueReportMapper.insert(report) > 0;
    }

    @Override
    public boolean update(IssueReport report) {
        if (report.getReservoirName() != null) {
            report.setReservoirName(ReservoirNameUtil.normalize(report.getReservoirName()));
        }
        return issueReportMapper.update(report) > 0;
    }

    @Override
    public Map<String, Object> review(Integer id, String action, String adminRemark, Integer assigneeId, String operator) {
        Map<String, Object> result = new HashMap<>();
        IssueReport report = issueReportMapper.findById(id);
        if (report == null) {
            result.put("success", false);
            result.put("message", "记录不存在");
            return result;
        }

        report.setAdminRemark(adminRemark);

        switch (action) {
            case "approve":
                report.setStatus("reviewing");
                break;
            case "assign":
                if (assigneeId == null) {
                    result.put("success", false);
                    result.put("message", "请指定巡查员");
                    return result;
                }
                User assignee = userMapper.findById(assigneeId);
                if (assignee == null) {
                    result.put("success", false);
                    result.put("message", "巡查员不存在");
                    return result;
                }
                report.setAssignedTo(assigneeId);
                report.setAssignedName(assignee.getRealName());
                report.setStatus("assigned");

                PatrolTask task = new PatrolTask();
                task.setTitle("处理问题: " + report.getTitle());
                task.setTaskType("emergency");
                task.setStatus("assigned");
                task.setReservoirName(report.getReservoirName());
                task.setAssigneeId(assigneeId);
                task.setAssigneeName(assignee.getRealName());
                task.setCreatedBy(operator);
                task.setDescription(report.getDescription());
                patrolTaskMapper.insert(task);
                report.setPatrolTaskId(task.getId());
                break;
            case "resolve":
                report.setStatus("resolved");
                break;
            case "close":
                report.setStatus("closed");
                break;
            default:
                result.put("success", false);
                result.put("message", "未知操作");
                return result;
        }

        issueReportMapper.update(report);
        result.put("success", true);
        result.put("message", "操作成功");
        result.put("data", report);
        return result;
    }

    @Override
    public CheckinPolicy getPolicy() {
        return checkinPolicyMapper.getPolicy();
    }

    @Override
    public boolean updatePolicy(CheckinPolicy policy) {
        return checkinPolicyMapper.updatePolicy(policy) > 0;
    }

    @Override
    public List<ReservoirLocation> getReservoirLocations() {
        return checkinPolicyMapper.findAllLocations();
    }
}
