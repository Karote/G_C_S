package com.coretronic.drone.main;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.LandscapeActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.WifiRssiReceiver;
import com.coretronic.drone.communication.SocketService;
import com.coretronic.dronecontrol.control.DroneController;
import com.coretronic.dronecontrol.service.DroneDevice;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends LandscapeActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button btnPiloting;
    private Button btnAlbum;
    private Button btnUpdate;
    private ImageView imgWifiSignal;
    private ImageView imgGpsSignal;
    private TextView tvTime;
    private WifiRssiReceiver wifiRssiReceiver;
    //    private boolean isSocketConnected = false;
    private Spinner spinnerDroneDevice;
    private List<DroneDevice> mDroneDevices;
    private DeviceAdapter mDeviceAdapter;

    private void assignViews() {
        btnPiloting = (Button) findViewById(R.id.btn_piloting);
        btnAlbum = (Button) findViewById(R.id.btn_album);
        btnUpdate = (Button) findViewById(R.id.btn_update);
        btnPiloting.setOnClickListener(this);
        btnAlbum.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);

        imgWifiSignal = (ImageView) findViewById(R.id.img_wifi_signal);
        wifiRssiReceiver = new WifiRssiReceiver(imgWifiSignal);
        imgGpsSignal = (ImageView) findViewById(R.id.img_gps_signal);
//        tvTime = (TextView) findViewById(R.id.tv_time);
        spinnerDroneDevice = (Spinner) findViewById(R.id.spinner_drone_device);

        mDroneDevices = new ArrayList<>();
        mDeviceAdapter = new DeviceAdapter();
        spinnerDroneDevice.setAdapter(mDeviceAdapter);
        spinnerDroneDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                         @Override
                                                         public void onItemSelected(AdapterView<?> adapterView, View view, final int i, long l) {
                                                             Log.d(TAG, "onItemSelected: " + mDroneDevices.get(i).getName());
//                selectControl(mDroneDevices.get(i), new OnDroneConnectedListener() {
//
//                    @Override
//                    public void onConnected() {
//                        Toast.makeText(MainActivity.this, "Init controller" + mDroneDevices.get(i).getName(),
//                                Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onConnectFail() {
//                        Toast.makeText(MainActivity.this, "Init controller error", Toast.LENGTH_LONG).show();
//                    }
//                });
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
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiRssiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
//        registerReceiver(socketStatusReceiver, new IntentFilter(SocketThread.BROADCAST_ACTION_SOCKET_STATUS));

//        Intent intent = new Intent(this, SocketService.class);
//        intent.putExtra(SocketService.ACTION_MODE, SocketService.ACTION_MODE_SOCKET_STATUS);
//        startService(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiRssiReceiver);
//        unregisterReceiver(socketStatusReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, SocketService.class));
    }

    @Override
    public void onDeviceAdded(final DroneDevice droneDevice) {
        if (!droneDevice.getName().equals("Select Device")) {
            mDroneDevices.add(droneDevice);
            mDeviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDeviceRemoved(final DroneDevice droneDevice) {
        mDroneDevices.remove(droneDevice);
        mDeviceAdapter.notifyDataSetChanged();
    }


    @Override
    public void OnBatteryUpdate(int battery) {

    }

    public DroneController getDroneController() {
        return this;
    }

    @Override
    public void onClick(View v) {
        Class cls = null;
        switch (v.getId()) {
            case R.id.btn_piloting:
//                if (isSocketConnected) {
                cls = PilotingActivity.class;
//                }

//                Timer timer = new Timer();
//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        startService(new Intent(MainActivity.this, SocketService.class));
//                    }
//                }, 0, 2000);

                break;
            case R.id.btn_album:
                cls = AlbumActivity.class;
                break;
            case R.id.btn_update:
                cls = UpdateActivity.class;
                break;
        }
        if (cls != null) startActivity(new Intent(MainActivity.this, cls));
    }

    private class DeviceAdapter extends BaseAdapter implements SpinnerAdapter {

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
