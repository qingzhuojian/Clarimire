package com.clarimire.service;

import com.clarimire.entity.WaterSituation;
import com.clarimire.entity.ImportResult;
import java.util.List;

public interface WaterSituationService {
    List<WaterSituation> getList(String reservoirName, String startDate, String endDate);
    WaterSituation getById(Integer id);
    boolean create(WaterSituation waterSituation);
    boolean update(WaterSituation waterSituation);
    boolean delete(Integer id);
    boolean deleteBatch(List<Integer> ids);
    ImportResult importExcel(String filePath);
    String exportExcel(List<Integer> ids);
    List<String> getDistinctReservoirs();
}
