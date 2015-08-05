package com.coretronic.drone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
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

import com.coretronic.drone.activity.MiniDronesActivity;
import com.coretronic.drone.album.AlbumFragment;
import com.coretronic.drone.controller.DroneDevice;
import com.coretronic.drone.missionplan.fragments.WaypointEditorFragment;
import com.coretronic.drone.ui.StatusView;

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

    private void assignViews(View view) {
        Button btnPiloting = (Button) view.findViewById(R.id.btn_piloting);
        Button btnMissionPlan = (Button) view.findViewById(R.id.btn_mission_plan);
        LinearLayout llAlbum = (LinearLayout) view.findViewById(R.id.ll_album);
        LinearLayout llUpdate = (LinearLayout) view.findViewById(R.id.ll_updates);

        btnPiloting.setOnClickListener(this);
        btnMissionPlan.setOnClickListener(this);
        llAlbum.setOnClickListener(this);
        llUpdate.setOnClickListener(this);

        statusView = (StatusView) view.findViewById(R.id.status);

        spinnerDroneDevice = (Spinner) view.findViewById(R.id.spinner_drone_device);
        mDroneDevices = new ArrayList<>();
        mDroneDevices.add(new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, "Select Device", 77));
        mDroneDevices.add(new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, "Add New Device", 88));

        mDeviceAdapter = new DeviceAdapter();
        spinnerDroneDevice.setAdapter(mDeviceAdapter);
        spinnerDroneDevice.setOnItemSelectedListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
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
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = null;
        String backStackName = null;
        switch (v.getId()) {
            case R.id.btn_piloting:
                // 20150805 morris : disable pilotion function
                //fragment = new PilotingFragment();
                break;
            case R.id.btn_mission_plan:
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
            transaction.add(R.id.frame_view, fragment, "fragment");
            transaction.addToBackStack(backStackName);
            transaction.commit();
        }
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
            TextView text = new TextView(getActivity());
            text.setLayoutParams(params);
            text.setText(getItem(position).getName());
            return text;
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
