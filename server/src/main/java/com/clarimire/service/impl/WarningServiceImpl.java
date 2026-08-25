package com.clarimire.service.impl;

import com.clarimire.entity.WaterReservoir;
import com.clarimire.entity.WarningRecord;
import com.clarimire.entity.WarningRule;
import com.clarimire.mapper.WarningRecordMapper;
import com.clarimire.mapper.WarningRuleMapper;
import com.clarimire.mapper.WaterReservoirMapper;
import com.clarimire.service.WarningService;
import com.clarimire.util.ReservoirNameUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WarningServiceImpl implements WarningService {

    @Autowired
    private WarningRecordMapper warningRecordMapper;

    @Autowired
    private WaterReservoirMapper waterReservoirMapper;

    @Autowired
    private WarningRuleMapper warningRuleMapper;

    @Override
    public List<WarningRecord> getList(String reservoirName) {
        return warningRecordMapper.selectList(reservoirName);
    }

    @Override
    public Map<String, Object> evaluateWaterLevel(String reservoirName, BigDecimal waterLevel) {
        String name = ReservoirNameUtil.normalize(reservoirName);
        WaterReservoir reservoir = findReservoir(name);

        String level = "正常";
        String levelCode = "blue";
        String summary = "输入水位处于安全范围。";

        if (reservoir != null && waterLevel != null) {
            BigDecimal flood = reservoir.getFloodLevel();
            BigDecimal max = reservoir.getMaxLevel();

            if (max != null && waterLevel.compareTo(max) > 0) {
                level = "危险";
                levelCode = "red";
                summary = "输入水位已超过历史最高水位，请立即启动应急响应。";
            } else if (flood != null && waterLevel.compareTo(flood) >= 0) {
                level = "警戒";
                levelCode = "orange";
                summary = "输入水位已达汛限水位，需加强监测并启动警戒预案。";
            }
        }

        WarningRecord record = new WarningRecord();
        record.setReservoirName(name);
        record.setWarningType("水位预警");
        record.setWarningLevel(level);
        record.setIndicator("water_level");
        record.setCurrentValue(waterLevel);
        record.setThresholdValue(reservoir != null ? reservoir.getFloodLevel() : null);
        record.setDescription(summary);
        record.setSuggestion(levelCode.equals("red") ? "立即启动最高级应急响应" : "加强监测");
        warningRecordMapper.insert(record);

        Map<String, Object> result = new HashMap<>();
        result.put("reservoirName", name);
        result.put("inputValue", waterLevel);
        result.put("floodLimit", reservoir != null ? reservoir.getFloodLevel() : null);
        result.put("maxLevel", reservoir != null ? reservoir.getMaxLevel() : null);
        result.put("avgLevel", reservoir != null ? reservoir.getAvgLevel() : null);
        result.put("status", level);
        result.put("statusClass", levelCode);
        result.put("summary", summary);
        return result;
    }

    @Override
    public Map<String, Object> evaluateEnvironment(String reservoirName, String indicator, BigDecimal value) {
        String name = ReservoirNameUtil.normalize(reservoirName);
        WarningRule rule = warningRuleMapper.selectByIndicator(indicator);

        String level = "正常";
        String levelCode = "blue";
        String summary = "指标处于安全范围。";
        BigDecimal threshold = null;

        if (rule != null && value != null) {
            threshold = rule.getYellowThreshold();
            if (rule.getRedThreshold() != null && value.compareTo(rule.getRedThreshold()) >= 0) {
                level = "红色";
                levelCode = "red";
                threshold = rule.getRedThreshold();
                summary = rule.getRuleName() + "超过红色阈值，存在严重污染风险。";
            } else if (rule.getOrangeThreshold() != null && value.compareTo(rule.getOrangeThreshold()) >= 0) {
                level = "橙色";
                levelCode = "orange";
                threshold = rule.getOrangeThreshold();
                summary = rule.getRuleName() + "超过橙色阈值，需启动预警响应。";
            } else if (rule.getYellowThreshold() != null && value.compareTo(rule.getYellowThreshold()) >= 0) {
                level = "黄色";
                levelCode = "yellow";
                threshold = rule.getYellowThreshold();
                summary = rule.getRuleName() + "超过黄色阈值，请加强监测。";
            }
        }

        WarningRecord record = new WarningRecord();
        record.setReservoirName(name);
        record.setWarningType("水质异常");
        record.setWarningLevel(level);
        record.setIndicator(indicator);
        record.setCurrentValue(value);
        record.setThresholdValue(threshold);
        record.setDescription(summary);
        record.setSuggestion(levelCode.equals("red") ? "立即排查污染源并启动应急" : "持续跟踪水质变化");
        warningRecordMapper.insert(record);

        Map<String, Object> result = new HashMap<>();
        result.put("reservoirName", name);
        result.put("indicator", indicator);
        result.put("inputValue", value);
        result.put("yellowThreshold", rule != null ? rule.getYellowThreshold() : null);
        result.put("orangeThreshold", rule != null ? rule.getOrangeThreshold() : null);
        result.put("redThreshold", rule != null ? rule.getRedThreshold() : null);
        result.put("status", level);
        result.put("statusClass", levelCode);
        result.put("summary", summary);
        return result;
    }

    private WaterReservoir findReservoir(String name) {
        for (WaterReservoir item : waterReservoirMapper.selectAll()) {
            if (name.equals(item.getReservoirName())) {
                return item;
            }
        }
        return null;
    }
}
