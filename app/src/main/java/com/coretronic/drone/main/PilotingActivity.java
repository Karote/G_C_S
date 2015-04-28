package com.coretronic.drone.main;

import android.content.Context;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.coretronic.drone.LandscapeFragmentActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.WifiRssiReceiver;
import com.coretronic.drone.settings.SettingViewPagerFragment;
import com.coretronic.drone.ui.JoyStickSurfaceView;
import com.coretronic.drone.ui.SemiCircleProgressBarView;
import com.coretronic.dronecontrol.control.DroneController;

/**
 * Created by jiaLian on 15/4/1.
 */
public class PilotingActivity extends LandscapeFragmentActivity {
    private static final String TAG = PilotingActivity.class.getSimpleName();
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
        for (int i = 0; i < joyStickSurfaceViews.length; i++) {
            if (radius < joyStickSurfaceViews[i].getRadius()) {
                radius = joyStickSurfaceViews[i].getRadius();
            }
        }
        mControlWrap = new ControlWrap(radius);
        initialJoypadMode();

        SemiCircleProgressBarView semiCircleProgressBarView = (SemiCircleProgressBarView) findViewById(R.id.semi_circle_bar);
        semiCircleProgressBarView.setProgressBarColor(Color.RED);
        semiCircleProgressBarView.setProgress(55);
        wifiRssiReceiver = new WifiRssiReceiver(btnBack);

        tvPitch = (TextView) findViewById(R.id.tv_pitch);
        tvRoll = (TextView) findViewById(R.id.tv_roll);
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
                    public void onStickEvent(View view, int dx, int dy) {
                        if (((JoyStickSurfaceView) view).getControlType() == JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL) {
                            tvX.setText("Roll: " + mControlWrap.changeRoll(dx));
                            tvY.setText("Pitch: " + mControlWrap.changePitch(-dy));
                        } else {
                            tvX.setText("Yaw: " + mControlWrap.changeYaw(dx));
                            tvY.setText("Throttle: " + mControlWrap.changeThrottle(dy));
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
                            tvPitch.setText("pitch: " + 0 + "º");
                            tvRoll.setText("roll: " + 0 + "º");
                            isOnOrientationSensorMode = false;
                        }
                    }
                }

        );
        final ViewGroup.LayoutParams originalParams = joyStickSurfaceViews[moduleIndex].getLayoutParams();
        final int size = (int) getResources().getDimension(R.dimen.joystick_size);
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        final FrameLayout frameLayout = (FrameLayout) moduleView.findViewById(R.id.frame_layout);
        frameLayout.setOnTouchListener(new View.OnTouchListener()

                                       {
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
                    pitch = result[1];
                    roll = result[2];
//                    Log.d(TAG, "portrait");
                    break;
                case Surface.ROTATION_90:
                    pitch = result[2];
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
//            tvPitch.setText("pitch: " + pitch + "º");
//            tvRoll.setText("roll: " + roll + "º");
            if (isOnOrientationSensorMode) {
                tvPitch.setText("pitch: " + (pitch - startPitch) + "º");
                tvRoll.setText("roll: " + (roll - startRoll) + "º");
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void sendControl() {
        if (getController() != null) {
            getController().control(mControlWrap.roll, mControlWrap.pitch, mControlWrap.throttle, mControlWrap.yaw);
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

    private DroneController getController() {
        return this;
    }

    private class ControlWrap {
        private float pitch = 0;
        private float roll = 0;
        private float throttle = -80;
        private float yaw = 0;
        private final static int DEFAULT_MAX_VALUE = 100;
        private final static int DEFAULT_MIN_VALUE = -100;
        private int radius;

        ControlWrap(int radius) {
            this.radius = radius;
        }

        public int changePitch(float pitch) {
            this.pitch = pitch;
//            this.pitch = Math.max(Math.min(this.pitch, DEFAULT_MAX_VALUE), DEFAULT_MIN_VALUE);
            sendControl();
            return (int) this.pitch;
        }

        public int changeRoll(float roll) {
            this.roll = roll;
//            this.roll = Math.max(Math.min(this.roll, DEFAULT_MAX_VALUE), DEFAULT_MIN_VALUE);
            sendControl();
            return (int) this.roll;
        }

        public int changeThrottle(float throttle) {
            this.throttle = throttle;
//            this.throttle = Math.max(Math.min(this.throttle, DEFAULT_MAX_VALUE), DEFAULT_MIN_VALUE);
            sendControl();
            return (int) this.throttle;
        }

        public int changeYaw(float yaw) {
            this.yaw = yaw;
//            this.yaw = Math.max(Math.min(this.yaw, DEFAULT_MAX_VALUE), DEFAULT_MIN_VALUE);
            sendControl();
            return (int) this.yaw;
        }
    }
}