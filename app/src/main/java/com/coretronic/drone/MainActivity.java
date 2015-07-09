package com.coretronic.drone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.album.AlbumFragment;
import com.coretronic.drone.missionplan.fragments.WaypointEditorFragment;
import com.coretronic.drone.piloting.PilotingFragment;
import com.coretronic.drone.piloting.Setting;
import com.coretronic.drone.service.DroneDevice;
import com.coretronic.drone.service.Parameter;
import com.coretronic.drone.ui.StatusView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends LandscapeFragmentActivity implements View.OnClickListener, DroneController.ParameterLoaderListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String G2_IP = "192.168.42.1";

    private static final char DEGREE_SYMBOL = 0x00B0;   //º

    public static final String SETTING_NAME_2015 = "setting_2015";
    public static final String SETTING_NAME_G2 = "setting_g2";
    public static final String SETTINGS_VALUE = "settings_value";

    public static Setting[] settings = new Setting[Setting.SettingType.LENGTH.ordinal()];

    private StatusView statusView;
    private Spinner spinnerDroneDevice;

    private List<DroneDevice> mDroneDevices;
    private DeviceAdapter mDeviceAdapter;
    private StatusChangedListener mStatusChangedListener;

    private DroneDevice connectedDroneDevice = new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, null, 0);

    private void assignViews() {
        Button btnPiloting = (Button) findViewById(R.id.btn_piloting);
        Button btnMissionPlan = (Button) findViewById(R.id.btn_mission_plan);
        LinearLayout llAlbum = (LinearLayout) findViewById(R.id.ll_album);
        LinearLayout llUpdate = (LinearLayout) findViewById(R.id.ll_updates);

        btnPiloting.setOnClickListener(this);
        btnMissionPlan.setOnClickListener(this);
        llAlbum.setOnClickListener(this);
        llUpdate.setOnClickListener(this);

        statusView = (StatusView) findViewById(R.id.status);

        spinnerDroneDevice = (Spinner) findViewById(R.id.spinner_drone_device);
        mDroneDevices = new ArrayList<>();
        mDroneDevices.add(new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, "Select Device", 77));
        mDroneDevices.add(new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, "Add New Device", 88));

        mDeviceAdapter = new DeviceAdapter();
        spinnerDroneDevice.setAdapter(mDeviceAdapter);
        spinnerDroneDevice.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, final int i, long l) {
                        Log.d(TAG, "onItemSelected: " + mDroneDevices.get(i).getName());
                        final DroneDevice droneDevice = mDroneDevices.get(i);
                        if (mDroneDevices.get(i).getDroneType() == DroneDevice.DRONE_TYPE_FAKE) {
                            if (mDroneDevices.get(i).getName().equals("Add New Device")) {
                                showAddNewDroneDialog();
                                spinnerDroneDevice.setSelection(0);
                            }
                        } else {
                            selectControl(mDroneDevices.get(i), new OnDroneConnectedListener() {

                                @Override
                                public void onConnected() {
                                    Toast.makeText(MainActivity.this, "Init controller" + mDroneDevices.get(i).getName(),
                                            Toast.LENGTH_LONG).show();
                                    connectedDroneDevice = droneDevice;
                                    initialSetting();
                                    readParameters(MainActivity.this, Parameter.Type.FLIP, Parameter.Type.FLIP_ORIENTATION, Parameter.Type.ROTATION_SPEED_MAX, Parameter.Type.ANGLE_MAX, Parameter.Type.VERTICAL_SPEED_MAX, Parameter.Type.ALTITUDE_LIMIT, Parameter.Type.ABSOLUTE_CONTROL);
                                }

                                @Override
                                public void onConnectFail() {
                                    spinnerDroneDevice.setSelection(0);
                                    Toast.makeText(MainActivity.this, "Init controller error", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                }
        );
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        assignViews();
        initialSetting();
    }

    @Override
    public void onDeviceAdded(final DroneDevice droneDevice) {
        mDroneDevices.add(droneDevice);
        mDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceRemoved(final DroneDevice droneDevice) {
        mDroneDevices.remove(droneDevice);
        mDeviceAdapter.notifyDataSetChanged();
        spinnerDroneDevice.setSelection(0);

        if (droneDevice.getName().equals(connectedDroneDevice.getName())) {
            Toast.makeText(this, connectedDroneDevice.getName() + " Disconnected", Toast.LENGTH_LONG).show();
            if (mStatusChangedListener != null) {
                mStatusChangedListener.onBatteryUpdate(0);
                mStatusChangedListener.onAltitudeUpdate(0);
                mStatusChangedListener.onLocationUpdate(0, 0, 0);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    statusView.setBatteryStatus(0);
                    statusView.setGpsVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onBatteryUpdate(final int battery) {
        if (mStatusChangedListener != null) {
            mStatusChangedListener.onBatteryUpdate(battery);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusView.setBatteryStatus(battery);
            }
        });
    }

    @Override
    public void onAltitudeUpdate(float altitude) {
        if (mStatusChangedListener != null) {
            mStatusChangedListener.onAltitudeUpdate(altitude);
        }
    }

    @Override
    public void onRadioSignalUpdate(int rssi) {
        if (mStatusChangedListener != null) {
            mStatusChangedListener.onRadioSignalUpdate(rssi);
        }
    }

    @Override
    public void onSpeedUpdate(float groundSpeed) {
        if (mStatusChangedListener != null) {
            mStatusChangedListener.onSpeedUpdate(groundSpeed);
        }
    }

    @Override
    public void onLocationUpdate(final long lat, final long lon, final int eph) {
        if (mStatusChangedListener != null) {
            mStatusChangedListener.onLocationUpdate(lat, lon, eph);
        }
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              if (connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC_G2) {
                                  statusView.setGpsVisibility(eph == 1 ? View.VISIBLE : View.GONE);
                              } else if (connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC) {
                                  statusView.setGpsVisibility((eph == 0 || eph == 9999) ? View.GONE : View.VISIBLE);
                              }
                          }
                      }
        );
    }

    @Override
    public void onHeadingUpdate(int heading) {
        if (mStatusChangedListener != null) {
            mStatusChangedListener.onHeadingUpdate(heading);
        }
    }

    public void registerDroneStatusChangedListener(StatusChangedListener statusChangedListener) {
        this.mStatusChangedListener = statusChangedListener;
    }

    public void unregisterDroneStatusChangedListener(StatusChangedListener statusChangedListener) {
        if (this.mStatusChangedListener == statusChangedListener) {
            this.mStatusChangedListener = null;
        }
    }

    public DroneController getDroneController() {
        return this;
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;
        String backStackName = null;
        switch (v.getId()) {
            case R.id.btn_piloting:
                fragment = new PilotingFragment();
                break;
            case R.id.btn_mission_plan:
                if (connectedDroneDevice.getDroneType() != DroneDevice.DRONE_TYPE_CORETRONIC) {
                    return;
                }
                fragment = new WaypointEditorFragment();
                break;
            case R.id.ll_album:
                fragment = new AlbumFragment();
                backStackName = "AlbumFragment";
                break;
            case R.id.ll_updates:
                break;
        }
        if (fragment != null) {
            transaction.replace(R.id.frame_view, fragment, "fragment");
            transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    public DroneDevice getConnectedDroneDevice() {
        return connectedDroneDevice;
    }

    @Override
    public void onParameterLoaded(Parameter.Type type, Parameter parameter) {
        for (Setting setting : settings) {
            if (type == setting.getParameterType()) {
                Log.d(TAG, "onParameterLoaded: " + type + "," + parameter.getValue());
                setting.setValue(parameter);
                Log.d(TAG, "onParameterLoaded setting: " + setting.getValue());
            }
        }
    }

    private void initialSetting() {
        switch (connectedDroneDevice.getDroneType()) {
            case DroneDevice.DRONE_TYPE_FAKE:
                defaultSettings();
                break;
            case DroneDevice.DRONE_TYPE_CORETRONIC:
                settings[Setting.SettingType.VERTICAL_SPEED_MAX.ordinal()] = new Setting(Parameter.Type.VERTICAL_SPEED_MAX, 0, 500, 300, "cm/s");
            case DroneDevice.DRONE_TYPE_CORETRONIC_G2:
                loadSettingsValue();
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

        settings[Setting.SettingType.ROTATION_SPEED_MAX.ordinal()] = new Setting(Parameter.Type.ROTATION_SPEED_MAX, 40, 120, 120, tempStr);
        settings[Setting.SettingType.ALTITUDE_LIMIT.ordinal()] = new Setting(Parameter.Type.ALTITUDE_LIMIT, 2, 100, 3, "m");
        settings[Setting.SettingType.VERTICAL_SPEED_MAX.ordinal()] = new Setting(Parameter.Type.VERTICAL_SPEED_MAX, 500, 6000, 2000, "mm/s");
        settings[Setting.SettingType.TILT_ANGLE_MAX.ordinal()] = new Setting(Parameter.Type.ANGLE_MAX, 10, 30, 30, String.valueOf(DEGREE_SYMBOL));
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

    public class DeviceAdapter extends BaseAdapter implements SpinnerAdapter {

        @Override
        public int getCount() {
            return mDroneDevices.size();
        }

        @Override
        public DroneDevice getItem(int position) {
            return mDroneDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.WRAP_CONTENT,
                    AbsListView.LayoutParams.MATCH_PARENT);
            TextView text = new TextView(MainActivity.this);
            text.setLayoutParams(params);
            text.setText(getItem(position).getName());
            return text;
        }
    }

    private void showAddNewDroneDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Add Device");
        alertDialog.setMessage("Enter Device ip");

        final EditText input = new EditText(this);
        input.setText(G2_IP);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("Add",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String deviceIp = input.getText().toString();
                        if (deviceIp.trim().length() <= 0) {
                            return;
                        }
                        addDevice(deviceIp);
                    }

                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }
}
