package com.yqs112358;

public class Utils {
    public static long parseCapacity(String capacity) {
        capacity = capacity.trim().toUpperCase();

        int i = 0;
        while (i < capacity.length() && Character.isDigit(capacity.charAt(i))) {
            i++;
        }
        long value = Long.parseLong(capacity.substring(0, i));
        String unit = capacity.substring(i);

        return switch (unit) {
            case "B" -> value;
            case "K", "KB" -> value * 1024;
            case "M", "MB" -> value * 1024 * 1024;
            case "G", "GB" -> value * 1024 * 1024 * 1024;
            case "T", "TB" -> value * 1024L * 1024L * 1024L * 1024L;
            case "" -> value; // default as B
            default -> value; // bad unit
        };
    }
}
