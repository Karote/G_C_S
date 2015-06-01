package com.coretronic.drone.piloting;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.coretronic.drone.Drone;
import com.coretronic.drone.DroneG2Application;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.WifiRssiReceiver;
import com.coretronic.drone.piloting.settings.SettingViewPagerFragment;
import com.coretronic.drone.service.DroneDevice;
import com.coretronic.drone.ui.JoyStickSurfaceView;
import com.coretronic.drone.ui.SemiCircleProgressBarView;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

/**
 * Created by jiaLian on 15/4/1.
 */
public class PilotingFragment extends UnBindDrawablesFragment implements Drone.StatusChangedListener {
    private static final String TAG = PilotingFragment.class.getSimpleName();
    //    public static final int DRONE_TEST_TYPE = 456;
    private static final float M_S2KM_H = 3.6f;
    public static final int MAX_SPEED_KMH = 100;
//    private static final String VIDEO_FILE_PATH = "rtsp://mm2.pcslab.com/mm/7m1000.mp4";
    private static final String VIDEO_FILE_PATH = "rtsp://192.168.1.171:8086";

    private WifiRssiReceiver wifiRssiReceiver;

    public static JoyStickSurfaceView[] joyStickSurfaceViews = new JoyStickSurfaceView[2];
    public static View markView;
    private TextView tvPitch;
    private TextView tvRoll;
    private Spinner spinnerDroneDevice;
    private List<DroneDevice> mDroneDevices;
    private DeviceAdapter mDeviceAdapter;
    private SemiCircleProgressBarView semiCircleProgressBarView;
    private TextView tvBattery;
    private TextView tvAltitude;

    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;

    private FragmentManager childFragmentManager;
    private SensorManager sensorManager;
    private FragmentActivity fragmentActivity;

    private float[] magneticValues = new float[3];
    private float[] accelerometerValues = new float[3];
    private int pitch;
    private int roll;
    private int startPitch;
    private int startRoll;
    private boolean isOnOrientationSensorMode = false;
    private ControlWrap mControlWrap;
    private int radius = 0;
    private FrameLayout rtspFrameLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fragment_piloting);
        fragmentActivity = getActivity();
        childFragmentManager = getChildFragmentManager();
        sensorManager = (SensorManager) fragmentActivity.getSystemService(Context.SENSOR_SERVICE);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.d(TAG, "onPrepared");
                mediaPlayer.start();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_piloting, container, false);
        assignViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) fragmentActivity).registerStatusChanged(this);
//        if (surfaceView == null) {
//            surfaceView = new SurfaceView(getActivity());
//            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            rtspFrameLayout.addView(surfaceView, layoutParams);
//        }

    }

    @Override
    public void onStart() {
        super.onStart();
//        setSurfaceViewLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onResume() {
        super.onResume();
//        fragmentActivity.registerReceiver(wifiRssiReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
//        if(mediaPlayer!=null){
//            mediaPlayer.start();
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        fragmentActivity.unregisterReceiver(wifiRssiReceiver);
        sensorManager.unregisterListener(sensorEventListener);
    }
