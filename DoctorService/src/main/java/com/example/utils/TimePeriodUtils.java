package com.example.utils;

import java.time.LocalTime;

public final class TimePeriodUtils {
    private TimePeriodUtils() {
    }

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
