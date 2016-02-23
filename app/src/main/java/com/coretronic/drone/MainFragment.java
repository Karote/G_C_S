package com.coretronic.drone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.DroneStatus.StatusChangedListener;
import com.coretronic.drone.activity.MiniDronesActivity;
import com.coretronic.drone.annotation.Callback.Event;
import com.coretronic.drone.missionplan.fragments.MapViewFragment;
import com.coretronic.drone.settings.SettingsMaingFragment;
import com.coretronic.drone.ui.StatusView;
import com.coretronic.drone.util.AppConfig;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends UnBindDrawablesFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener, DroneDevice.OnDeviceChangedListener, StatusChangedListener {

    private static final String ADD_NEW_DEVICE_TITLE = "Add New Device";
    private static final String TAG = MainFragment.class.getSimpleName();
    private static final String G2_IP = "192.168.42.1";

    private StatusView mStatusView;
    private Spinner mDroneDeviceSpinner;

    private DeviceAdapter mDeviceAdapter;
    private MainActivity mMainActivity;
    private SharedPreferences mSharedPreferences;

    private final int TTS_RESULT_CODE = 0x1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_mission_plan).setOnClickListener(this);
        view.findViewById(R.id.btn_flight_history).setOnClickListener(this);
        view.findViewById(R.id.btn_flight_setting).setOnClickListener(this);

        ((TextView) view.findViewById(R.id.tv_app_version)).setText("v " + BuildConfig.VERSION_NAME);
        Button logoutButton = (Button) view.findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(this);
        logoutButton.setText(mSharedPreferences.getString(AppConfig.SHARED_PREFERENCE_USER_MAIL_KEY, ""));

        mStatusView = (StatusView) view.findViewById(R.id.status);

        mDeviceAdapter = new DeviceAdapter(mMainActivity, R.layout.main_fragment_select_device_spinner_item, new ArrayList<DroneDevice>());
        mDeviceAdapter.add(DroneDevice.FAKE_DRONE_DEVICE);
        mDeviceAdapter.add(new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, ADD_NEW_DEVICE_TITLE, 88));

        mDroneDeviceSpinner = (Spinner) view.findViewById(R.id.spinner_drone_device);
        mDroneDeviceSpinner.setAdapter(mDeviceAdapter);
        mDroneDeviceSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMainActivity = (MainActivity) activity;
        mSharedPreferences = mMainActivity.getPreferences(Context.MODE_PRIVATE);
    }

    private boolean tryToLogin() {
        String userId = mSharedPreferences.getString(AppConfig.SHARED_PREFERENCE_USER_MAIL_KEY, "");
        if (userId.length() == 0) {
            return false;
        }
        mMainActivity.login(userId.replace("@", "_").replace(".", "_").toLowerCase());
        return true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivity.registerDeviceChangedListener(this);
        mMainActivity.registerDroneStatusChangedListener(this);
        if (!tryToLogin()) {
            Toast.makeText(mMainActivity, "Drone Cloud login error.", Toast.LENGTH_SHORT).show();
            mMainActivity.switchToLoginFragment();
        }
        checkTTS();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMainActivity.unregisterDeviceChangedListener(this);
        mMainActivity.unregisterDroneStatusChangedListener(this);
        mStatusView.onDisconnect();
    }

    @Override
    public void onDeviceAdded(final DroneDevice droneDevice) {
        if (mDeviceAdapter.contains(droneDevice)) {
            return;
        }
        mDeviceAdapter.add(droneDevice);
    }

    @Override
    public void onDeviceRemoved(final DroneDevice droneDevice) {
        mDeviceAdapter.remove(droneDevice);
    }

    @Override
    public void onConnectingDeviceRemoved(DroneDevice droneDevice) {
        mDroneDeviceSpinner.setSelection(0);
        mStatusView.onDisconnect();
    }

    @Override
    public void onStatusUpdate(Event event, DroneStatus droneStatus) {
        switch (event) {
            case ON_BATTERY_UPDATE:
                mStatusView.setBatteryStatus(droneStatus.getBattery());
                break;
            case ON_SATELLITE_UPDATE:
                mStatusView.setGpsStatus(droneStatus.getSatellites());
                break;
            case ON_RADIO_SIGNAL_UPDATE:
                mStatusView.setRFStatus(droneStatus.getRadioSignal());
                break;
            case ON_HEARTBEAT:
                mStatusView.updateCommunicateLight();
                break;

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
        final DroneDevice droneDevice = mDeviceAdapter.getItem(position);
        Log.d(TAG, "onItemSelected: " + droneDevice.getName());

        if (droneDevice == mMainActivity.getSelectedDevice()) {
            return;
        }
        mStatusView.onDisconnect();
        if (droneDevice.getDroneType() == DroneDevice.DRONE_TYPE_FAKE) {
            if (ADD_NEW_DEVICE_TITLE.equals(droneDevice.getName())) {
                showAddNewDroneDialog();
                mDroneDeviceSpinner.setSelection(0);
                return;
            }
        }
        mMainActivity.selectDevice(droneDevice, new MiniDronesActivity.OnDroneConnectedListener() {
            @Override
            public void onConnected() {
                Toast.makeText(getActivity(), "Init controller" + droneDevice.getName(), Toast.LENGTH_LONG).show();
                mMainActivity.initialSetting(droneDevice);
                mMainActivity.readParameter();
            }

            @Override
            public void onConnectFail() {
                mDroneDeviceSpinner.setSelection(0);
                Toast.makeText(getActivity(), "Init controller error", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void clearUserInfo() {

        mSharedPreferences.edit()
                .putString(AppConfig.SHARED_PREFERENCE_USER_MAIL_KEY, "")
                .putString(AppConfig.SHARED_PREFERENCE_USER_PASSWORD_KEY, "")
                .putBoolean(AppConfig.SHARED_PREFERENCE_USER_STAY_LOGIN_KEY, false)
                .apply();
    }

    @Override
    public void onClick(View v) {
        Fragment fragment = null;
        switch (v.getId()) {
            case R.id.btn_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
                Dialog dialog = builder.setTitle("Log Out")
                        .setMessage("Do you want to log out?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearUserInfo();
                                mMainActivity.switchToLoginFragment();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
                return;
            case R.id.btn_mission_plan:
                fragment = MapViewFragment.newInstance(MapViewFragment.FRAGMENT_TYPE_PLANNING);
                break;
            case R.id.btn_flight_history:
                fragment = MapViewFragment.newInstance(MapViewFragment.FRAGMENT_TYPE_HISTORY);
                break;
            case R.id.btn_flight_setting:
//                fragment = new SettingFragment();
                fragment = new SettingsMaingFragment();
                break;
        }
        if (fragment != null) {
            getFragmentManager().beginTransaction().replace(R.id.frame_view, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName()).commitAllowingStateLoss();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
    }

    private void checkTTS() {
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, TTS_RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_RESULT_CODE && resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installIntent);
        }
    }

    public class DeviceAdapter extends ArrayAdapter<DroneDevice> {

        private List<DroneDevice> droneDevices;

        public DeviceAdapter(Activity context, int resource, List<DroneDevice> droneDevices) {
            super(context, resource, droneDevices);
            this.droneDevices = droneDevices;
        }

        public boolean contains(DroneDevice droneDevice) {
            return droneDevices.contains(droneDevice);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mMainActivity).inflate(R.layout.main_fragment_select_device_spinner, parent, false);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.spinner_item_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textView.setText(getItem(position).getName());
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mMainActivity).inflate(R.layout.main_fragment_select_device_spinner_item, parent, false);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.spinner_item_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textView.setText(getItem(position).getName());
            return convertView;
        }

        private class ViewHolder {
            public TextView textView;
        }
    }

    private void showAddNewDroneDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Add Device");
        alertDialog.setMessage("Enter Device ip");

        final EditText input = new EditText(getActivity());
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
                        mMainActivity.addDevice(deviceIp);
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