//    @Override
//    public void onDeviceAdded(final DroneDevice droneDevice) {
//        mDroneDevices.add(droneDevice);
//        mDeviceAdapter.notifyDataSetChanged();
//    }
//
//    @Override
//    public void onDeviceRemoved(final DroneDevice droneDevice) {
//        mDroneDevices.remove(droneDevice);
//        mDeviceAdapter.notifyDataSetChanged();
//    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    public void onBatteryUpdate(final int battery) {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvBattery.setText(battery + "%");
            }
        });
    }

    @Override
    public void onAltitudeUpdate(final float altitude) {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvAltitude.setText(String.format("%.1fm", altitude));
            }
        });
    }

    @Override
    public void onRadioSignalUpdate(int rssi) {

    }

    @Override
    public void onSpeedUpdate(final float groundSpeed) {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                semiCircleProgressBarView.setProgress((int) (groundSpeed * M_S2KM_H));
            }
        });
    }

    private void assignViews(View view) {
        ImageButton btnBack = (ImageButton) view.findViewById(R.id.btn_back);
        ImageButton btnSettings = (ImageButton) view.findViewById(R.id.btn_settings);
        markView = view.findViewById(R.id.settings_mark_view);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
//                fragmentActivity.getSupportFragmentManager().popBackStack();
                getFragmentManager().popBackStack();
            }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.FragmentTransaction transaction = childFragmentManager.beginTransaction();
                transaction.replace(R.id.settings_framelayout, new SettingViewPagerFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        initialJoystickModule(view, R.id.module1);
        initialJoystickModule(view, R.id.module2);

        mControlWrap = new ControlWrap();
        initialJoypadMode();
        for (int i = 0; i < joyStickSurfaceViews.length; i++) {
            if (radius < joyStickSurfaceViews[i].getRadius()) {
                radius = joyStickSurfaceViews[i].getRadius();
            }
        }
        semiCircleProgressBarView = (SemiCircleProgressBarView) view.findViewById(R.id.semi_circle_bar);
        semiCircleProgressBarView.setProgressBarColor(Color.RED);
        semiCircleProgressBarView.setProgress(0);
//        wifiRssiReceiver = new WifiRssiReceiver(btnBack);

        tvPitch = (TextView) view.findViewById(R.id.tv_pitch);
        tvRoll = (TextView) view.findViewById(R.id.tv_roll);

        int size = (int) (getResources().getDimension(R.dimen.joystick_size) / getResources().getDisplayMetrics().density) / 2;
        final String[] stickList = new String[(size / 5) - 3];
        for (int i = 0; i < stickList.length; i++) {
            stickList[i] = String.valueOf(size);
            size -= 5;
        }

        tvBattery = (TextView) view.findViewById(R.id.tv_battery);
        tvAltitude = (TextView) view.findViewById(R.id.tv_altitude);

        Button btnEmergency = (Button) view.findViewById(R.id.emergency);
        Button btnAction = (Button) view.findViewById(R.id.take_off);
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
//        rtspFrameLayout = (FrameLayout) view.findViewById(R.id.rtsp_framelayout);
        surfaceView = (SurfaceView) view.findViewById(R.id.rtsp_surfaceview);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    mediaPlayer.reset();
                    Log.d(TAG, "setDisplay");
                    mediaPlayer.setDisplay(surfaceHolder);
                    Log.d(TAG, "setDataSource");
//                    mediaPlayer.setDataSource(getActivity(), Uri.parse(VIDEO_FILE_PATH));
                    mediaPlayer.setDataSource(VIDEO_FILE_PATH);
                    Log.d(TAG, "prepareAsync");
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
    }

    private void setSurfaceViewLayoutParams(int width, int height) {
        ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        surfaceView.setLayoutParams(layoutParams);
    }

    private void initialJoystickModule(View rootView, int moduleViewId) {
        final int moduleIndex = moduleViewId == R.id.module1 ? 0 : 1;
        View moduleView = rootView.findViewById(moduleViewId);
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
                                mControlWrap.pitch = ControlWrap.DEFAULT_VALUE;
                                mControlWrap.roll = ControlWrap.DEFAULT_VALUE;
                                tvPitch.setText("pitch: " + mControlWrap.pitch);
                                tvRoll.setText("roll: " + mControlWrap.roll);
//                                getController().control(mControlWrap.roll, mControlWrap.pitch, mControlWrap.throttle, mControlWrap.yaw);
//                                Log.d(TAG, "sendControl Throttle: " + mControlWrap.throttle);
//                                Log.d(TAG, "sendControl Yaw: " + mControlWrap.yaw);
//                                Log.d(TAG, "sendControl Pitch: " + mControlWrap.pitch);
//                                Log.d(TAG, "sendControl Roll: " + mControlWrap.roll);
                                sendControl();
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
        boolean joypadMode = DroneG2Application.settings[Setting.SettingType.JOYPAD_MODE.ordinal()].getValue() == Setting.ON ? true : false;
        boolean leftHanded = DroneG2Application.settings[Setting.SettingType.LEFT_HANDED.ordinal()].getValue() == Setting.ON ? true : false;

        if (joypadMode) {
            if (leftHanded) {
                controlType = new int[]{JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL, JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_YAW};
            } else {
                controlType = new int[]{JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_YAW, JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL};
            }
            isJoyModes = new boolean[]{true, true};
            bgDrawableIds = new int[]{R.drawable.image_button_bg, R.drawable.image_button_bg};
            stickDrawableIds = new int[]{R.drawable.redpoint, R.drawable.redpoint};
        } else {
            if (leftHanded) {
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
            final int rotation = ((WindowManager) fragmentActivity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
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
//                    getController().control(mControlWrap.roll, mControlWrap.pitch, mControlWrap.throttle, mControlWrap.yaw);
//                    Log.d(TAG, "sendControl Throttle: " + mControlWrap.throttle);
//                    Log.d(TAG, "sendControl Yaw: " + mControlWrap.yaw);
//                    Log.d(TAG, "sendControl Pitch: " + mControlWrap.pitch);
//                    Log.d(TAG, "sendControl Roll: " + mControlWrap.roll);
                    sendControl();
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
        return ((MainActivity) fragmentActivity).getDroneController();
    }


    //    private void showAddNewDroneDialog() {
//
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//        alertDialog.setTitle("Add Device");
//        alertDialog.setMessage("Enter Device ip");
//
//        final EditText input = new EditText(this);
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//        input.setLayoutParams(lp);
//        alertDialog.setView(input);
//
//        alertDialog.setPositiveButton("Add",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        String deviceIp = input.getText().toString();
//                        if (deviceIp.trim().length() <= 0) {
//                            return;
//                        }
//                        addDevice(deviceIp);
//                    }
//
//                });
//
//        alertDialog.setNegativeButton("Cancel",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//
//        alertDialog.show();
//    }

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
            this.yaw = changeValue(yaw) * 0.75f;
            sendControl();
            return (int) this.yaw;
        }

        private float changeValue(float value) {
            if (radius == 0) {
                radius = ((DroneG2Application) fragmentActivity.getApplicationContext()).joyStickRadius;
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
            TextView text = new TextView(fragmentActivity);
            text.setLayoutParams(params);
            text.setText(getItem(position).getName());

            return text;
        }
    }
}