package com.coretronic.drone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.coretronic.drone.activity.MiniDronesActivity;
import com.coretronic.drone.controller.DroneDevice;
import com.coretronic.drone.piloting.Setting;
import com.coretronic.drone.service.Parameter;
import com.coretronic.drone.ui.ViewManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class MainActivity extends MiniDronesActivity implements DroneController.ParameterLoaderListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final char DEGREE_SYMBOL = 0x00B0;   //º

    private static final String SETTING_NAME_2015 = "setting_2015";
    private static final String SETTING_NAME_G2 = "setting_g2";
    private static final String SETTINGS_VALUE = "settings_value";

    private static Setting[] settings = new Setting[Setting.SettingType.values().length];

    private DroneDevice connectedDroneDevice = new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, null, 0);
    private DroneDevice.OnDeviceChangedListener deviceChangedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        replaceFragment();
        initialSetting();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewManager.unbindDrawables(findViewById(android.R.id.content));
    }

    @Override
    public void onBackPressed() {
        // if there is a fragment and the back stack of this fragment is not empty,
        // then emulate 'onBackPressed' behaviour, because in default, it is not working
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragList = fm.getFragments();
        if (fragList != null && fragList.size() > 0) {
            for (Fragment frag : fragList) {
                if (frag == null) {
                    continue;
                }
                if (frag.isVisible()) {
                    FragmentManager childFm = frag.getChildFragmentManager();
                    if (childFm.getBackStackEntryCount() > 0) {
                        childFm.popBackStack();
                        return;
                    }
                }
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onDeviceAdded(final DroneDevice droneDevice) {
        if (deviceChangedListener != null) {
            deviceChangedListener.onDeviceAdded(droneDevice);
        }
    }

    @Override
    public void onDeviceRemoved(final DroneDevice droneDevice) {
        if (deviceChangedListener != null) {
            deviceChangedListener.onDeviceRemoved(droneDevice);
        }
        if (droneDevice.getName().equals(connectedDroneDevice.getName())) {
            Toast.makeText(this, connectedDroneDevice.getName() + " Disconnected", Toast.LENGTH_LONG).show();
            connectedDroneDevice = new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, null, 0);
            initialSetting();
        }
    }

    private void replaceFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_view, new MainFragment(), "fragment");
        transaction.commit();
    }

    public void registerDeviceChangedListener(DroneDevice.OnDeviceChangedListener deviceChangedListener) {
        this.deviceChangedListener = deviceChangedListener;
    }

    public void unregisterDeviceChangedListener(DroneDevice.OnDeviceChangedListener deviceChangedListener) {
        if (this.deviceChangedListener == deviceChangedListener) {
            this.deviceChangedListener = null;
        }
    }

    public void setConnectedDroneDevice(DroneDevice droneDevice) {
        connectedDroneDevice = droneDevice;
    }

    public DroneDevice getConnectedDroneDevice() {
        return connectedDroneDevice;
    }

    public void selectDrone(DroneDevice droneDevice, OnDroneConnectedListener onDroneConnectedListener) {
        selectControl(droneDevice, onDroneConnectedListener);
    }

    @Override
    public void onParameterLoaded(Parameter.Type type, Parameter parameter) {

        for (Setting setting : settings) {
            if(setting == null){
                continue;
            }
            if (type == setting.getParameterType()) {
                Log.d(TAG, "onParameterLoaded: " + type + "," + parameter.getValue());
                setting.setValue(parameter);
                Log.d(TAG, "onParameterLoaded setting: " + setting.getValue());
                break;
            }
        }
    }

    public boolean hasGPSSignal(int eph) {
        if (eph > 0) {
            return true;
        }
        return false;
    }

    public void readParameter() {
        getDroneController().readParameters(MainActivity.this, Parameter.Type.DRONE_SETTING, Parameter.Type.FLIP, Parameter.Type.FLIP_ORIENTATION, Parameter.Type.ROTATION_SPEED_MAX, Parameter.Type.ANGLE_MAX, Parameter.Type.VERTICAL_SPEED_MAX, Parameter.Type.ALTITUDE_LIMIT, Parameter.Type.ABSOLUTE_CONTROL);
    }

    public void initialSetting() {
        switch (connectedDroneDevice.getDroneType()) {
            case DroneDevice.DRONE_TYPE_FAKE:
                defaultSettings();
                break;
            case DroneDevice.DRONE_TYPE_CORETRONIC:
                settings[Setting.SettingType.VERTICAL_SPEED_MAX.ordinal()] = new Setting(Parameter.Type.VERTICAL_SPEED_MAX, 0, 500, 300, "cm/s");
            case DroneDevice.DRONE_TYPE_CORETRONIC_G2:
                loadSettingsValue();
                break;
            default:
                defaultSettings();
                break;
        }
    }

    private void defaultSettings() {
        settings[Setting.SettingType.INTERFACE_OPACITY.ordinal()] = new Setting(20, 100, 50, "%");
        settings[Setting.SettingType.SD_RECORD.ordinal()] = new Setting(Setting.ON);
        settings[Setting.SettingType.FLIP_ENABLE.ordinal()] = new Setting(Parameter.Type.FLIP, Setting.OFF);
        settings[Setting.SettingType.FLIP_ORIENTATION.ordinal()] = new Setting(Parameter.Type.FLIP_ORIENTATION, Setting.FLIP_ORIENTATION_RIGHT);

        settings[Setting.SettingType.JOYPAD_MODE.ordinal()] = new Setting(Setting.JOYPAD_MODE_USA);
        settings[Setting.SettingType.ABSOLUTE_CONTROL.ordinal()] = new Setting(Parameter.Type.ABSOLUTE_CONTROL, Setting.ON);
        settings[Setting.SettingType.LEFT_HANDED.ordinal()] = new Setting(Setting.OFF);
        settings[Setting.SettingType.PHONE_TILT.ordinal()] = new Setting(5, 50, 20, String.valueOf(DEGREE_SYMBOL));

        String tempStr = String.valueOf(DEGREE_SYMBOL) + "/s";

        // Basic Setting
        settings[Setting.SettingType.ALTITUDE_LIMIT.ordinal()] = new Setting(Parameter.Type.ALTITUDE_LIMIT, 10, 500, 500, "m");
        settings[Setting.SettingType.ROTATION_SPEED_MAX.ordinal()] = new Setting(Parameter.Type.ROTATION_SPEED_MAX, 40, 120, 120, tempStr);
        settings[Setting.SettingType.VERTICAL_SPEED_MAX.ordinal()] = new Setting(Parameter.Type.VERTICAL_SPEED_MAX, 2000, 6000, 6000, "mm/s");
        settings[Setting.SettingType.TILT_ANGLE_MAX.ordinal()] = new Setting(Parameter.Type.ANGLE_MAX, 10, 30, 30, String.valueOf(DEGREE_SYMBOL));
        settings[Setting.SettingType.LOW_BATTERY_PROTECTION_WARN_ENABLE.ordinal()] = new Setting(Parameter.Type.LOW_BATTERY_PROTECTION_WARN_ENABLE,Setting.OFF);
        settings[Setting.SettingType.LOW_BATTERY_PROTECTION_WARN_VALUE.ordinal()] = new Setting(Parameter.Type.LOW_BATTERY_PROTECTION_WARN_VALUE, 10, 40, 30, "%");
        settings[Setting.SettingType.LOW_BATTERY_PROTECTION_CRITICAL_ENABLE.ordinal()] = new Setting(Parameter.Type.LOW_BATTERY_PROTECTION_CRITICAL_ENABLE, Setting.OFF);
        settings[Setting.SettingType.LOW_BATTERY_PROTECTION_CRITICAL_VALUE.ordinal()] = new Setting(Parameter.Type.LOW_BATTERY_PROTECTION_CRITICAL_VALUE, 10, 40, 25, "%");

        // Gain & Expo Setting
        settings[Setting.SettingType.BASIC_AILERON_GAIN.ordinal()] = new Setting(Parameter.Type.BASIC_AILERON_GAIN, 50, 300, 100, "%");
        settings[Setting.SettingType.BASIC_ELEVATOR_GAIN.ordinal()] = new Setting(Parameter.Type.BASIC_ELEVATOR_GAIN, 50, 300, 100, "%");
        settings[Setting.SettingType.BASIC_RUDDER_GAIN.ordinal()] = new Setting(Parameter.Type.BASIC_RUDDER_GAIN, 50, 300, 100, "%");
        settings[Setting.SettingType.ATTITUDE_AILERON_GAIN.ordinal()] = new Setting(Parameter.Type.ATTITUDE_AILERON_GAIN, 50, 300, 100, "%");
        settings[Setting.SettingType.ATTITUDE_ELEVATOR_GAIN.ordinal()] = new Setting(Parameter.Type.ATTITUDE_ELEVATOR_GAIN, 50, 300, 100, "%");
        settings[Setting.SettingType.ATTITUDE_RUDDER_GAIN.ordinal()] = new Setting(Parameter.Type.ATTITUDE_RUDDER_GAIN, 50, 300, 100, "%");
        settings[Setting.SettingType.ATTITUDE_GAIN.ordinal()] = new Setting(Parameter.Type.ATTITUDE_GAIN, 50, 300, 100, "%");

    }

    public void resetSettings() {
        defaultSettings();
        if (connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC) {
            settings[Setting.SettingType.VERTICAL_SPEED_MAX.ordinal()] = new Setting(Parameter.Type.VERTICAL_SPEED_MAX, 0, 500, 300, "cm/s");
        }
    }

    public int getSettingValue(Setting.SettingType settingType) {
        return settings[settingType.ordinal()].getValue();
    }

    public void setSettingValue(Setting.SettingType settingType, int value) {
        settings[settingType.ordinal()].setValue(value);
    }

    public Setting getSetting(Setting.SettingType settingType) {
        return settings[settingType.ordinal()];
    }

    public Setting[] getSettings() {
        return settings;
    }

    public boolean saveSettingsValue() {
        if (connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_FAKE) {
            return false;
        }
        String name = connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC ? SETTING_NAME_2015 : SETTING_NAME_G2;
        SharedPreferences prefs = getSharedPreferences(name, MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        for (Setting setting : settings) {
            jsonArray.put(setting.getValue());
        }
        Log.d(TAG, jsonArray.toString());
        prefs.edit().putString(SETTINGS_VALUE, jsonArray.toString()).commit();
        return true;
    }

    public boolean loadSettingsValue() {
        String name = connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC ? SETTING_NAME_2015 : SETTING_NAME_G2;
        SharedPreferences prefs = getSharedPreferences(name, MODE_PRIVATE);
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
