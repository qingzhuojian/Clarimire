package com.clarimire.service.impl;

import com.clarimire.entity.SimulationConfig;
import com.clarimire.mapper.SimulationConfigMapper;
import com.clarimire.service.SimulationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SimulationConfigServiceImpl implements SimulationConfigService {

    @Autowired
    private SimulationConfigMapper simulationConfigMapper;

    @Override
    public List<SimulationConfig> getAllConfigs() {
        return simulationConfigMapper.findAll();
    }

    @Override
    public SimulationConfig getDefaultConfig() {
        SimulationConfig config = simulationConfigMapper.findDefault();
        if (config == null) {
            // 创建默认配置
            config = new SimulationConfig();
            config.setConfigName("默认配置");
            config.setDiffusionRadius(new java.math.BigDecimal("1000"));
            config.setDecayCoefficient(new java.math.BigDecimal("0.01"));
            config.setSimulationDuration(3600);
            config.setIsDefault(true);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            simulationConfigMapper.insert(config);
        }
        return config;
    }

    @Override
    public SimulationConfig getConfigById(Integer id) {
        return simulationConfigMapper.findById(id);
    }

    @Override
    public SimulationConfig createConfig(SimulationConfig config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        simulationConfigMapper.insert(config);
        return config;
    }

    @Override
    public void updateConfig(SimulationConfig config) {
        config.setUpdatedAt(LocalDateTime.now());
        simulationConfigMapper.update(config);
    }

    @Override
    public void deleteConfig(Integer id) {
        simulationConfigMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void setDefaultConfig(Integer id) {
        // 取消所有默认
        simulationConfigMapper.clearAllDefaults();
        // 设置新的默认
        simulationConfigMapper.setDefault(id);
    }
}
