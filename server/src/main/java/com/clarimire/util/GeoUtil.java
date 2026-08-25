package com.clarimire.util;

/**
 * 地理距离与打卡区域判定
 */
public final class GeoUtil {

    private GeoUtil() {}

    public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double r = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }

    /**
     * @return core / buffer / remote
     */
    public static String resolveZone(double distanceM, int coreRadiusM, int bufferRadiusM) {
        if (distanceM <= coreRadiusM) {
            return "core";
        }
        if (distanceM <= bufferRadiusM) {
            return "buffer";
        }
        return "remote";
    }
}
