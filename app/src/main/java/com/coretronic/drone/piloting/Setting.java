package com.coretronic.drone.piloting;

import com.coretronic.drone.service.Parameter;

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

    public static final int JOYPAD_MODE_JAPAN = 0;
    public static final int JOYPAD_MODE_USA = 1;
    public static final int JOYPAD_MODE_KINESICS = 2;

    private Parameter.Type parameterType = null;
    private int minVale;
    private int maxValue;
    private int value;
    private String unit = null;

    public enum SettingType {
        INTERFACE_OPACTITY, SD_RECORD, FLIP_ENABLE, FLIP_ORIENTATION,
        ALTITUDE_LIMIT, VERTICAL_SPEED_MAX, ROTATION_SPEED_MAX, TILT_ANGLE_MAX,
        JOYPAD_MODE, ABSOLUTE_CONTROL, LEFT_HANDED, PHONE_TILT, LENGTH
    }

    public Setting(int value) {
//        this.value = value;
        this(null, value);
    }

    public Setting(Parameter.Type parameterType, int value) {
        this.value = value;
        this.parameterType = parameterType;
    }

    public Setting(int minVale, int maxValue, int value) {
//        this.minVale = minVale;
//        this.maxValue = maxValue;
//        this.value = value;
        this(null, minVale, maxValue, value, null);
    }

    public Setting(int minVale, int maxValue, int value, String unit) {
//        this.minVale = minVale;
//        this.maxValue = maxValue;
//        this.value = value;
//        this.unit = unit;
        this(null, minVale, maxValue, value, unit);
    }

    public Setting(Parameter.Type parameterType, int minVale, int maxValue, int value, String unit) {
        this.parameterType = parameterType;
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

    public Parameter.Type getParameterType() {
        return parameterType;
    }

    public Parameter getParameter() {
        Parameter parameter = null;
        switch (parameterType) {
            case FLIP:
            case ABSOLUTE_CONTROL:
                parameter = value == Setting.ON ? Parameter.Control.ENABLE : Parameter.Control.DISABLE;
                break;
            case FLIP_ORIENTATION:
                switch (value) {
                    case Setting.FLIP_ORIENTATION_BACK:
                        parameter = Parameter.Orientation.BACK;
                        break;
                    case Setting.FLIP_ORIENTATION_FRONT:
                        parameter = Parameter.Orientation.FRONT;
                        break;
                    case Setting.FLIP_ORIENTATION_LEFT:
                        parameter = Parameter.Orientation.LEFT;
                        break;
                    case Setting.FLIP_ORIENTATION_RIGHT:
                        parameter = Parameter.Orientation.RIGHT;
                        break;
                }
                break;
            case ROTATION_SPEED_MAX:
            case VERTICAL_SPEED_MAX:
            case ANGLE_MAX:
                parameter = Parameter.Number.getInstance().setValue((short) value);
                break;
            case ALTITUDE_LIMIT:
                parameter = Parameter.Number.getInstance().setValue((short) (value * 100));
                break;
        }
        return parameter;
    }
}
