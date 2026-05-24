package com.clarimire.service.impl;

import com.clarimire.entity.*;
import com.clarimire.mapper.*;
import com.clarimire.service.WarningService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WarningServiceImpl implements WarningService {

    @Autowired
    private WarningRecordMapper warningRecordMapper;

    @Autowired
    private WarningThresholdMapper warningThresholdMapper;

    @Autowired
    private SectionMonitorMapper sectionMonitorMapper;

    @Autowired
    private ReservoirMapper reservoirMapper;

    @Override
    public PageInfo<WarningRecord> getWarnings(int page, int pageSize, String level, String status,
                                               String startDate, String endDate) {
        PageHelper.startPage(page, pageSize);
        return new PageInfo<>(warningRecordMapper.findAll(level, status, startDate, endDate));
    }

    @Override
    public WarningRecord getWarningById(Integer id) {
        return warningRecordMapper.findById(id);
    }

    @Override
    public void updateWarning(WarningRecord warning) {
        warning.setUpdatedAt(LocalDateTime.now());
        warningRecordMapper.update(warning);
    }

    @Override
    public void deleteWarning(Integer id) {
        warningRecordMapper.deleteById(id);
    }

    @Override
    public boolean updateStatus(Integer id, String status) {
        WarningRecord warning = warningRecordMapper.findById(id);
        if (warning == null) {
            return false;
        }
        warning.setStatus(status);
        warning.setUpdatedAt(LocalDateTime.now());
        warningRecordMapper.update(warning);
        return true;
    }

    @Override
    @Transactional
    public int generateWarningsFromMonitorData() {
        WarningThreshold threshold = warningThresholdMapper.findLatest();
        if (threshold == null) {
            // 使用默认值
            threshold = new WarningThreshold();
            threshold.setCodThreshold(new BigDecimal("40"));
            threshold.setAmmoniaNitrogenThreshold(new BigDecimal("1.5"));
            threshold.setTotalPhosphorusThreshold(new BigDecimal("0.2"));
            threshold.setTotalNitrogenThreshold(new BigDecimal("2.0"));
            threshold.setPermanganateThreshold(new BigDecimal("10"));
        }

        List<SectionMonitor> monitorList = sectionMonitorMapper.findAll();
        int count = 0;

        for (SectionMonitor monitor : monitorList) {
            String warningType = null;
            BigDecimal indicatorValue = null;
            BigDecimal thresholdValue = null;
            String warningLevel = "low";

            if (monitor.getAmmoniaNitrogen() != null &&
                monitor.getAmmoniaNitrogen().compareTo(threshold.getAmmoniaNitrogenThreshold()) > 0) {
                warningType = "氨氮超标";
                indicatorValue = monitor.getAmmoniaNitrogen();
                thresholdValue = threshold.getAmmoniaNitrogenThreshold();
                if (indicatorValue.compareTo(thresholdValue.multiply(new BigDecimal("2"))) > 0) {
                    warningLevel = "critical";
                } else if (indicatorValue.compareTo(thresholdValue.multiply(new BigDecimal("1.5"))) > 0) {
                    warningLevel = "high";
                } else if (indicatorValue.compareTo(thresholdValue.multiply(new BigDecimal("1.2"))) > 0) {
                    warningLevel = "medium";
                }
            } else if (monitor.getCod() != null &&
                       monitor.getCod().compareTo(threshold.getCodThreshold()) > 0) {
                warningType = "COD超标";
                indicatorValue = monitor.getCod();
                thresholdValue = threshold.getCodThreshold();
            } else if (monitor.getTotalPhosphorus() != null &&
                       monitor.getTotalPhosphorus().compareTo(threshold.getTotalPhosphorusThreshold()) > 0) {
                warningType = "总磷超标";
                indicatorValue = monitor.getTotalPhosphorus();
                thresholdValue = threshold.getTotalPhosphorusThreshold();
            }

            if (warningType != null) {
                WarningRecord warning = new WarningRecord();
                warning.setWarningType(warningType);
                warning.setWarningLevel(warningLevel);
                warning.setReservoirName(monitor.getReservoirName());
                warning.setDescription(warningType + "，监测点: " + monitor.getMonitorPointName());
                warning.setIndicatorValue(indicatorValue);
                warning.setThresholdValue(thresholdValue);
                warning.setStatus("pending");
                warning.setCreatedAt(LocalDateTime.now());
                warning.setUpdatedAt(LocalDateTime.now());

                // 获取水库坐标
                Reservoir reservoir = reservoirMapper.findByName(monitor.getReservoirName());
                if (reservoir != null) {
                    warning.setReservoirId(reservoir.getId());
                }

                warningRecordMapper.insert(warning);
                count++;
            }
        }

        return count;
    }

    @Override
    public int countTodayWarnings() {
        return warningRecordMapper.countTodayWarnings();
    }

    @Override
    public List<WarningRecord> getLatestWarnings(int limit) {
        return warningRecordMapper.findLatest(limit);
    }

    @Override
    public WarningThreshold getThreshold() {
        WarningThreshold threshold = warningThresholdMapper.findLatest();
        if (threshold == null) {
            threshold = new WarningThreshold();
            threshold.setCodThreshold(new BigDecimal("40"));
            threshold.setAmmoniaNitrogenThreshold(new BigDecimal("1.5"));
            threshold.setTotalPhosphorusThreshold(new BigDecimal("0.2"));
            threshold.setTotalNitrogenThreshold(new BigDecimal("2.0"));
            threshold.setPermanganateThreshold(new BigDecimal("10"));
            threshold.setFloodLimitWaterLevel(new BigDecimal("592.0"));
        }
        return threshold;
    }

    @Override
    public void updateThreshold(WarningThreshold threshold) {
        if (threshold.getId() == null) {
            threshold.setCreatedAt(LocalDateTime.now());
        }
        threshold.setUpdatedAt(LocalDateTime.now());
        warningThresholdMapper.insertOrUpdate(threshold);
    }
}
