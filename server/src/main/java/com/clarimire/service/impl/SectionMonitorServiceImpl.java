package com.clarimire.service.impl;

import com.clarimire.entity.SectionMonitor;
import com.clarimire.entity.ImportResult;
import com.clarimire.mapper.SectionMonitorMapper;
import com.clarimire.service.SectionMonitorService;
import com.clarimire.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SectionMonitorServiceImpl implements SectionMonitorService {

    @Autowired
    private SectionMonitorMapper sectionMonitorMapper;

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Override
    public List<SectionMonitor> getList(String reservoirName, Integer year, Integer month) {
        return sectionMonitorMapper.selectList(reservoirName, year, month);
    }

    @Override
    public SectionMonitor getById(Integer id) {
        return sectionMonitorMapper.selectById(id);
    }

    @Override
    public boolean create(SectionMonitor sectionMonitor) {
        return sectionMonitorMapper.insert(sectionMonitor) > 0;
    }

    @Override
    public boolean update(SectionMonitor sectionMonitor) {
        return sectionMonitorMapper.update(sectionMonitor) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return sectionMonitorMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteBatch(List<Integer> ids) {
        return sectionMonitorMapper.deleteBatch(ids) > 0;
    }

    @Override
    public ImportResult importExcel(String filePath) {
        try {
            List<SectionMonitor> dataList = ExcelUtil.readSectionMonitorFromExcel(filePath);
            if (dataList == null || dataList.isEmpty()) {
                return new ImportResult(false, "导入数据为空");
            }
            int imported = sectionMonitorMapper.insertBatch(dataList);
            return new ImportResult(imported, 0, new ArrayList<>(), "成功导入" + imported + "条数据");
        } catch (Exception e) {
            return new ImportResult(false, "导入失败: " + e.getMessage());
        }
    }

    @Override
    public String exportExcel(List<Integer> ids) {
        try {
            String fileName = UUID.randomUUID().toString() + ".xlsx";
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String filePath = uploadPath + File.separator + fileName;
            List<SectionMonitor> list;
            if (ids == null || ids.isEmpty()) {
                list = sectionMonitorMapper.selectList(null, null, null);
            } else {
                list = new ArrayList<>();
                for (Integer id : ids) {
                    SectionMonitor sm = sectionMonitorMapper.selectById(id);
                    if (sm != null) {
                        list.add(sm);
                    }
                }
            }
            ExcelUtil.writeSectionMonitorToExcel(list, filePath);
            return filePath;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> getDistinctReservoirs() {
        return sectionMonitorMapper.selectDistinctReservoirs();
    }

    @Override
    public List<Integer> getDistinctYears() {
        return sectionMonitorMapper.selectDistinctYears();
    }
}
