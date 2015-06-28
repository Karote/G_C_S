package com.coretronic.drone.piloting;

/**
 * Created by jiaLian on 15/5/25.
 */
public class Setting {
    private static final String TAG = Setting.class.getSimpleName();
    public static final int ON = 1;
    public static final int OFF = 0;

    public static final int FLIP_ORIENTATION_FRONT = 0;
    public static final int FLIP_ORIENTATION_BACK = 1;
    public static final int FLIP_ORIENTATION_LEFT = 2;
    public static final int FLIP_ORIENTATION_RIGHT = 3;

    private int minVale ;
    private int maxValue;
    private int value;
    private String unit = null;

    public enum SettingType {
        INTERFACE_OPACTITY, SD_RECORD, FLIP_ENABLE, FLIP_ORIENTATION,
        ALTITUDE_LIMIT, VERTICAL_SPEED_MAX, ROTATION_SPEED_MAX, TILT_ANGLE_MAX,
        JOYPAD_MODE, HEADLESS, LEFT_HANDED, PHONE_TILT, LENGTH
    }

    public Setting(int value) {
        this.value = value;
    }

    public Setting(int minVale, int maxValue, int value) {
        this.minVale = minVale;
        this.maxValue = maxValue;
        this.value = value;
    }

    public Setting(int minVale, int maxValue, int value, String unit) {
        this.minVale = minVale;
        this.maxValue = maxValue;
        this.value = value;
        this.unit = unit;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getMinValue() {
        return minVale;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public String getUnit() {
        return unit;
    }
}
