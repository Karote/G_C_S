package com.coretronic.drone.piloting;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coretronic.drone.Drone;
import com.coretronic.drone.DroneApplication;
import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.piloting.settings.SettingViewPagerFragment;
import com.coretronic.drone.service.DroneDevice;
import com.coretronic.drone.ui.JoyStickSurfaceView;
import com.coretronic.drone.ui.SemiCircleProgressBarView;
import com.coretronic.drone.ui.StatusView;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;


/**
 * Created by jiaLian on 15/4/1.
 */
public class PilotingFragment extends UnBindDrawablesFragment implements Drone.StatusChangedListener, DroneController.MediaCommandListener, View.OnClickListener {
    private static final String TAG = PilotingFragment.class.getSimpleName();
    private static final String SEND_DRONE_CONTROL = "send drone control: ";

    private static final int UNLOCK_HOLD_ALTITUDE_DEFAULT = 0;
    private static final boolean HOLD_ALTITUDE_UNLOCK = false;
    private static final boolean HOLD_ALTITUDE_LOCK = true;
    //    private static final float M_S2KM_H = 3.6f;
    private static final String VIDEO_FILE_PATH_RTSP_PREFIX = "rtsp://";
    private static final String VIDEO_FILE_PATH_TEST = "rtsp://mm2.pcslab.com/mm/7m1000.mp4";
    private static final String VIDEO_FILE_PATH_G2_SUFFIX = "/live";
    private static final String VIDEO_FILE_PATH_2015_SUFFIX = ":8086";

    private static final int ORIENTATION_SENSOR_ANGLE_MAX = 30;
    public static final int MAX_SPEED = 50;
    private static final int TAKE_OFF_ALTITUDE = 1;

    private static final int HANDLER_RELEASE_CONTROL = 1;
    private static final int HANDLER_RECORDING_TIME = 2;
    private static final int CONTROL_RELEASE_DELAY_MILLIS = 120;

    private DroneDevice connectedDroneDevice = new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, null, 0);

    public static JoyStickSurfaceView[] joyStickSurfaceViews = new JoyStickSurfaceView[2];
    public static View markView;

    private SemiCircleProgressBarView semiCircleProgressBarView;
    private TextView tvAltitude;
    private TextView tvSpeed;
    private Button btnAction;
    private StatusView statusView;
    private TextView tvRecordingTime;
    private LinearLayout llRecording;

    //    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private FragmentManager childFragmentManager;
    private SensorManager sensorManager;
    private FragmentActivity fragmentActivity;

    private LibVLC libvlc;
    private int videoWidth;
    private int videoHeight;
    private final static int VideoSizeChanged = -1;

    private float phoneAngleScale = ControlWrap.DEFAULT_RADIUS / (float) DroneApplication.settings[Setting.SettingType.PHONE_TILT.ordinal()].getMaxValue();

    private float[] magneticValues = new float[3];
    private float[] accelerometerValues = new float[3];

    private int pitch;
    private int roll;
    private int startPitch;
    private int startRoll;
    private boolean isOnOrientationSensorMode = false;
    private boolean isTakeOff = false;
    private ControlWrap controlWrap;
    private int stickShiftRadius = 0;


    private String mrl = null;
    private float currentAltitude;
    private boolean isRecording = false;
    private int recordingTime = 0;
//    private long testTime;
//    private int testCount=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentActivity = getActivity();
        childFragmentManager = getChildFragmentManager();
        sensorManager = (SensorManager) fragmentActivity.getSystemService(Context.SENSOR_SERVICE);
        connectedDroneDevice = ((MainActivity) fragmentActivity).getConnectedDroneDevice();
        if (connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC) {
            mrl = VIDEO_FILE_PATH_RTSP_PREFIX + connectedDroneDevice.getName() + VIDEO_FILE_PATH_2015_SUFFIX;
//            mrl = VIDEO_FILE_PATH_TEST;
        } else if (connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC_G2) {
            mrl = VIDEO_FILE_PATH_RTSP_PREFIX + connectedDroneDevice.getName() + VIDEO_FILE_PATH_G2_SUFFIX;
        }
