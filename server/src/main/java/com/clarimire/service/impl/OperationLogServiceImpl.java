package com.clarimire.service.impl;

import com.clarimire.entity.OperationLog;
import com.clarimire.mapper.OperationLogMapper;
import com.clarimire.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public List<OperationLog> getList(String username, String startDate, String endDate) {
        return operationLogMapper.selectList(username, startDate, endDate);
    }

    @Override
    public void addLog(String username, String operation, String module, String detail, String ip) {
        OperationLog log = new OperationLog();
        log.setUsername(username);
        log.setOperation(operation);
        log.setModule(module);
        log.setDetail(detail);
        log.setIp(ip);
        operationLogMapper.insert(log);
    }

    @Override
    public boolean delete(Integer id) {
        return operationLogMapper.deleteById(id) > 0;
    }
}
