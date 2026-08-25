package com.clarimire.controller;

import com.clarimire.entity.ImportResult;
import com.clarimire.entity.WaterSituation;
import com.clarimire.service.WaterSituationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/waterSituation")
public class WaterSituationController {

    @Autowired
    private WaterSituationService waterSituationService;

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(required = false) String reservoirName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = new HashMap<>();
        List<WaterSituation> list = waterSituationService.getList(reservoirName, startDate, endDate);
        result.put("code", 200);
        result.put("data", list);
        result.put("total", list.size());
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        WaterSituation ws = waterSituationService.getById(id);
        if (ws != null) {
            result.put("code", 200);
            result.put("data", ws);
        } else {
            result.put("code", 404);
            result.put("message", "数据不存在");
        }
        return result;
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody WaterSituation waterSituation) {
        Map<String, Object> result = new HashMap<>();
        if (waterSituationService.create(waterSituation)) {
            result.put("code", 200);
            result.put("message", "创建成功");
        } else {
            result.put("code", 400);
            result.put("message", "创建失败");
        }
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody WaterSituation waterSituation) {
        Map<String, Object> result = new HashMap<>();
        if (waterSituationService.update(waterSituation)) {
            result.put("code", 200);
            result.put("message", "更新成功");
        } else {
            result.put("code", 400);
            result.put("message", "更新失败");
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        if (waterSituationService.delete(id)) {
            result.put("code", 200);
            result.put("message", "删除成功");
        } else {
            result.put("code", 400);
            result.put("message", "删除失败");
        }
        return result;
    }

    @DeleteMapping("/batch")
    public Map<String, Object> deleteBatch(@RequestBody(required = false) List<Integer> ids) {
        Map<String, Object> result = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            result.put("code", 400);
            result.put("message", "请选择要删除的数据");
            return result;
        }
        if (waterSituationService.deleteBatch(ids)) {
            result.put("code", 200);
            result.put("message", "批量删除成功");
        } else {
            result.put("code", 400);
            result.put("message", "批量删除失败");
        }
        return result;
    }

    @PostMapping("/import")
    public Map<String, Object> importExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("code", 400);
            result.put("message", "请选择文件");
            return result;
        }
        
        try {
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File destFile = new File(dir, fileName);
            file.transferTo(destFile);
            
            ImportResult importResult = waterSituationService.importExcel(destFile.getAbsolutePath());
            result.put("code", importResult.isSuccess() ? 200 : 400);
            result.put("message", importResult.getMessage());
            result.put("importedData", importResult.getImportedData());
            
            // 删除临时文件
            destFile.delete();
        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "文件上传失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/export")
    public void exportExcel(@RequestBody(required = false) List<Integer> ids, HttpServletResponse response) {
        try {
            String filePath = waterSituationService.exportExcel(ids);
            if (filePath != null) {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setCharacterEncoding("utf-8");
                String fileName = URLEncoder.encode("水情数据", "UTF-8").replaceAll("\\+", "%20");
                response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
                
                try (FileInputStream fis = new FileInputStream(new File(filePath));
                     OutputStream os = response.getOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        os.write(buffer, 0, len);
                    }
                    os.flush();
                }
                // 删除临时文件
                new File(filePath).delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/reservoirs")
    public Map<String, Object> getReservoirs() {
        Map<String, Object> result = new HashMap<>();
        List<String> list = waterSituationService.getDistinctReservoirs();
        result.put("code", 200);
        result.put("data", list);
        return result;
    }
}
