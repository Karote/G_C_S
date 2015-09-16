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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.DroneController.DroneStatus;
import com.coretronic.drone.activity.MiniDronesActivity;
import com.coretronic.drone.annotation.Callback.Event;
import com.coretronic.drone.controller.DroneDevice;
import com.coretronic.drone.missionplan.fragments.MapViewFragment;
import com.coretronic.drone.piloting.settings.SettingViewPagerFragment;
import com.coretronic.drone.ui.StatusView;
import com.coretronic.drone.utility.AppConfig;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends UnBindDrawablesFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener, DroneDevice.OnDeviceChangedListener, DroneController.StatusChangedListener {

    private static final String ADD_NEW_DEVICE_TITLE = "Add New Device";

    private static final String TAG = MainFragment.class.getSimpleName();
    private static final String G2_IP = "192.168.42.1";

    private StatusView mStatusView;
    private Spinner mDroneDeviceSpinner;

    private List<DroneDevice> mDroneDevices;
    private DeviceAdapter mDeviceAdapter;
    private MainActivity mMainActivity;
    private SharedPreferences mSharedPreferences;

    private final int TTS_RESULT_CODE = 0x1;
    private Button btnLogout;

    private void assignViews(View view) {
        Button btnMissionPlan = (Button) view.findViewById(R.id.btn_mission_plan);
        btnLogout = (Button) view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(this);
        btnMissionPlan.setOnClickListener(this);

        // 20150814 add flight history and flight setting button
        Button btnFlightHistory = (Button) view.findViewById(R.id.btn_flight_history);
        ImageButton btnFlightSetting = (ImageButton) view.findViewById(R.id.btn_flight_setting);
        btnFlightHistory.setOnClickListener(this);
        btnFlightSetting.setOnClickListener(this);

        // version setting
        TextView tvAppVersion = (TextView) view.findViewById(R.id.tv_app_version);
        tvAppVersion.setText("v " + BuildConfig.VERSION_NAME);

        mStatusView = (StatusView) view.findViewById(R.id.status);

        mDroneDeviceSpinner = (Spinner) view.findViewById(R.id.spinner_drone_device);
        mDroneDevices = new ArrayList<>();
        mDroneDevices.add(new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, "Select Device", 77));
        mDroneDevices.add(new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, ADD_NEW_DEVICE_TITLE, 88));

        mDeviceAdapter = new DeviceAdapter(getActivity(), R.layout.spinner_item, mDroneDevices);
        mDroneDeviceSpinner.setAdapter(mDeviceAdapter);
        mDroneDeviceSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkTTS();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        assignViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivity = (MainActivity) getActivity();
        mSharedPreferences = mMainActivity.getSharedPreferences(AppConfig.SHAREDPREFERENCE_ID, Context.MODE_PRIVATE);
        String userId = mSharedPreferences.getString(LoginFragment.ARG_USER_ID, "");
        Log.d("morris", "db name:" + userId);
        // login the couchbase
        if (userId.length() == 0) {
            Toast.makeText(getActivity(), "Drone Cloud login error.", Toast.LENGTH_SHORT).show();
            getFragmentManager().beginTransaction().replace(R.id.frame_view, new LoginFragment(), LoginFragment.class.getSimpleName()).commit();
            return;
        } else {
            mMainActivity.login(userId.replace("@", "_").replace(".", "_").toLowerCase());
            btnLogout.setText(userId);
        }
        mMainActivity.registerDeviceChangedListener(this);
        mMainActivity.registerDroneStatusChangedListener(this);

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
        if (mDroneDevices.contains(droneDevice)) {
            return;
        }
        mDroneDevices.add(droneDevice);
        mDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceRemoved(final DroneDevice droneDevice) {
        mDroneDevices.remove(droneDevice);
        mDeviceAdapter.notifyDataSetChanged();
        if (droneDevice.equals(mMainActivity.getConnectedDroneDevice())) {
            mDroneDeviceSpinner.setSelection(0);
            mStatusView.onDisconnect();
        }
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
        final DroneDevice droneDevice = mDroneDevices.get(position);
        Log.d(TAG, "onItemSelected: " + droneDevice.getName());

        if (droneDevice == mMainActivity.getConnectedDroneDevice()) {
            return;
        }

        if (droneDevice.getDroneType() == DroneDevice.DRONE_TYPE_FAKE) {
            if (ADD_NEW_DEVICE_TITLE.equals(droneDevice.getName())) {
                showAddNewDroneDialog();
                mDroneDeviceSpinner.setSelection(0);
            }
        }
        mMainActivity.selectDrone(droneDevice, new MiniDronesActivity.OnDroneConnectedListener() {
            @Override
            public void onConnected() {
                Toast.makeText(getActivity().getApplicationContext(), "Init controller" + droneDevice.getName(),
                        Toast.LENGTH_LONG).show();
                mMainActivity.setConnectedDroneDevice(droneDevice);
                mMainActivity.initialSetting();
                mMainActivity.readParameter();
            }

            @Override
            public void onConnectFail() {
                mDroneDeviceSpinner.setSelection(0);
                Toast.makeText(getActivity().getApplicationContext(), "Init controller error", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        Fragment fragment = null;
        switch (v.getId()) {
            case R.id.btn_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                Dialog dialog = builder.setTitle("Log Out")
                        .setMessage("Do you want to log out?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSharedPreferences.edit()
                                        .putString(LoginFragment.ARG_USER_ID, "")
                                        .putString(LoginFragment.ARG_USER_PW, "")
                                        .putBoolean(LoginFragment.ARG_STAY_LOGGED, false)
                                        .apply();
                                getFragmentManager().beginTransaction().replace(R.id.frame_view, new LoginFragment(), LoginFragment.class.getSimpleName()).commit();
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
                fragment = new SettingViewPagerFragment();
                break;
        }
        if (fragment != null) {
            getFragmentManager().beginTransaction().replace(R.id.frame_view, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName()).commit();
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

        private Activity context;
        List<DroneDevice> deer;

        public DeviceAdapter(Activity context, int resource, List<DroneDevice> deer) {

            super(context, resource, deer);
            this.context = context;
            this.deer = deer;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.spinner_style_main, parent, false);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.spinner_title_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textView.setText(getItem(position).getName());
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
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