//        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mediaPlayer) {
//                Log.d(TAG, "onPrepared");
//                mediaPlayer.start();
//                surfaceView.setBackgroundColor(Color.TRANSPARENT);
//            }
//        });
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
        ((MainActivity) fragmentActivity).registerDroneStatusChangedListener(this);
        if (connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC_G2) {
            initialG2Setting();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        createPlayer(mrl);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
        releasePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
//        if (mediaPlayer != null) {
//            mediaPlayer.release();
//        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(videoWidth, videoHeight);
    }

    @Override
    public void onDestroyView() {
        ((MainActivity) fragmentActivity).unregisterDroneStatusChangedListener(this);
        super.onDestroyView();
    }

    @Override
    public void onBatteryUpdate(final int battery) {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusView.setBatteryStatus(battery);
            }
        });
    }

    @Override
    public void onAltitudeUpdate(final float altitude) {
        if (altitude > 0) {
            currentAltitude = altitude;
        }
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvAltitude.setText(String.format("%.1fm", altitude));
            }
        });
    }

    @Override
    public void onRadioSignalUpdate(int rssi) {
        Log.d(TAG, "radio signal:" + rssi);
    }

    @Override
    public void onSpeedUpdate(final float groundSpeed) {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int speed = (int) (groundSpeed /* M_S2KM_H*/);
                semiCircleProgressBarView.setProgress(speed);
                tvSpeed.setText(speed + "");
            }
        });
    }

    @Override
    public void onLocationUpdate(final long lat, final long lon, final int eph) {
        fragmentActivity.runOnUiThread(new Runnable() {
                                           @Override
                                           public void run() {
                                               if (connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC_G2) {
                                                   statusView.setGpsVisibility(eph == 1 ? View.VISIBLE : View.GONE);
                                               } else if (connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC) {
                                                   statusView.setGpsVisibility((eph == 0 || eph == 9999) ? View.GONE : View.VISIBLE);
                                               }
                                           }
                                       }
        );

    }

    @Override
    public void onHeadingUpdate(int heading) {

    }

    private void assignViews(View view) {
        Button btnHome = (Button) view.findViewById(R.id.btn_home);
        Button btnHoldAlt = (Button) view.findViewById(R.id.btn_hold_altitude);
        Button btnEmergency = (Button) view.findViewById(R.id.btn_emergency);
        Button btnDocking = (Button) view.findViewById(R.id.btn_docking);
        Button btnSelfie = (Button) view.findViewById(R.id.btn_selfie);
        Button btnRecording = (Button) view.findViewById(R.id.btn_recording);
        Button btnCamera = (Button) view.findViewById(R.id.btn_camera);
        Button btnBack = (Button) view.findViewById(R.id.btn_back);
        Button btnSettings = (Button) view.findViewById(R.id.btn_settings);
        btnAction = (Button) view.findViewById(R.id.btn_take_off);

        btnDocking.setOnClickListener(this);
        btnSelfie.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnRecording.setOnClickListener(this);
        btnEmergency.setOnClickListener(this);
        btnHome.setOnClickListener(this);
        btnAction.setOnClickListener(this);

        markView = view.findViewById(R.id.settings_mark_view);
        llRecording = (LinearLayout) view.findViewById(R.id.ll_recording);
        tvRecordingTime = (TextView) view.findViewById(R.id.tv_recording_time);

        statusView = (StatusView) view.findViewById(R.id.status);
        if (connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC) {
            btnDocking.setVisibility(View.VISIBLE);
            btnSelfie.setVisibility(View.VISIBLE);
            btnHoldAlt.setVisibility(View.VISIBLE);
            btnHoldAlt.setTag(HOLD_ALTITUDE_UNLOCK);
            btnHoldAlt.setOnClickListener(this);
        }

        initialJoystickModule(view, R.id.module1);
        initialJoystickModule(view, R.id.module2);

        controlWrap = new ControlWrap();
        initialJoypadMode();

        semiCircleProgressBarView = (SemiCircleProgressBarView) view.findViewById(R.id.semi_circle_bar);
        semiCircleProgressBarView.setProgressBarColor(Color.RED);
        semiCircleProgressBarView.setMaxProgress(MAX_SPEED);
        tvSpeed = (TextView) view.findViewById(R.id.tv_speed);

//        int size = (int) (getResources().getDimension(R.dimen.joypad_size) / getResources().getDisplayMetrics().density) / 2;
//        final String[] stickList = new String[(size / 5) - 3];
//        for (int i = 0; i < stickList.length; i++) {
//            stickList[i] = String.valueOf(size);
//            size -= 5;
//        }

        tvAltitude = (TextView) view.findViewById(R.id.tv_altitude);

        surfaceView = (SurfaceView) view.findViewById(R.id.rtsp_surfaceview);
        holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
//                try {
//                    mediaPlayer.reset();
//                    mediaPlayer.setDisplay(surfaceHolder);
//                    mediaPlayer.setDataSource(VIDEO_FILE_PATH);
//                    mediaPlayer.prepareAsync();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
                if (libvlc != null)
                    libvlc.attachSurface(surfaceHolder.getSurface(), new IVideoPlayer() {
                        @Override
                        public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
                            Message msg = Message.obtain(vlcHandler, VideoSizeChanged, width, height);
                            msg.sendToTarget();
                        }
                    });
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
    }

    private void initialJoystickModule(View rootView, int moduleViewId) {
        final int moduleIndex = moduleViewId == R.id.module1 ? 0 : 1;
        View moduleView = rootView.findViewById(moduleViewId);
        joyStickSurfaceViews[moduleIndex] = (JoyStickSurfaceView) moduleView.findViewById(R.id.joystick);
        joyStickSurfaceViews[moduleIndex].setOnStickListener(
                new JoyStickSurfaceView.OnStickListener() {
                    @Override
                    public void onStickMoveEvent(View view, int action, int dx, int dy) {
                        switch (((JoyStickSurfaceView) view).getControlType()) {
                            case JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL:
                                controlWrap.changeRoll(dx);
                                controlWrap.changePitch(-dy);
                                Log.d(TAG, "onStickMoveEvent: Pitch " + (-dy) + ", Roll " + dx);
                                break;
                            case JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_YAW:
                                controlWrap.changeYaw(dx);
                                controlWrap.changeThrottle(dy);
                                Log.d(TAG, "onStickMoveEvent: Throttle " + dy + ", Yaw: " + dx);
                                break;
                            case JoyStickSurfaceView.CONTROL_TYPE_PITCH_YAW:
                                controlWrap.changeYaw(dx);
                                controlWrap.changePitch(-dy);
                                Log.d(TAG, "onStickMoveEvent: Pitch " + (-dy) + ", Yaw " + dx);
                                break;
                            case JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_ROLL:
                                controlWrap.changeRoll(dx);
                                controlWrap.changeThrottle(dy);
                                Log.d(TAG, "onStickMoveEvent: Throttle " + dy + ", Roll: " + dx);
                                break;
                        }
//                        if (((JoyStickSurfaceView) view).getControlType() == JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL) {
//                            controlWrap.changeRoll(dx);
//                            controlWrap.changePitch(-dy);
//                            Log.d(TAG, "onStickMoveEvent: Pitch " + (-dy) + ", Roll " + dx);
//
//                        } else {
//                            controlWrap.changeYaw(dx);
//                            controlWrap.changeThrottle(dy);
//                            Log.d(TAG, "onStickMoveEvent: Throttle " + dy + ", Yaw: " + dx);
//                        }
                        if (action == MotionEvent.ACTION_UP) {
                            pilotingStatusControlReleaseHandler.sendEmptyMessageDelayed(HANDLER_RELEASE_CONTROL, CONTROL_RELEASE_DELAY_MILLIS);
                            pilotingStatusControlReleaseHandler.sendEmptyMessageDelayed(HANDLER_RELEASE_CONTROL, CONTROL_RELEASE_DELAY_MILLIS * 2);
                        }
                    }

                    @Override
                    public void onDoubleClick(View view) {
                        sendFlip();
                        Log.d(TAG, "onDoubleClick");
                    }

                    @Override
                    public void onOrientationSensorMode(int action) {
                        if (action == MotionEvent.ACTION_DOWN) {
                            startPitch = pitch;
                            startRoll = roll;
                            isOnOrientationSensorMode = true;
                            Log.d(TAG, "onOrientationSensorMode: Action down");
//                            testTime = System.currentTimeMillis();
//                            testCount=0;
                        } else if (action == MotionEvent.ACTION_UP) {
                            Log.d(TAG, "onOrientationSensorMode: Action up");
                            if (getController() != null) {
                                controlWrap.pitch = ControlWrap.DEFAULT_VALUE;
                                controlWrap.roll = ControlWrap.DEFAULT_VALUE;
                                sendControl();
                                pilotingStatusControlReleaseHandler.sendEmptyMessageDelayed(HANDLER_RELEASE_CONTROL, CONTROL_RELEASE_DELAY_MILLIS);
                                pilotingStatusControlReleaseHandler.sendEmptyMessageDelayed(HANDLER_RELEASE_CONTROL, CONTROL_RELEASE_DELAY_MILLIS * 2);
                            }
                            isOnOrientationSensorMode = false;
                        }
                    }
                }

        );
        final ViewGroup.LayoutParams originalParams = joyStickSurfaceViews[moduleIndex].getLayoutParams();
        final int size = (int) getResources().getDimension(R.dimen.joypad_size);
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
        int[] controlType = new int[0];
        boolean[] isJoypads = new boolean[0];
        int joypadMode = DroneApplication.settings[Setting.SettingType.JOYPAD_MODE.ordinal()].getValue();
        boolean leftHanded = DroneApplication.settings[Setting.SettingType.LEFT_HANDED.ordinal()].getValue() == Setting.ON ? true : false;
        switch (joypadMode) {
            case Setting.JOYPAD_MODE_USA:
                if (leftHanded) {
                    controlType = new int[]{JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL, JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_YAW};
                } else {
                    controlType = new int[]{JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_YAW, JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL};
                }
                isJoypads = new boolean[]{true, true};
                break;
            case Setting.JOYPAD_MODE_KINESICS:
                if (leftHanded) {
                    controlType = new int[]{JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL, JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_YAW};
                    isJoypads = new boolean[]{false, true};
                } else {
                    controlType = new int[]{JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_YAW, JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL};
                    isJoypads = new boolean[]{true, false};
                }
                break;
            case Setting.JOYPAD_MODE_JAPAN:
                if (leftHanded) {
                    controlType = new int[]{JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_ROLL, JoyStickSurfaceView.CONTROL_TYPE_PITCH_YAW};
                } else {
                    controlType = new int[]{JoyStickSurfaceView.CONTROL_TYPE_PITCH_YAW, JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_ROLL};
                }
                isJoypads = new boolean[]{true, true};
                break;
        }

        for (int i = 0; i < joyStickSurfaceViews.length; i++) {
            joyStickSurfaceViews[i].initJoyMode(controlType[i], isJoypads[i]);
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
                int phoneAngleMax = DroneApplication.settings[Setting.SettingType.PHONE_TILT.ordinal()].getValue();
                if (getController() != null) {
                    if (rcPitch > phoneAngleMax) {
                        rcPitch = phoneAngleMax;
                    } else if (rcPitch < -phoneAngleMax) {
                        rcPitch = -phoneAngleMax;
                    }
                    if (rcRoll > phoneAngleMax) {
                        rcRoll = phoneAngleMax;
                    } else if (rcRoll < -phoneAngleMax) {
                        rcRoll = -phoneAngleMax;
                    }
                    controlWrap.pitch = rcPitch * phoneAngleScale;
                    controlWrap.roll = rcRoll * phoneAngleScale;
                    Log.d(TAG, "Phone angle scale: " + phoneAngleScale);
                    Log.d(TAG, "Phone angle: " + controlWrap.pitch + ", " + controlWrap.roll);
                    sendControl();
//                    if (System.currentTimeMillis() - testTime < 1000) {
//                        Log.d(TAG, "kinesics count: " + (testCount++));
//                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void sendControl() {
        if (getController() != null /*&& isTakeOff*/) {
            getController().control(controlWrap.roll, controlWrap.pitch, controlWrap.throttle, controlWrap.yaw);
            Log.d(TAG, SEND_DRONE_CONTROL +
                    "Throttle " + controlWrap.throttle +
                    ", Yaw " + controlWrap.yaw +
                    ", Pitch: " + controlWrap.pitch +
                    ", Roll: " + controlWrap.roll);
        }
    }

    private void sendLanding() {
        if (getController() != null) {
            getController().land();
        }
    }

    private void sendTakeoff() {
        if (getController() != null) {
            getController().takeOff(TAKE_OFF_ALTITUDE);
        }
    }

    private void sendEmergency() {
        if (getController() != null) {
            getController().emergency();
        }
    }

    private void sendHoldAlt(int altitude) {
        getController().holdAlt(altitude);
    }

    private void sendReturnToLanch() {
        getController().returnToLaunch();
    }

    private void sendFlip() {
        getController().flip(null);
    }

    private DroneController getController() {
        return ((MainActivity) fragmentActivity).getDroneController();
    }

    private void setSize(int width, int height) {
        videoWidth = width;
        videoHeight = height;
        holder.setFixedSize(videoWidth, videoHeight);
    }

    private void createPlayer(String media) {
        if (mrl == null) {
            return;
        }
        releasePlayer();
        try {
            // Create a new media player
            libvlc = LibVLC.getInstance();
            libvlc.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
            LibVLC.restart(getActivity());
            EventHandler.getInstance().addHandler(vlcHandler);
            holder.setFormat(PixelFormat.TRANSLUCENT);
            MediaList list = libvlc.getMediaList();
            list.clear();
            list.add(new Media(libvlc, LibVLC.PathToURI(media)), false);
            libvlc.playIndex(0);
        } catch (Exception e) {
        }
    }

    private void releasePlayer() {
        if (libvlc == null)
            return;
        EventHandler.getInstance().removeHandler(vlcHandler);
        libvlc.stop();
        libvlc.detachSurface();
        holder = null;
        libvlc.closeAout();
        libvlc.destroy();
        libvlc = null;

        videoWidth = 0;
        videoHeight = 0;
    }


    @Override
    public void onCommandResult(MediaCommand mediaCommand, boolean isSuccess, Object datd) {
        switch (mediaCommand) {
            case START_RECORD:
                fragmentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        llRecording.setVisibility(View.VISIBLE);
                    }
                });
                pilotingStatusControlReleaseHandler.sendEmptyMessageDelayed(HANDLER_RECORDING_TIME, 1000);
                recordingTime = 0;
                Log.d(TAG, "onCommandResult: START_RECORD");
                break;
            case STOP_RECORD:
                fragmentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        llRecording.setVisibility(View.GONE);
                    }
                });
                pilotingStatusControlReleaseHandler.removeMessages(HANDLER_RECORDING_TIME);
                Log.d(TAG, "onCommandResult: STOP_RECORD");
                break;
            case TAKE_PHOTO:
                Log.d(TAG, "onCommandResult: TAKE_PHOTO");
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.btn_settings:
                FragmentTransaction transaction = childFragmentManager.beginTransaction();
                transaction.replace(R.id.settings_framelayout, new SettingViewPagerFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.btn_take_off:
                Button btn = (Button) view;
                if (!isTakeOff) {
                    sendTakeoff();
                    isTakeOff = true;
                    btn.setBackgroundResource(R.drawable.btn_pilot_landing);
                    Log.d(TAG, SEND_DRONE_CONTROL + "take off");
                } else {
                    sendLanding();
                    isTakeOff = false;
                    btn.setBackgroundResource(R.drawable.btn_pilot_takeoff);
                    Log.d(TAG, SEND_DRONE_CONTROL + "landing");
                }
                break;
            case R.id.btn_emergency:
                sendEmergency();
                isTakeOff = false;
                btnAction.setBackgroundResource(R.drawable.btn_pilot_takeoff);
                Log.d(TAG, SEND_DRONE_CONTROL + "Emergency");
                break;
            case R.id.btn_hold_altitude:
                if (!isTakeOff) {
                    return;
                }
                if ((boolean) view.getTag() == HOLD_ALTITUDE_LOCK) {
                    view.setBackgroundResource(R.drawable.ico_pilot_altitude_unlock);
                    view.setTag(HOLD_ALTITUDE_UNLOCK);
                    sendHoldAlt(UNLOCK_HOLD_ALTITUDE_DEFAULT);
                    Log.d(TAG, SEND_DRONE_CONTROL + "Hold Altitude Unlock");
                } else {
                    view.setBackgroundResource(R.drawable.ico_pilot_altitude_lock);
                    view.setTag(HOLD_ALTITUDE_LOCK);
                    sendHoldAlt((int) currentAltitude);
                    Log.d(TAG, SEND_DRONE_CONTROL + "Hold Altitude Lock " + currentAltitude);
                }
                break;
            case R.id.btn_home:
                if (!isTakeOff) {
                    return;
                }
                sendReturnToLanch();
                Log.d(TAG, SEND_DRONE_CONTROL + "RTL");
                break;
            case R.id.btn_camera:
                getController().takePhoto(PilotingFragment.this);
                break;
            case R.id.btn_recording:
                if (isRecording) {
                    getController().stopRecord(PilotingFragment.this);
                    isRecording = false;
                } else {
                    getController().startRecord(PilotingFragment.this);
                    isRecording = true;
                }
                break;
            case R.id.btn_selfie:

                break;
            case R.id.btn_docking:

                break;
        }
    }

    private Handler vlcHandler = new VlcHandler(this);

    private static class VlcHandler extends Handler {
        private WeakReference<PilotingFragment> mOwner;

        public VlcHandler(PilotingFragment owner) {
            mOwner = new WeakReference<PilotingFragment>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            PilotingFragment player = mOwner.get();

            // SamplePlayer events
            if (msg.what == VideoSizeChanged) {
                player.setSize(msg.arg1, msg.arg2);
                return;
            }

            // Libvlc events
            Bundle b = msg.getData();
            switch (b.getInt("event")) {
                case EventHandler.MediaPlayerEndReached:
                    player.releasePlayer();
                    player.surfaceView.setBackgroundResource(R.drawable.bg_piloting);
                    Log.d(TAG, "MediaPlayerEndReached");
                    break;
                case EventHandler.MediaPlayerPlaying:
                    player.surfaceView.setBackgroundColor(Color.TRANSPARENT);
                    Log.d(TAG, "MediaPlayerPlaying");
                    break;
                case EventHandler.MediaPlayerPaused:
                    Log.d(TAG, "MediaPlayerPaused");
                    break;
                case EventHandler.MediaPlayerStopped:
                    player.surfaceView.setBackgroundResource(R.drawable.bg_piloting);
                    Log.d(TAG, "MediaPlayerStopped");
                    break;
                default:
                    break;
            }
        }
    }

    private PilotingStatusControlReleaseHandler pilotingStatusControlReleaseHandler = new PilotingStatusControlReleaseHandler();

    private class PilotingStatusControlReleaseHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_RELEASE_CONTROL) {
                Log.d(TAG, SEND_DRONE_CONTROL + "release control");
                sendControl();
            } else if (msg.what == HANDLER_RECORDING_TIME) {
                tvRecordingTime.setText((stringForTime(++recordingTime)) + "");
                this.sendEmptyMessageDelayed(HANDLER_RECORDING_TIME, 1000);
            }
        }
    }

    private void initialG2Setting() {
        for (Setting setting : DroneApplication.settings) {
            if (setting.getParameterType() != null) {
                ((MainActivity) fragmentActivity).setParameters(setting.getParameterType(), setting.getParameter());
                Log.d(TAG, "Initial G2 parameter" + setting.getParameterType() + ", " + setting.getParameter().getValue());
            }
        }
    }

    public static String stringForTime(int time) {
        Formatter formatter = new Formatter(new StringBuilder(), Locale.getDefault());
        int totalSeconds = time/* / 1000*/;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private class ControlWrap {
        private float pitch = 0;
        private float roll = 0;
        private float throttle = 0;
        private float yaw = 0;
        public final static int DEFAULT_VALUE = 0;
        public final static int DEFAULT_RADIUS = 100;

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
            this.yaw = changeValue(yaw);// * 0.7f;
            sendControl();
            return (int) this.yaw;
        }

        private float changeValue(float value) {
            if (stickShiftRadius == 0) {
                stickShiftRadius = joyStickSurfaceViews[0].getStickShiftRadius();
            }
            if (value == DEFAULT_VALUE) {
                return DEFAULT_VALUE;
            } else {
                return (value / stickShiftRadius) * DEFAULT_RADIUS;
            }
        }
    }
}