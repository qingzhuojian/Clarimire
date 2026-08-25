package com.clarimire.service;

import com.clarimire.entity.OperationLog;
import java.util.List;

public interface OperationLogService {
    List<OperationLog> getList(String username, String startDate, String endDate);
    void addLog(String username, String operation, String module, String detail, String ip);
    boolean delete(Integer id);
}
