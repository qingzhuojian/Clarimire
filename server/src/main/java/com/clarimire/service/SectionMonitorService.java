package com.clarimire.service;

import com.clarimire.entity.SectionMonitor;
import com.clarimire.entity.ImportResult;
import java.util.List;

public interface SectionMonitorService {
    List<SectionMonitor> getList(String reservoirName, Integer year, Integer month);
    SectionMonitor getById(Integer id);
    boolean create(SectionMonitor sectionMonitor);
    boolean update(SectionMonitor sectionMonitor);
    boolean delete(Integer id);
    boolean deleteBatch(List<Integer> ids);
    ImportResult importExcel(String filePath);
    String exportExcel(List<Integer> ids);
    List<String> getDistinctReservoirs();
    List<Integer> getDistinctYears();
}
