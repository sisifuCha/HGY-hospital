package com.example.utils;

import java.time.LocalTime;

public final class TimePeriodUtils {
    private TimePeriodUtils() {
    }

    /**
     * 将时段编号映射为 schedule_template.id
     * @param periodIndex 时段编号 (1, 2, 3)
     * @return schedule_template.id (TIME0001, TIME0002, TIME0003)
     */
    public static String mapPeriodIndexToTemplateId(Integer periodIndex) {
        if (periodIndex == null) {
            return null;
        }
        switch (periodIndex) {
            case 1:
                return "TIME0001";
            case 2:
                return "TIME0002";
            case 3:
                return "TIME0003";
            default:
                return null;
        }
    }

    /**
     * 将 schedule_template.id 映射为时段编号
     * @param templateId schedule_template.id (TIME0001, TIME0002, TIME0003)
     * @return 时段编号 (1, 2, 3)
     */
    public static int resolveTemplateIdToPeriodIndex(String templateId) {
        if (templateId == null) {
            return 0;
        }
        switch (templateId.trim()) {
            case "TIME0001":
                return 1;
            case "TIME0002":
                return 2;
            case "TIME0003":
                return 3;
            default:
                return 0;
        }
    }

    // 保留旧方法以兼容现有代码（如果还在使用时段名称）
    public static int resolvePeriodIndex(String timePeriodName) {
        if (timePeriodName == null) {
            return 0;
        }
        String normalized = timePeriodName.trim().toLowerCase();
        switch (normalized) {
            case "上午":
            case "morning":
                return 1;
            case "下午":
            case "afternoon":
                return 2;
            case "晚上":
            case "evening":
                return 3;
            default:
                return 0;
        }
    }

    public static int resolvePeriodIndex(LocalTime startTime) {
        if (startTime == null) {
            return 0;
        }
        int hour = startTime.getHour();
        if (hour < 12) {
            return 1;
        }
        if (hour < 18) {
            return 2;
        }
        return 3;
    }
}
