package com.clarimire.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 水库名称标准化（Excel 简称 → 标准库名）
 */
public final class ReservoirNameUtil {

    private static final Map<String, String> NAME_MAP = new HashMap<>();

    static {
        String[][] pairs = {
            {"白河堡", "白河堡水库"}, {"半城子", "半城子水库"}, {"北台上", "北台上水库"},
            {"崇青", "崇青水库"}, {"大宁", "大宁水库"}, {"大水峪", "大水峪水库"},
            {"官厅", "官厅水库"}, {"海子", "海子水库"}, {"怀柔", "怀柔水库"},
            {"黄松峪", "黄松峪水库"}, {"密云", "密云水库"}, {"沙厂", "沙厂水库"},
            {"十三陵", "十三陵水库"}, {"桃峪口", "桃峪口水库"}, {"西峪", "西峪水库"},
            {"遥桥峪", "遥桥峪水库"}, {"斋堂", "斋堂水库"}, {"珠窝", "珠窝水库"}
        };
        for (String[] p : pairs) {
            NAME_MAP.put(p[0], p[1]);
        }
    }

    private ReservoirNameUtil() {}

    /**
     * @param name 原始库名
     * @return 标准库名
     */
    public static String normalize(String name) {
        if (name == null) {
            return null;
        }
        String n = name.replaceAll("\\s+", "").trim();
        if (n.isEmpty()) {
            return n;
        }
        if (n.endsWith("水库")) {
            return n;
        }
        return NAME_MAP.getOrDefault(n, n + "水库");
    }
}
