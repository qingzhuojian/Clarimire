package com.clarimire.service;

import com.clarimire.entity.IssueReport;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface IssueReportService {
    void createReport(IssueReport report);
    IssueReport getReportById(Integer id);
    PageInfo<IssueReport> getReports(int page, int pageSize, String status, String severity, String reporterUsername, String startDate, String endDate);
    List<IssueReport> getMyReports(String reporterUsername);
    List<IssueReport> getPendingReports();
    void updateReport(IssueReport report);
    void deleteReport(Integer id);
    boolean assignReport(Integer id, String assignedInspector, String note);
    boolean processReport(Integer id, String processingResult);
    boolean convertToTask(Integer reportId, Integer assigneeId, String assigneeName, String note);
    int countPendingReports();
}
