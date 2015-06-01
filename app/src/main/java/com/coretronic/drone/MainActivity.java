package com.coretronic.drone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.communication.SocketService;
import com.coretronic.drone.missionplan.fragments.WaypointEditorFragment;
import com.coretronic.drone.piloting.PilotingFragment;
import com.coretronic.drone.service.DroneDevice;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends LandscapeFragmentActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int DRONE_TEST_TYPE = 456;
    private ImageView imgWifiSignal;
    private ImageView imgGpsSignal;
    private TextView tvTime;
    private WifiRssiReceiver wifiRssiReceiver;
    //    private boolean isSocketConnected = false;
    private Spinner spinnerDroneDevice;
    private List<DroneDevice> mDroneDevices;
    private DeviceAdapter mDeviceAdapter;

    private void assignViews() {
        Button btnPiloting = (Button) findViewById(R.id.btn_piloting);
        Button btnMissionPlan = (Button) findViewById(R.id.btn_mission_plan);
        Button btnAlbum = (Button) findViewById(R.id.btn_album);
        Button btnUpdate = (Button) findViewById(R.id.btn_update);
        btnPiloting.setOnClickListener(this);
        btnMissionPlan.setOnClickListener(this);
        btnAlbum.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);

        imgWifiSignal = (ImageView) findViewById(R.id.img_wifi_signal);
//        wifiRssiReceiver = new WifiRssiReceiver(imgWifiSignal);
        imgGpsSignal = (ImageView) findViewById(R.id.img_gps_signal);
//        tvTime = (TextView) findViewById(R.id.tv_time);
        spinnerDroneDevice = (Spinner) findViewById(R.id.spinner_drone_device);

        mDroneDevices = new ArrayList<>();
        mDroneDevices.add(new DroneDevice(DRONE_TEST_TYPE, "null", 77));
        mDroneDevices.add(new DroneDevice(DRONE_TEST_TYPE, "Add New Device", 88));

        mDeviceAdapter = new DeviceAdapter();
        spinnerDroneDevice.setAdapter(mDeviceAdapter);
        spinnerDroneDevice.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, final int i, long l) {
                        Log.d(TAG, "onItemSelected: " + mDroneDevices.get(i).getName());

                        if (mDroneDevices.get(i).getDroneType() == DRONE_TEST_TYPE) {
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
//        ((DroneG2Application) getApplication()).loadSettingsValue();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        registerReceiver(wifiRssiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
//        registerReceiver(socketStatusReceiver, new IntentFilter(SocketThread.BROADCAST_ACTION_SOCKET_STATUS));

//        Intent intent = new Intent(this, SocketService.class);
//        intent.putExtra(SocketService.ACTION_MODE, SocketService.ACTION_MODE_SOCKET_STATUS);
//        startService(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(wifiRssiReceiver);
//        unregisterReceiver(socketStatusReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, SocketService.class));
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

    public Drone getDroneController() {
        return this;
    }

    @Override
    public void onClick(View v) {
//        Class cls = null;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;
        switch (v.getId()) {
            case R.id.btn_piloting:
//                cls = PilotingActivity.class;
                fragment = new PilotingFragment();
                break;
            case R.id.btn_mission_plan:
                fragment = new WaypointEditorFragment();
                break;
            case R.id.btn_album:
//                cls = AlbumActivity.class;
                break;
            case R.id.btn_update:
//                cls = UpdateActivity.class;
                break;
        }
        if (fragment != null) {
            transaction.replace(R.id.frame_view, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
//        if (cls != null) startActivity(new Intent(MainActivity.this, cls));
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
//    private BroadcastReceiver socketStatusReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, "onReceive");
//            if (intent.getAction() == SocketThread.BROADCAST_ACTION_SOCKET_STATUS) {
//                if (intent.getStringExtra(SocketThread.STATUS).equals(SocketThread.SOCKET_STATUS_CONNECTED)) {
//                    isSocketConnected = true;
//                    Log.d(TAG, "onReceive isSocketConnected: " + isSocketConnected);
//                }
//            }
//        }
//    };
}
