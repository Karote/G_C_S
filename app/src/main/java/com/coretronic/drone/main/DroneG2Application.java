package com.coretronic.drone.main;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.coretronic.drone.struct.Setting;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by jiaLian on 15/4/1.
 */
public class DroneG2Application extends Application {
    private static final String TAG = DroneG2Application.class.getSimpleName();
    public static final int ON = 1;
    public static final int OFF = 0;
    public static final String SETTINGS_VALUE = "settings_value";
    public static final String SETTING = "setting";

    public enum SettingType {JOYPAD_MODE, HEADLESS, LEFT_HANDED, PHONE_TILT, LENGTH}

    public static Setting[] settings = new Setting[SettingType.LENGTH.ordinal()];
    public static int joyStickRadius = 0;
    public static final boolean isUITesting = false;

    @Override
    public void onCreate() {
        super.onCreate();
        settings[SettingType.JOYPAD_MODE.ordinal()] = new Setting(ON);
        settings[SettingType.HEADLESS.ordinal()] = new Setting(OFF);
        settings[SettingType.LEFT_HANDED.ordinal()] = new Setting(OFF);
        settings[SettingType.PHONE_TILT.ordinal()] = new Setting(0, 50, 20, "º");
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
                for (int i = 0; i < settings.length; i++) {
                    settings[i].setValue(jsonArray.getInt(i));
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
