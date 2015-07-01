package com.coretronic.drone;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.coretronic.drone.piloting.Setting;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

/**
 * Created by jiaLian on 15/4/1.
 */
public class DroneApplication extends Application {
    private static final String TAG = DroneApplication.class.getSimpleName();
    private static final char DEGREE_SYMBOL = 0x00B0;
    public static final String SETTINGS_VALUE = "settings_value";
    public static final String SETTING = "setting";

    public static Setting[] settings = new Setting[Setting.SettingType.LENGTH.ordinal()];

    @Override
    public void onCreate() {
        super.onCreate();
        settings[Setting.SettingType.INTERFACE_OPACTITY.ordinal()] = new Setting(20, 100, 70, "%");
        settings[Setting.SettingType.SD_RECORD.ordinal()] = new Setting(Setting.ON);
        settings[Setting.SettingType.FLIP_ENABLE.ordinal()] = new Setting(Setting.OFF);
        settings[Setting.SettingType.FLIP_ORIENTATION.ordinal()] = new Setting(Setting.FLIP_ORIENTATION_LEFT);

        settings[Setting.SettingType.JOYPAD_MODE.ordinal()] = new Setting(Setting.JOYPAD_MODE_USA);
        settings[Setting.SettingType.HEADLESS.ordinal()] = new Setting(Setting.OFF);
        settings[Setting.SettingType.LEFT_HANDED.ordinal()] = new Setting(Setting.OFF);
        settings[Setting.SettingType.PHONE_TILT.ordinal()] = new Setting(5, 50, 20, String.valueOf(DEGREE_SYMBOL));

        String tempStr = String.valueOf(DEGREE_SYMBOL) + "/s";
        settings[Setting.SettingType.ROTATION_SPEED_MAX.ordinal()] = new Setting(40, 350, 100, tempStr);
        settings[Setting.SettingType.ALTITUDE_LIMIT.ordinal()] = new Setting(3, 100, 3, "m");
        settings[Setting.SettingType.VERTICAL_SPEED_MAX.ordinal()] = new Setting(200, 2000, 700, "mm/s");
        settings[Setting.SettingType.TILT_ANGLE_MAX.ordinal()] = new Setting(5, 30, 12, String.valueOf(DEGREE_SYMBOL));
        loadSettingsValue();
    }

    public void saveSettingsValue() {
        SharedPreferences prefs = getSharedPreferences(SETTING, MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        for (Setting setting : settings) {
            jsonArray.put(setting.getValue());
        }
        Log.d(TAG, jsonArray.toString());
        prefs.edit().putString(SETTINGS_VALUE, jsonArray.toString()).commit();
    }

    public boolean loadSettingsValue() {
        SharedPreferences prefs = getSharedPreferences(SETTING, MODE_PRIVATE);
        String json = prefs.getString(SETTINGS_VALUE, null);
        try {
            if (json != null) {
                JSONArray jsonArray = new JSONArray(json);
                int i = 0;
                for (Setting setting : settings) {
                    setting.setValue(jsonArray.getInt(i++));
                }
            } else {
                Log.d(TAG, "Json Null");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

}
