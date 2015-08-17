package com.coretronic.drone.piloting;

import com.coretronic.drone.service.Parameter;
import com.coretronic.drone.service.Parameter.Orientation;

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
        INTERFACE_OPACITY, SD_RECORD, FLIP_ENABLE, FLIP_ORIENTATION,
        ALTITUDE_LIMIT, VERTICAL_SPEED_MAX, ROTATION_SPEED_MAX, TILT_ANGLE_MAX,
        JOYPAD_MODE, ABSOLUTE_CONTROL, LEFT_HANDED, PHONE_TILT, LENGTH,
        LOW_BATTERY_PROTECTION, BASIC_AILERON_GAIN, BASIC_ELEVATOR_GAIN, BASIC_RUDDER_GAIN,
        ATTITUDE_AILERON_GAIN, ATTITUDE_ELEVATOR_GAIN, ATTITUDE_RUDDER_GAIN, ATTITUDE_GAIN
    }

    public Setting(int value) {
        this(null, value);
    }

    public Setting(Parameter.Type parameterType, int value) {
        this.value = value;
        this.parameterType = parameterType;
    }

    public Setting(int minVale, int maxValue, int value) {
        this(null, minVale, maxValue, value, null);
    }

    public Setting(int minVale, int maxValue, int value, String unit) {
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

    public void setValue(Parameter parameter) {
        switch (parameterType) {
            case FLIP:
            case ABSOLUTE_CONTROL:
                value = parameter == Parameter.Control.ENABLE ? Setting.ON : Setting.OFF;
                break;
            case FLIP_ORIENTATION:
                if (parameter.getValue() == Orientation.BACK) {
                    value = Setting.FLIP_ORIENTATION_BACK;
                } else if (parameter.getValue() == Orientation.FRONT) {
                    value = Setting.FLIP_ORIENTATION_FRONT;
                } else if (parameter.getValue() == Orientation.LEFT) {
                    value = Setting.FLIP_ORIENTATION_LEFT;
                } else if (parameter.getValue() == Orientation.RIGHT) {
                    value = Setting.FLIP_ORIENTATION_RIGHT;
                }
                break;
            case ROTATION_SPEED_MAX:
            case VERTICAL_SPEED_MAX:
            case ANGLE_MAX:
            case BASIC_AILERON_GAIN:
            case BASIC_ELEVATOR_GAIN:
            case BASIC_RUDDER_GAIN:
            case ATTITUDE_AILERON_GAIN:
            case ATTITUDE_ELEVATOR_GAIN:
            case ATTITUDE_RUDDER_GAIN:
            case ATTITUDE_GAIN:
                value = (short) parameter.getValue();
                break;
            case ALTITUDE_LIMIT:
                value = (int) ((short) parameter.getValue() / 100f);
                break;
            case LOW_BATTERY_PROTECTION:

                break;
        }

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
                        parameter = Orientation.BACK;
                        break;
                    case Setting.FLIP_ORIENTATION_FRONT:
                        parameter = Orientation.FRONT;
                        break;
                    case Setting.FLIP_ORIENTATION_LEFT:
                        parameter = Orientation.LEFT;
                        break;
                    case Setting.FLIP_ORIENTATION_RIGHT:
                        parameter = Orientation.RIGHT;
                        break;
                }
                break;
            case ROTATION_SPEED_MAX:
            case VERTICAL_SPEED_MAX:
            case ANGLE_MAX:
            case BASIC_AILERON_GAIN:
            case BASIC_ELEVATOR_GAIN:
            case BASIC_RUDDER_GAIN:
            case ATTITUDE_AILERON_GAIN:
            case ATTITUDE_ELEVATOR_GAIN:
            case ATTITUDE_RUDDER_GAIN:
            case ATTITUDE_GAIN:
                parameter = Parameter.Number.getInstance().setValue((short) value);
                break;
            case ALTITUDE_LIMIT:
                parameter = Parameter.Number.getInstance().setValue((short) (value * 100));
                break;

            case LOW_BATTERY_PROTECTION:
                break;

        }
        return parameter;
    }
}
