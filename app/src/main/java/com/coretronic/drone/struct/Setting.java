package com.coretronic.drone.struct;

/**
 * Created by jiaLian on 15/5/25.
 */
public class Setting {
    private int minVale = 0;
    private int maxValue = 1;
    private int value;
    private String unit = null;

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

    public int getMinVale() {
        return minVale;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public String getUnit() {
        return unit;
    }
}
