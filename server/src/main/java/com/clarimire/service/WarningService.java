package com.clarimire.service;

import com.clarimire.entity.WarningRecord;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WarningService {
    List<WarningRecord> getList(String reservoirName);

    Map<String, Object> evaluateWaterLevel(String reservoirName, BigDecimal waterLevel);

    Map<String, Object> evaluateEnvironment(String reservoirName, String indicator, BigDecimal value);
}
