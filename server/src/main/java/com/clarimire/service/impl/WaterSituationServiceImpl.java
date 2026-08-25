package com.clarimire.service.impl;

import com.clarimire.entity.WaterSituation;
import com.clarimire.entity.ImportResult;
import com.clarimire.mapper.WaterSituationMapper;
import com.clarimire.service.WaterSituationService;
import com.clarimire.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WaterSituationServiceImpl implements WaterSituationService {

    @Autowired
    private WaterSituationMapper waterSituationMapper;

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Override
    public List<WaterSituation> getList(String reservoirName, String startDate, String endDate) {
        return waterSituationMapper.selectList(reservoirName, startDate, endDate);
    }

    @Override
    public WaterSituation getById(Integer id) {
        return waterSituationMapper.selectById(id);
    }

    @Override
    public boolean create(WaterSituation waterSituation) {
        return waterSituationMapper.insert(waterSituation) > 0;
    }

    @Override
    public boolean update(WaterSituation waterSituation) {
        return waterSituationMapper.update(waterSituation) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return waterSituationMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteBatch(List<Integer> ids) {
        return waterSituationMapper.deleteBatch(ids) > 0;
    }

    @Override
    public ImportResult importExcel(String filePath) {
        try {
            List<WaterSituation> dataList = ExcelUtil.readWaterSituationFromExcel(filePath);
            if (dataList == null || dataList.isEmpty()) {
                return new ImportResult(false, "导入数据为空");
            }
            int imported = waterSituationMapper.insertBatch(dataList);
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
            List<WaterSituation> list;
            if (ids == null || ids.isEmpty()) {
                list = waterSituationMapper.selectList(null, null, null);
            } else {
                list = new ArrayList<>();
                for (Integer id : ids) {
                    WaterSituation ws = waterSituationMapper.selectById(id);
                    if (ws != null) {
                        list.add(ws);
                    }
                }
            }
            ExcelUtil.writeWaterSituationToExcel(list, filePath);
            return filePath;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> getDistinctReservoirs() {
        return waterSituationMapper.selectDistinctReservoirs();
    }
}
