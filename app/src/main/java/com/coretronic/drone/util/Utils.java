package com.coretronic.drone.util;

/**
 * Created by Poming on 2015/11/23.
 */
public class Utils {

    private final static int MIN_DURATION_IN_SECONDS = 0;
    private final static int MAX_HOUR_SHOW = 99;
    private final static int SECONDS_ONE_HOUR = 60 * 60;
    private final static int SECONDS_ONE_MINUTE = 60;
    private final static int MAX_DURATION_IN_SECONDS = MAX_HOUR_SHOW * SECONDS_ONE_HOUR - 1;

    public static String getDurationInHMSFormat(double durationInSeconds) {

        int normalizedDuration = (int) Math.max(Math.min(durationInSeconds, MAX_DURATION_IN_SECONDS), MIN_DURATION_IN_SECONDS);

        int hours = normalizedDuration / SECONDS_ONE_HOUR;
        int minutes = normalizedDuration % SECONDS_ONE_HOUR / SECONDS_ONE_MINUTE;
        int seconds = normalizedDuration % SECONDS_ONE_MINUTE;

        if (hours == 0) {
            return String.format("%02d:%02d", minutes, seconds);
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static int calculateLevel(int max, int min, int value, int level) {
        if (value < min) {
            return 0;
        } else if (value >= max) {
            return level - 1;
        } else {
            int inputRange = (max - min);
            int outputRange = (level - 1);
            return ((value - min) * outputRange / inputRange);
        }
    }

}
