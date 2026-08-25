package com.clarimire.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @PostMapping("/photo")
    public Map<String, Object> uploadPhoto(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (file == null || file.isEmpty()) {
                result.put("code", 400);
                result.put("message", "请选择文件");
                return result;
            }

            File dir = new File(uploadPath, "photos").getAbsoluteFile();
            if (!dir.exists() && !dir.mkdirs()) {
                result.put("code", 500);
                result.put("message", "无法创建上传目录: " + dir.getAbsolutePath());
                return result;
            }

            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
            File dest = new File(dir, fileName).getAbsoluteFile();
            Files.copy(file.getInputStream(), dest.toPath());

            result.put("code", 200);
            result.put("message", "上传成功");
            Map<String, String> data = new HashMap<>();
            data.put("url", "/uploads/photos/" + fileName);
            result.put("data", data);
            return result;
        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "上传失败: " + e.getMessage());
            return result;
        }
    }
}
