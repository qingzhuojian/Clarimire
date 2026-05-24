package com.clarimire.service.impl;

import com.clarimire.entity.IssueReport;
import com.clarimire.mapper.IssueReportMapper;
import com.clarimire.service.IssueReportService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class IssueReportServiceImpl implements IssueReportService {

    @Autowired
    private IssueReportMapper reportMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void createReport(IssueReport report) {
        if (report.getStatus() == null) {
            report.setStatus("pending");
        }
        LocalDateTime now = LocalDateTime.now();
        if (report.getCreatedAt() == null) {
            report.setCreatedAt(now);
        }
        if (report.getUpdatedAt() == null) {
            report.setUpdatedAt(now);
        }
        reportMapper.insert(report);
    }

    @Override
    public IssueReport getReportById(Integer id) {
        return reportMapper.findById(id);
    }

    @Override
    public PageInfo<IssueReport> getReports(int page, int pageSize, String status, String severity,
                                             String reporterUsername, String startDate, String endDate) {
        PageHelper.startPage(page, pageSize);
        List<IssueReport> reports = reportMapper.findAll(status, severity, reporterUsername, null, startDate, endDate);
        return new PageInfo<>(reports);
    }

    @Override
    public List<IssueReport> getMyReports(String reporterUsername) {
        return reportMapper.findByReporter(reporterUsername);
    }

    @Override
    public List<IssueReport> getPendingReports() {
        return reportMapper.findPending();
    }

    @Override
    public void updateReport(IssueReport report) {
        report.setUpdatedAt(LocalDateTime.now());
        reportMapper.update(report);
    }

    @Override
    public void deleteReport(Integer id) {
        reportMapper.deleteById(id);
    }

    @Override
    public boolean assignReport(Integer id, String assignedInspector, String note) {
        IssueReport report = reportMapper.findById(id);
        if (report == null) {
            return false;
        }
        reportMapper.assign(id, assignedInspector, note);
        return true;
    }

    @Override
    public boolean processReport(Integer id, String processingResult) {
        IssueReport report = reportMapper.findById(id);
        if (report == null) {
            return false;
        }
        reportMapper.complete(id, processingResult);
        return true;
    }

    @Override
    public boolean convertToTask(Integer reportId, Integer assigneeId, String assigneeName, String note) {
        IssueReport report = reportMapper.findById(reportId);
        if (report == null) {
            return false;
        }
        reportMapper.assign(reportId, assigneeName, note);
        return true;
    }

    @Override
    public int countPendingReports() {
        return reportMapper.countByStatus("pending");
    }
}
