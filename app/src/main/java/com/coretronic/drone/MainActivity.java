package com.coretronic.drone;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.coretronic.drone.service.DroneDevice;
import com.coretronic.drone.ui.StatusView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends LandscapeFragmentActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Spinner spinnerDroneDevice;
    private List<DroneDevice> mDroneDevices;
    private DeviceAdapter mDeviceAdapter;
    private StatusChangedListener mStatusChangedListener;

    private int connectedDroneType = DroneDevice.DRONE_TYPE_FAKE;
    private StatusView statusView;

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
        mDroneDevices.add(new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, "null", 77));
        mDroneDevices.add(new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, "Add New Device", 88));

        mDeviceAdapter = new DeviceAdapter();
        spinnerDroneDevice.setAdapter(mDeviceAdapter);
        spinnerDroneDevice.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, final int i, long l) {
                        Log.d(TAG, "onItemSelected: " + mDroneDevices.get(i).getName());
                        final int droneType = mDroneDevices.get(i).getDroneType();
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
                                    connectedDroneType = droneType;
                                    Log.i(TAG, "Drone Type: " + droneType);
                                }

                                @Override
                                public void onConnectFail() {
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
    public void onLocationUpdate(long lat, long lon, int eph) {
        if (mStatusChangedListener != null) {
            mStatusChangedListener.onLocationUpdate(lat, lon, eph);
        }
    }

    @Override
    public void onHeadingUpdate(int heading){
        if(mStatusChangedListener != null){
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

    public Drone getDroneController() {
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

    public int getConnectedDroneType() {
        return connectedDroneType;
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
