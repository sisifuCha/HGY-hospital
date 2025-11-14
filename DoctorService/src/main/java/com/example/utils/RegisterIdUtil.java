package com.example.utils;

public final class RegisterIdUtil {
    private static final String DELIMITER = "::";

    private RegisterIdUtil() {
    }

    public static String encode(String patientId, String scheduleId) {
        if (patientId == null || scheduleId == null) {
            throw new IllegalArgumentException("patientId and scheduleId must not be null");
        }
        return patientId + DELIMITER + scheduleId;
    }

    public static RegisterKey decode(String registerId) {
        if (registerId == null || registerId.isEmpty()) {
            throw new IllegalArgumentException("registerId must not be empty");
        }
        String[] parts = registerId.split(DELIMITER, -1);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            throw new IllegalArgumentException("registerId format is invalid");
        }
        return new RegisterKey(parts[0], parts[1]);
    }

    public static final class RegisterKey {
        private final String patientId;
        private final String scheduleId;

        public RegisterKey(String patientId, String scheduleId) {
            this.patientId = patientId;
            this.scheduleId = scheduleId;
        }

        public String getPatientId() {
            return patientId;
        }

        public String getScheduleId() {
            return scheduleId;
        }
    }
}
