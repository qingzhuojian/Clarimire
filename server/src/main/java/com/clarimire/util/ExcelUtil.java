package com.clarimire.util;

import com.clarimire.entity.SectionMonitor;
import com.clarimire.entity.WaterSituation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelUtil {

    private static final Pattern WATER_DATE_PATTERN = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日");

    public static List<WaterSituation> readWaterSituationFromExcel(String filePath) throws Exception {
        List<WaterSituation> list = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new Exception("文件不存在");
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String reservoirName = ReservoirNameUtil.normalize(getCellValue(row.getCell(0)));
                if (reservoirName == null || reservoirName.isEmpty()) {
                    continue;
                }

                WaterSituation ws = new WaterSituation();
                ws.setReservoirName(reservoirName);
                ws.setDate(parseWaterDate(getCellValue(row.getCell(1))));
                ws.setWaterLevel(toBigDecimal(getCellValue(row.getCell(2))));
                ws.setStorage(toBigDecimal(getCellValue(row.getCell(3))));
                ws.setAvgInflow(toBigDecimal(getCellValue(row.getCell(4))));
                ws.setAvgOutflow(toBigDecimal(getCellValue(row.getCell(5))));
                ws.setYoyIncrease(toBigDecimal(getCellValue(row.getCell(6))));
                ws.setTotalCapacity(toBigDecimal(getCellValue(row.getCell(7))));
                ws.setFloodLevel(toBigDecimal(getCellValue(row.getCell(8))));
                if (ws.getDate() != null) {
                    list.add(ws);
                }
            }
        }
        return list;
    }

    public static void writeWaterSituationToExcel(List<WaterSituation> list, String filePath) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("水情数据");
            String[] headers = {"库名", "日期", "库水位(米)", "蓄水量(万立方米)", "日平均入库流量(立方米/秒)",
                    "日平均出库流量(立方米/秒)", "比去年同期增减(万立方米)", "总库容(万立方米)", "汛限水位(米)"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            for (int i = 0; i < list.size(); i++) {
                WaterSituation ws = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(ws.getReservoirName());
                row.createCell(1).setCellValue(ws.getDate() != null ? dateFormat.format(ws.getDate()) : "");
                setDecimal(row, 2, ws.getWaterLevel());
                setDecimal(row, 3, ws.getStorage());
                setDecimal(row, 4, ws.getAvgInflow());
                setDecimal(row, 5, ws.getAvgOutflow());
                setDecimal(row, 6, ws.getYoyIncrease());
                setDecimal(row, 7, ws.getTotalCapacity());
                setDecimal(row, 8, ws.getFloodLevel());
            }

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }

    public static List<SectionMonitor> readSectionMonitorFromExcel(String filePath) throws Exception {
        List<SectionMonitor> list = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new Exception("文件不存在");
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String monitorPointName = getCellValue(row.getCell(0));
                if (monitorPointName == null || monitorPointName.isEmpty()) {
                    continue;
                }

                SectionMonitor sm = new SectionMonitor();
                sm.setMonitorPointName(monitorPointName.trim());
                sm.setReservoirName(ReservoirNameUtil.normalize(getCellValue(row.getCell(1))));
                sm.setYear(toInteger(getCellValue(row.getCell(2))));
                sm.setMonth(toInteger(getCellValue(row.getCell(3))));
                sm.setAmmoniaNitrogen(toBigDecimal(getCellValue(row.getCell(4))));
                sm.setPotassiumPermanganate(toBigDecimal(getCellValue(row.getCell(5))));
                sm.setCod(toBigDecimal(getCellValue(row.getCell(6))));
                sm.setFlow(toBigDecimal(getCellValue(row.getCell(7))));
                sm.setWaterDepth(toBigDecimal(getCellValue(row.getCell(8))));
                sm.setTotalNitrogen(toBigDecimal(getCellValue(row.getCell(9))));
                sm.setTotalPhosphorus(toBigDecimal(getCellValue(row.getCell(10))));
                list.add(sm);
            }
        }
        return list;
    }

    public static void writeSectionMonitorToExcel(List<SectionMonitor> list, String filePath) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("监测断面数据");
            String[] headers = {"监测点名称", "水库名称", "年份", "月份", "氨氮(mg/L)", "高锰酸盐指数(mg/L)",
                    "化学需氧量(mg/L)", "流量(m³/s)", "水深(m)", "总氮(mg/L)", "总磷(mg/L)"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            for (int i = 0; i < list.size(); i++) {
                SectionMonitor sm = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(sm.getMonitorPointName());
                row.createCell(1).setCellValue(sm.getReservoirName());
                row.createCell(2).setCellValue(sm.getYear() != null ? sm.getYear() : 0);
                row.createCell(3).setCellValue(sm.getMonth() != null ? sm.getMonth() : 0);
                setDecimal(row, 4, sm.getAmmoniaNitrogen());
                setDecimal(row, 5, sm.getPotassiumPermanganate());
                setDecimal(row, 6, sm.getCod());
                setDecimal(row, 7, sm.getFlow());
                setDecimal(row, 8, sm.getWaterDepth());
                setDecimal(row, 9, sm.getTotalNitrogen());
                setDecimal(row, 10, sm.getTotalPhosphorus());
            }

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }

    private static Date parseWaterDate(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        Matcher m = WATER_DATE_PATTERN.matcher(text.trim());
        if (m.find()) {
            @SuppressWarnings("deprecation")
            Date d = new Date(Integer.parseInt(m.group(1)) - 1900,
                    Integer.parseInt(m.group(2)) - 1,
                    Integer.parseInt(m.group(3)),
                    8, 0, 0);
            return d;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(text.trim().substring(0, Math.min(19, text.length())));
        } catch (Exception e) {
            return null;
        }
    }

    private static void setDecimal(Row row, int idx, BigDecimal val) {
        if (val != null) {
            row.createCell(idx).setCellValue(val.doubleValue());
        } else {
            row.createCell(idx).setCellValue("");
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cell.getDateCellValue());
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num) && !Double.isInfinite(num)) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private static BigDecimal toBigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer toInteger(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return (int) Double.parseDouble(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
