package com.coretronic.drone.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.Drone;
import com.coretronic.drone.LandscapeFragmentActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.WifiRssiReceiver;
import com.coretronic.drone.service.DroneDevice;
import com.coretronic.drone.settings.SettingViewPagerFragment;
import com.coretronic.drone.ui.JoyStickSurfaceView;
import com.coretronic.drone.ui.SemiCircleProgressBarView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiaLian on 15/4/1.
 */
public class PilotingActivity extends LandscapeFragmentActivity {
    private static final String TAG = PilotingActivity.class.getSimpleName();
    public static final int DRONE_TEST_TYPE = 456;
    public static JoyStickSurfaceView[] joyStickSurfaceViews = new JoyStickSurfaceView[2];
    public static View markView;
    private WifiRssiReceiver wifiRssiReceiver;
    private TextView tvPitch;
    private TextView tvRoll;
    private SensorManager sensorManager;
    private float[] magneticValues = new float[3];
    private float[] accelerometerValues = new float[3];
    private int pitch;
    private int roll;
    private int startPitch;
    private int startRoll;
    private boolean isOnOrientationSensorMode = false;
    private ControlWrap mControlWrap;
    private int radius = 0;
    private Spinner spinnerDroneDevice;
    private List<DroneDevice> mDroneDevices;
    private DeviceAdapter mDeviceAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piloting);
        assignViews();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiRssiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiRssiReceiver);
        sensorManager.unregisterListener(sensorEventListener);
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

    private void assignViews() {
        ImageButton btnBack = (ImageButton) findViewById(R.id.btn_back);
        ImageButton btnSettings = (ImageButton) findViewById(R.id.btn_settings);
        markView = findViewById(R.id.mark_view);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_view, new SettingViewPagerFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        initialJoystickModule(R.id.module1);
        initialJoystickModule(R.id.module2);

        mControlWrap = new ControlWrap();
        initialJoypadMode();
        for (int i = 0; i < joyStickSurfaceViews.length; i++) {
            if (radius < joyStickSurfaceViews[i].getRadius()) {
                radius = joyStickSurfaceViews[i].getRadius();
            }
        }
        SemiCircleProgressBarView semiCircleProgressBarView = (SemiCircleProgressBarView) findViewById(R.id.semi_circle_bar);
        semiCircleProgressBarView.setProgressBarColor(Color.RED);
        semiCircleProgressBarView.setProgress(55);
        wifiRssiReceiver = new WifiRssiReceiver(btnBack);

        tvPitch = (TextView) findViewById(R.id.tv_pitch);
        tvRoll = (TextView) findViewById(R.id.tv_roll);

        int size = (int) (getResources().getDimension(R.dimen.joystick_size) / getResources().getDisplayMetrics().density) / 2;
        final String[] stickList = new String[(size / 5) - 3];
        for (int i = 0; i < stickList.length; i++) {
            stickList[i] = String.valueOf(size);
            size -= 5;
        }
        Spinner spinnerStickSize = (Spinner) findViewById(R.id.spinner_stick_size);
        ArrayAdapter<String> stickSizeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, stickList);
        spinnerStickSize.setAdapter(stickSizeAdapter);
        spinnerStickSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                for (JoyStickSurfaceView joyStickSurfaceView : joyStickSurfaceViews) {
                    if (((DroneG2Application) getApplication()).isUITesting)
                        joyStickSurfaceView.changeStickSize(Integer.valueOf(stickList[i]));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
                                    Toast.makeText(PilotingActivity.this, "Init controller" + mDroneDevices.get(i).getName(),
                                            Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onConnectFail() {
                                    Toast.makeText(PilotingActivity.this, "Init controller error", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                }

        );
        Button btnEmergency = (Button) findViewById(R.id.emergency);
        Button btnAction = (Button) findViewById(R.id.take_off);
        btnEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmergency();
                Log.d(TAG, "Emergency");
            }
        });
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                if (btn.getText().equals("Take Off")) {
                    sendTakeoff();
                    btn.setText("Landing");
                    Log.d(TAG, "Take Off");
                } else {
                    sendLanding();
                    btn.setText("Take Off");
                    Log.d(TAG, "Landing");
                }
            }
        });
    }

    private void initialJoystickModule(int moduleViewId) {
        final int moduleIndex = moduleViewId == R.id.module1 ? 0 : 1;
        View moduleView = findViewById(moduleViewId);
        final TextView tvX = (TextView) moduleView.findViewById(R.id.tv_dx);
        final TextView tvY = (TextView) moduleView.findViewById(R.id.tv_dy);
        joyStickSurfaceViews[moduleIndex] = (JoyStickSurfaceView) moduleView.findViewById(R.id.joystick);
        joyStickSurfaceViews[moduleIndex].setOnStickListener(
                new JoyStickSurfaceView.OnStickListener() {
                    @Override
                    public void onStickEvent(View view, int action, int dx, int dy) {
                        if (((JoyStickSurfaceView) view).getControlType() == JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL) {
                            tvX.setText("Roll: " + mControlWrap.changeRoll(dx));
                            tvY.setText("Pitch: " + mControlWrap.changePitch(-dy));

                            Log.d(TAG, "onStickEvent Pitch: " + (-dy));
                            Log.d(TAG, "onStickEvent Roll: " + dx);
                        } else {
                            tvX.setText("Yaw: " + mControlWrap.changeYaw(dx));
                            tvY.setText("Throttle: " + mControlWrap.changeThrottle(dy));
                            Log.d(TAG, "onStickEvent Throttle: " + dy);
                            Log.d(TAG, "onStickEvent Yaw: " + dx);
                        }
                    }

                    @Override
                    public void onDoubleClick(View view) {
                        Log.d(TAG, "onDoubleClick");
                    }

                    @Override
                    public void onOrientationSensorMode(int action) {
                        Log.d(TAG, "onOrientationSensorMode");
                        if (action == MotionEvent.ACTION_DOWN) {
                            startPitch = pitch;
                            startRoll = roll;
                            isOnOrientationSensorMode = true;
                        } else if (action == MotionEvent.ACTION_UP) {
                            if (getController() != null) {
                                mControlWrap.pitch = 0;
                                mControlWrap.roll = 0;
                                tvPitch.setText("pitch: " + mControlWrap.pitch);
                                tvRoll.setText("roll: " + mControlWrap.roll);
                                getController().control(mControlWrap.roll, mControlWrap.pitch, mControlWrap.throttle, mControlWrap.yaw);
                                Log.d(TAG, "sendControl Throttle: " + mControlWrap.throttle);
                                Log.d(TAG, "sendControl Yaw: " + mControlWrap.yaw);
                                Log.d(TAG, "sendControl Pitch: " + mControlWrap.pitch);
                                Log.d(TAG, "sendControl Roll: " + mControlWrap.roll);
                            }
                            isOnOrientationSensorMode = false;
                        }
                    }
                }

        );
        final ViewGroup.LayoutParams originalParams = joyStickSurfaceViews[moduleIndex].getLayoutParams();
        final int size = (int) getResources().getDimension(R.dimen.joystick_size);
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        final FrameLayout frameLayout = (FrameLayout) moduleView.findViewById(R.id.frame_layout);
        frameLayout.setOnTouchListener(new View.OnTouchListener() {
                                           @Override
                                           public boolean onTouch(View v, MotionEvent event) {
                                               if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                                   params.leftMargin = (int) event.getX() - (params.width >> 1);
                                                   params.topMargin = (int) event.getY() - (params.height >> 1);
                                                   joyStickSurfaceViews[moduleIndex].setLayoutParams(params);
                                               }
                                               if (event.getAction() == MotionEvent.ACTION_UP) {
                                                   joyStickSurfaceViews[moduleIndex].setLayoutParams(originalParams);
                                               }
                                               event.setLocation(event.getX() - params.leftMargin, event.getY() - params.topMargin);
                                               joyStickSurfaceViews[moduleIndex].onTouchEvent(event);
                                               return true;
                                           }
                                       }
        );
    }

    public static void initialJoypadMode() {
        int[] controlType;
        boolean[] isJoyModes;
        int[] bgDrawableIds;
        int[] stickDrawableIds;
        if (DroneG2Application.isSettings[DroneG2Application.SettingType.JOYPAD_MODE.ordinal()]) {
            if (DroneG2Application.isSettings[DroneG2Application.SettingType.LEFT_HANDED.ordinal()]) {
                controlType = new int[]{JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL, JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_YAW};
            } else {
                controlType = new int[]{JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_YAW, JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL};
            }
            isJoyModes = new boolean[]{true, true};
            bgDrawableIds = new int[]{R.drawable.image_button_bg, R.drawable.image_button_bg};
            stickDrawableIds = new int[]{R.drawable.redpoint, R.drawable.redpoint};
        } else {
            if (DroneG2Application.isSettings[DroneG2Application.SettingType.LEFT_HANDED.ordinal()]) {
                controlType = new int[]{JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL, JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_YAW};
                isJoyModes = new boolean[]{false, true};
                bgDrawableIds = new int[]{0, R.drawable.image_button_bg};
                stickDrawableIds = new int[]{R.drawable.redpoint, R.drawable.redpoint};
            } else {
                controlType = new int[]{JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_YAW, JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL};
                isJoyModes = new boolean[]{true, false};
                bgDrawableIds = new int[]{R.drawable.image_button_bg, 0};
                stickDrawableIds = new int[]{R.drawable.redpoint, R.drawable.redpoint};
            }
        }

        for (int i = 0; i < joyStickSurfaceViews.length; i++) {
            joyStickSurfaceViews[i].initJoyMode(controlType[i], isJoyModes[i], bgDrawableIds[i], stickDrawableIds[i]);
        }
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            int type = event.sensor.getType();
            switch (type) {
                case Sensor.TYPE_MAGNETIC_FIELD:
                    // 取得加速度感應器資訊
                    magneticValues = event.values.clone();
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    // 取得磁場感應器資訊
                    accelerometerValues = event.values.clone();
                    break;
            }

            // 準備轉換資料用的陣列變數
            float[] ra = new float[9];
            float[] ia = new float[9];

            // 儲存轉換後資料
            float[] values = new float[3];

            // 儲存轉換為角度的資料
            int[] result = new int[3];

            // 先取得裝置旋轉資訊
            SensorManager.getRotationMatrix(ra, ia, accelerometerValues, magneticValues);

            // 轉換為方向與傾斜度資訊
            SensorManager.getOrientation(ra, values);

            // 轉換為角度整數
            for (int i = 0; i < result.length; i++) {
                result[i] = (int) Math.toDegrees(values[i]);
            }
            final int rotation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_0:
                    pitch = -result[1];
                    roll = result[2];
//                    Log.d(TAG, "portrait");
                    break;
                case Surface.ROTATION_90:
                    pitch = -result[2];
                    roll = -result[1];
//                    Log.d(TAG, "landscape");
                    break;
//                case Surface.ROTATION_180:
//                    Log.d(TAG, "reverse portrait");
//                    break;
//                default:
//                    Log.d(TAG, "reverse landscape");
//                    break;
            }
            if (isOnOrientationSensorMode) {
                int rcPitch = pitch - startPitch;
                int rcRoll = roll - startRoll;
                if (getController() != null) {
                    if (rcPitch > 30) {
                        rcPitch = 30;
                    } else if (rcPitch < -30) {
                        rcPitch = -30;
                    }
                    if (rcRoll > 30) {
                        rcRoll = 30;
                    } else if (rcRoll < -30) {
                        rcRoll = -30;
                    }
                    mControlWrap.pitch = rcPitch * 2;
                    mControlWrap.roll = rcRoll * 2;
                    tvPitch.setText("pitch: " + mControlWrap.pitch);
                    tvRoll.setText("roll: " + mControlWrap.roll);
                    getController().control(mControlWrap.roll, mControlWrap.pitch, mControlWrap.throttle, mControlWrap.yaw);
                    Log.d(TAG, "sendControl Throttle: " + mControlWrap.throttle);
                    Log.d(TAG, "sendControl Yaw: " + mControlWrap.yaw);
                    Log.d(TAG, "sendControl Pitch: " + mControlWrap.pitch);
                    Log.d(TAG, "sendControl Roll: " + mControlWrap.roll);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void sendControl() {
        if (getController() != null) {
            getController().control(mControlWrap.roll, mControlWrap.pitch, mControlWrap.throttle, mControlWrap.yaw);
            Log.d(TAG, "sendControl Throttle: " + mControlWrap.throttle);
            Log.d(TAG, "sendControl Yaw: " + mControlWrap.yaw);
            Log.d(TAG, "sendControl Pitch: " + mControlWrap.pitch);
            Log.d(TAG, "sendControl Roll: " + mControlWrap.roll);
        }
    }

    private void sendLanding() {
        if (getController() != null) {
            getController().land();
        }
    }

    private void sendTakeoff() {
        if (getController() != null) {
            getController().takeOff(10);
        }
    }

    private void sendEmergency() {
        if (getController() != null) {
            getController().emergency();
        }
    }

    private Drone getController() {
        return this;
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

    private class ControlWrap {
        private float pitch = 0;
        private float roll = 0;
        private float throttle = 0;
        private float yaw = 0;
        public final static int DEFAULT_VALUE = 0;
        private final static int DEFAULT_RADIUS = 100;
        private int radius = 0;

        public int changePitch(float pitch) {
            this.pitch = changeValue(pitch);
            sendControl();
            return (int) this.pitch;
        }

        public int changeRoll(float roll) {
            this.roll = changeValue(roll);
            sendControl();
            return (int) this.roll;
        }

        public int changeThrottle(float throttle) {
            this.throttle = changeValue(throttle);
            sendControl();
            return (int) this.throttle;
        }

               public int changeYaw(float yaw) {
            this.yaw = changeValue(yaw);
            sendControl();
            return (int) this.yaw;
        }

        private float changeValue(float value) {
            if (radius == 0) {
                radius = ((DroneG2Application) PilotingActivity.this.getApplicationContext()).joyStickRadius;
            }
            if (value == DEFAULT_VALUE) {
                return DEFAULT_VALUE;
            } else {
//            Log.d(TAG, "radius: " + radius);
                return (value / radius) * DEFAULT_RADIUS;
            }
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
            TextView text = new TextView(PilotingActivity.this);
            text.setLayoutParams(params);
            text.setText(getItem(position).getName());

            return text;
        }
    }
}