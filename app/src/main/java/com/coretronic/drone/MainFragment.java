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
import android.support.v4.app.FragmentTransaction;
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

import com.coretronic.drone.activity.MiniDronesActivity;
import com.coretronic.drone.controller.DroneDevice;
import com.coretronic.drone.missionplan.fragments.WaypointEditorFragment;
import com.coretronic.drone.piloting.settings.SettingViewPagerFragment;
import com.coretronic.drone.ui.StatusView;
import com.coretronic.drone.utility.AppConfig;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends UnBindDrawablesFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener, DroneDevice.OnDeviceChangedListener, DroneController.StatusChangedListener {
    private static final String TAG = MainFragment.class.getSimpleName();
    private static final String G2_IP = "192.168.42.1";

    private StatusView statusView;
    private Spinner spinnerDroneDevice;

    private List<DroneDevice> mDroneDevices;
    private DeviceAdapter mDeviceAdapter;
    private MainActivity activity;
    private SharedPreferences sharedPreferences;

    // TTS
    private final int CHECK_CODE = 0x1;
    private String mUserId;
    private String mUserPw;
    private boolean isStayLogged;

    private void assignViews(View view) {
        Button btnMissionPlan = (Button) view.findViewById(R.id.btn_mission_plan);
        Button btnLogout = (Button) view.findViewById(R.id.btn_logout);
        btnLogout.setText(mUserId);

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

        statusView = (StatusView) view.findViewById(R.id.status);

        spinnerDroneDevice = (Spinner) view.findViewById(R.id.spinner_drone_device);
        mDroneDevices = new ArrayList<>();
        mDroneDevices.add(new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, "Select Device", 77));
        mDroneDevices.add(new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, "Add New Device", 88));

        mDeviceAdapter = new DeviceAdapter(getActivity(), R.layout.spinner_item, mDroneDevices);
        spinnerDroneDevice.setAdapter(mDeviceAdapter);
        spinnerDroneDevice.setOnItemSelectedListener(this);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences(AppConfig.SHAREDPREFERENCE_ID, Context.MODE_PRIVATE);
        activity = (MainActivity) getActivity();
        mUserId = sharedPreferences.getString(LoginFragment.ARG_USER_ID, "");
        String dbName = mUserId.replace("@", "_").replace(".", "_").toLowerCase();
        Log.d("morris", "db name:" + dbName);
        // login the couchbase
        if (mUserId.length() == 0) {
            Toast.makeText(getActivity(), "Couchbase login error.", Toast.LENGTH_SHORT).show();
        } else {
            activity.login(dbName);
        }
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
        activity.registerDeviceChangedListener(this);
        activity.registerDroneStatusChangedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.unregisterDeviceChangedListener(this);
        activity.unregisterDroneStatusChangedListener(this);
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

        if (droneDevice.getName().equals(activity.getConnectedDroneDevice().getName())) {
            activity.runOnUiThread(new Runnable() {
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
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusView.setBatteryStatus(battery);
            }
        });
    }

    @Override
    public void onAltitudeUpdate(float altitude) {

    }

    @Override
    public void onRadioSignalUpdate(int rssi) {
    }

    @Override
    public void onSpeedUpdate(float groundSpeed) {

    }

    @Override
    public void onLocationUpdate(final long lat, final long lon, final int eph) {
        activity.runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       statusView.setGpsVisibility(activity.hasGPSSignal(eph) ? View.VISIBLE : View.GONE);
                                   }
                               }
        );
    }

    @Override
    public void onHeadingUpdate(int heading) {
    }

    @Override
    public void onDroneStateUpdate(DroneController.DroneMode droneMode, DroneController.MissionStatus missionStatus, int duration) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
        final DroneDevice droneDevice = mDroneDevices.get(position);
        Log.d(TAG, "onItemSelected: " + droneDevice.getName());
        if (droneDevice.getDroneType() == DroneDevice.DRONE_TYPE_FAKE) {
            if (droneDevice.getName().equals("Add New Device")) {
                showAddNewDroneDialog();
                spinnerDroneDevice.setSelection(0);
            }
        } else {
            activity.selectDrone(droneDevice, new MiniDronesActivity.OnDroneConnectedListener() {
                @Override
                public void onConnected() {
                    Toast.makeText(getActivity().getApplicationContext(), "Init controller" + droneDevice.getName(),
                            Toast.LENGTH_LONG).show();
                    activity.setConnectedDroneDevice(droneDevice);
                    activity.initialSetting();
                    activity.readParameter();
                }

                @Override
                public void onConnectFail() {
                    spinnerDroneDevice.setSelection(0);
                    Toast.makeText(getActivity().getApplicationContext(), "Init controller error", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = null;
        String backStackName = null;
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.btn_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                Dialog dialog = builder.setTitle("Log Out")
                        .setMessage("Do you want to log out?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sharedPreferences.edit()
                                        .putString(LoginFragment.ARG_USER_ID, "")
                                        .putString(LoginFragment.ARG_USER_PW, "")
                                        .putBoolean(LoginFragment.ARG_STAY_LOGGED, false)
                                        .apply();
                                transaction.replace(R.id.frame_view, new LoginFragment(), LoginFragment.class.getSimpleName()).commit();
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

                break;
            case R.id.btn_mission_plan:
                fragment = new WaypointEditorFragment();
                bundle.putInt(AppConfig.MAIN_FRAG_ARGUMENT, 0);
                backStackName = "WaypointEditorFragment";
                fragment.setArguments(bundle);
                break;
            case R.id.btn_flight_history:
                fragment = new WaypointEditorFragment();
                bundle.putInt(AppConfig.MAIN_FRAG_ARGUMENT, 1);
                backStackName = "WaypointEditorFragment";
                fragment.setArguments(bundle);
                break;
            case R.id.btn_flight_setting:
                fragment = new SettingViewPagerFragment();
                break;
        }
        if (fragment != null) {
            transaction.add(R.id.frame_view, fragment, backStackName);
            transaction.addToBackStack(MainFragment.class.getSimpleName());
            transaction.hide(MainFragment.this);
            transaction.commit();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
    }

    private void checkTTS() {
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHECK_CODE) {
            if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
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
            ViewHolder holder = null;
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
                        activity.addDevice(deviceIp);
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
