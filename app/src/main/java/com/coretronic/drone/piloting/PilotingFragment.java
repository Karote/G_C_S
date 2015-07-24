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
import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.piloting.settings.SettingViewPagerFragment;
import com.coretronic.drone.service.DroneDevice;
import com.coretronic.drone.ui.JoyStickSurfaceView;
import com.coretronic.drone.ui.SemiCircleProgressBarView;
import com.coretronic.drone.ui.StatusView;
import com.coretronic.drone.utility.AppUtils;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import java.lang.ref.WeakReference;


/**
 * Created by jiaLian on 15/4/1.
 */
public class PilotingFragment extends UnBindDrawablesFragment implements Drone.StatusChangedListener, DroneController.MediaCommandListener, View.OnClickListener, FragmentManager.OnBackStackChangedListener {
    private static final String TAG = PilotingFragment.class.getSimpleName();
    private static final String SEND_DRONE_CONTROL = "send drone control: ";

    private static final int DEFAULT_UNLOCK_HOLD_ALTITUDE = 0;
    private static final boolean HOLD_ALTITUDE_LOCK = true;
    private static final boolean HOLD_ALTITUDE_UNLOCK = false;

    private static final boolean ACTION_MODE_TAKE_OFF = true;
    private static final boolean ACTION_MODE_LANDING = false;

    private static final String VIDEO_FILE_PATH_RTSP_PREFIX = "rtsp://";
    private static final String VIDEO_FILE_PATH_G2_SUFFIX = "/live";
    private static final String VIDEO_FILE_PATH_2015_SUFFIX = ":8086";
    private static final String VIDEO_FILE_PATH_TEST = "rtsp://mm2.pcslab.com/mm/7m1000.mp4";

    private static final int MAX_SPEED = 50;
    private static final float DEFAULT_TAKE_OFF_ALTITUDE = 2.0f;

    private static final int HANDLER_RELEASE_CONTROL = 1;
    private static final int HANDLER_RECORDING_COUNTER = 2;

    private static final int CONTROL_RELEASE_DELAY_MILLIS = 120;

    private DroneDevice connectedDroneDevice = new DroneDevice(DroneDevice.DRONE_TYPE_FAKE, null, 0);

    private JoyStickSurfaceView[] joyStickSurfaceViews = new JoyStickSurfaceView[2];
    private View markView;

    private SemiCircleProgressBarView semiCircleProgressBarView;
    private TextView tvAltitude;
    private TextView tvSpeed;
    private Button btnAction;
    private StatusView statusView;
    private TextView tvRecordingTime;
    private LinearLayout llRecording;
    private Button btnHoldAlt;

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private FragmentManager childFragmentManager;
    private SensorManager sensorManager = null;
    private MainActivity activity;

    private LibVLC libvlc;
    private int videoWidth;
    private int videoHeight;
    private final static int VIDEO_SIZE_CHANGED = -1;

    private float phoneAngleScale;

    private float[] magneticValues = new float[3];
    private float[] accelerometerValues = new float[3];

    private int pitch;
    private int roll;
    private int startPitch;
    private int startRoll;
    private boolean isOrientationActionDown = false;
    private ControlWrap controlWrap;
    private int stickShiftRadius = 0;

    private String mrl = null;
    private float currentAltitude;
    private boolean isRecording = false;
    private int recordingTime = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        childFragmentManager = getChildFragmentManager();
        childFragmentManager.addOnBackStackChangedListener(this);
        connectedDroneDevice = activity.getConnectedDroneDevice();
        if (connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC) {
//            mrl = VIDEO_FILE_PATH_RTSP_PREFIX + connectedDroneDevice.getName() + VIDEO_FILE_PATH_2015_SUFFIX;
//            mrl = VIDEO_FILE_PATH_TEST;
        } else if (connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC_G2) {
            mrl = VIDEO_FILE_PATH_RTSP_PREFIX + connectedDroneDevice.getName() + VIDEO_FILE_PATH_G2_SUFFIX;
        }
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
        phoneAngleScale = ControlWrap.DEFAULT_RADIUS / (float) activity.getSetting(Setting.SettingType.PHONE_TILT).getMaxValue();
        activity.registerDroneStatusChangedListener(this);
        if (connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC_G2) {
            initialG2Setting();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (activity.getSettingValue(Setting.SettingType.JOYPAD_MODE) == Setting.JOYPAD_MODE_KINESICS) {
            registerKinesicsSensor();
        }
        createPlayer(mrl);
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterKinesicsSensor();
        releasePlayer();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(videoWidth, videoHeight);
    }

    @Override
    public void onDestroyView() {
        activity.unregisterDroneStatusChangedListener(this);
        super.onDestroyView();
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
    public void onAltitudeUpdate(final float altitude) {
        if (altitude > 0) {
            currentAltitude = altitude;
        }
        activity.runOnUiThread(new Runnable() {
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
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int speed = (int) (groundSpeed);
                semiCircleProgressBarView.setProgress(speed);
                tvSpeed.setText(speed + "");
            }
        });
    }

    @Override
    public void onLocationUpdate(final long lat, final long lon, final int eph) {
        activity.runOnUiThread(
                new Runnable() {
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

    private void assignViews(View view) {
        Button btnHome = (Button) view.findViewById(R.id.btn_home);
        Button btnEmergency = (Button) view.findViewById(R.id.btn_emergency);
        Button btnDocking = (Button) view.findViewById(R.id.btn_docking);
        Button btnSelfie = (Button) view.findViewById(R.id.btn_selfie);
        Button btnRecording = (Button) view.findViewById(R.id.btn_recording);
        Button btnCamera = (Button) view.findViewById(R.id.btn_camera);
        Button btnBack = (Button) view.findViewById(R.id.btn_back);
        Button btnSettings = (Button) view.findViewById(R.id.btn_settings);
        btnAction = (Button) view.findViewById(R.id.btn_action);
        btnHoldAlt = (Button) view.findViewById(R.id.btn_hold_altitude);

        btnDocking.setOnClickListener(this);
        btnSelfie.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnRecording.setOnClickListener(this);
        btnEmergency.setOnClickListener(this);
        btnHome.setOnClickListener(this);
        btnAction.setOnClickListener(this);
        btnAction.setSelected(ACTION_MODE_LANDING);

        markView = view.findViewById(R.id.settings_mark_view);
        llRecording = (LinearLayout) view.findViewById(R.id.ll_recording);
        tvRecordingTime = (TextView) view.findViewById(R.id.tv_recording_time);

        statusView = (StatusView) view.findViewById(R.id.status);
        if (connectedDroneDevice.getDroneType() == DroneDevice.DRONE_TYPE_CORETRONIC) {
            btnDocking.setVisibility(View.VISIBLE);
            btnSelfie.setVisibility(View.VISIBLE);
            btnHoldAlt.setVisibility(View.VISIBLE);
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
                            Message msg = Message.obtain(vlcHandler, VIDEO_SIZE_CHANGED, width, height);
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
                                break;
                            case JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_YAW:
                                controlWrap.changeYaw(dx);
                                controlWrap.changeThrottle(dy);
                                break;
                            case JoyStickSurfaceView.CONTROL_TYPE_PITCH_YAW:
                                controlWrap.changeYaw(dx);
                                controlWrap.changePitch(-dy);
                                break;
                            case JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_ROLL:
                                controlWrap.changeRoll(dx);
                                controlWrap.changeThrottle(dy);
                                break;
                        }

                        //G2 release control
                        if (action == MotionEvent.ACTION_UP) {
                            pilotingStatusControlReleaseHandler.sendEmptyMessageDelayed(HANDLER_RELEASE_CONTROL, CONTROL_RELEASE_DELAY_MILLIS);
                            pilotingStatusControlReleaseHandler.sendEmptyMessageDelayed(HANDLER_RELEASE_CONTROL, CONTROL_RELEASE_DELAY_MILLIS * 2);
                        }
                    }

                    @Override
                    public void onDoubleClick(View view) {
                        sendFlip();
                    }

                    @Override
                    public void onOrientationAction(int action) {
                        if (action == MotionEvent.ACTION_DOWN) {
                            startPitch = pitch;
                            startRoll = roll;
                            isOrientationActionDown = true;
                        } else if (action == MotionEvent.ACTION_UP) {
                            if (getController() != null) {
                                controlWrap.pitch = ControlWrap.DEFAULT_VALUE;
                                controlWrap.roll = ControlWrap.DEFAULT_VALUE;
                                sendControl();
                                //G2 release control
                                pilotingStatusControlReleaseHandler.sendEmptyMessageDelayed(HANDLER_RELEASE_CONTROL, CONTROL_RELEASE_DELAY_MILLIS);
                                pilotingStatusControlReleaseHandler.sendEmptyMessageDelayed(HANDLER_RELEASE_CONTROL, CONTROL_RELEASE_DELAY_MILLIS * 2);
                            }
                            isOrientationActionDown = false;
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
                                               } else if (event.getAction() == MotionEvent.ACTION_UP) {
                                                   joyStickSurfaceViews[moduleIndex].setLayoutParams(originalParams);
                                               }
                                               event.setLocation(event.getX() - params.leftMargin, event.getY() - params.topMargin);
                                               joyStickSurfaceViews[moduleIndex].onTouchEvent(event);
                                               return true;
                                           }
                                       }
        );
    }

    private void initialJoypadMode() {
        int[] controlType = new int[0];
        boolean[] isJoypads = new boolean[0];
        int joypadMode = activity.getSettingValue(Setting.SettingType.JOYPAD_MODE);
        boolean leftHanded = activity.getSettingValue(Setting.SettingType.LEFT_HANDED) == Setting.ON ;
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
            joyStickSurfaceViews[i].initJoyMode(controlType[i], isJoypads[i], activity.getSettingValue(Setting.SettingType.INTERFACE_OPACITY));
        }
    }

    private void registerKinesicsSensor() {
        if (sensorManager == null) {
            sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        }
    }

    private void unregisterKinesicsSensor() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
            sensorManager = null;
        }
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            int type = event.sensor.getType();
            switch (type) {
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magneticValues = event.values.clone();
                    break;
                case Sensor.TYPE_ACCELEROMETER:
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
            final int rotation = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
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

            if (isOrientationActionDown) {
                int rcPitch = pitch - startPitch;
                int rcRoll = roll - startRoll;
                int phoneAngleMax = activity.getSettingValue(Setting.SettingType.PHONE_TILT);
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
                    sendControl();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public void onBackStackChanged() {
        int backStackCount = childFragmentManager.getBackStackEntryCount();
        if (backStackCount == 0) {
            updateBackground(Color.TRANSPARENT, 1, true, View.VISIBLE);
            initialJoypadMode();

            if (activity.getSettingValue(Setting.SettingType.JOYPAD_MODE) == Setting.JOYPAD_MODE_KINESICS) {
                registerKinesicsSensor();
            } else {
                unregisterKinesicsSensor();
            }
        } else if (backStackCount == 1) {
            updateBackground(Color.BLACK, 0.85f, false, View.INVISIBLE);
        }
    }

    private void updateBackground(int color, float alpha, boolean needChangePaintAlpha, int visibility) {
        markView.setBackgroundColor(color);
        markView.setAlpha(alpha);

        for (JoyStickSurfaceView joyStickSurfaceView : joyStickSurfaceViews) {
            if (needChangePaintAlpha) {
                joyStickSurfaceView.setPaintPressedAlpha(activity.getSettingValue(Setting.SettingType.INTERFACE_OPACITY) / 100f);
            }
            joyStickSurfaceView.setVisibility(visibility);
        }
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
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        llRecording.setVisibility(View.VISIBLE);
                        tvRecordingTime.setText(AppUtils.stringForTime(0));
                    }
                });
                pilotingStatusControlReleaseHandler.sendEmptyMessageDelayed(HANDLER_RECORDING_COUNTER, 1000);
                recordingTime = 0;
                Log.d(TAG, "onCommandResult: START_RECORD");
                break;
            case STOP_RECORD:
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        llRecording.setVisibility(View.GONE);
                    }
                });
                pilotingStatusControlReleaseHandler.removeMessages(HANDLER_RECORDING_COUNTER);
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
            case R.id.btn_action:
                if (view.isSelected() == ACTION_MODE_LANDING) {
                    sendTakeoff();
                    view.setSelected(ACTION_MODE_TAKE_OFF);
                } else {
                    sendLanding();
                    view.setSelected(ACTION_MODE_LANDING);
                    resetHoldAltBtn();
                }
                break;
            case R.id.btn_emergency:
                sendEmergency();
                btnAction.setSelected(ACTION_MODE_LANDING);
                resetHoldAltBtn();
                break;
            case R.id.btn_hold_altitude:
                if (btnAction.isSelected() == ACTION_MODE_LANDING) {
                    break;
                }
                if (view.isSelected() == HOLD_ALTITUDE_LOCK) {
                    resetHoldAltBtn();
                } else {
                    view.setSelected(HOLD_ALTITUDE_LOCK);
                    sendHoldAlt(currentAltitude);
                }
                break;
            case R.id.btn_home:
                if (btnAction.isSelected() == ACTION_MODE_LANDING) {
                    break;
                }
                sendReturnToLaunch();
                break;
            case R.id.btn_camera:
                sendTakePhoto();
                break;
            case R.id.btn_recording:
                if (isRecording) {
                    sendStopRecord();
                    isRecording = false;
                } else {
                    sendStartRecord();
                    isRecording = true;
                }
                break;
            case R.id.btn_selfie:
//                sendSelfie();
                break;
            case R.id.btn_docking:
                if (btnAction.isSelected() == ACTION_MODE_LANDING) {
                    break;
                }
//                sendAutoDocking();
                break;
        }
    }

    private void sendControl() {
        if (getController() != null) {
            getController().control(controlWrap.roll, controlWrap.pitch, controlWrap.throttle, controlWrap.yaw);
            Log.d(TAG, SEND_DRONE_CONTROL +
                    "Throttle " + controlWrap.throttle +
                    ", Yaw " + controlWrap.yaw +
                    ", Pitch: " + controlWrap.pitch +
                    ", Roll: " + controlWrap.roll);
        }
    }

    private void sendEmergency() {
        if (getController() != null) {
            getController().emergency();
            Log.d(TAG, SEND_DRONE_CONTROL + "Emergency");
        }
    }

    private void sendTakeoff() {
        if (getController() != null) {
            getController().takeOff(DEFAULT_TAKE_OFF_ALTITUDE);
            Log.d(TAG, SEND_DRONE_CONTROL + "Take Off");
        }
    }

    private void sendLanding() {
        if (getController() != null) {
            getController().land();
            Log.d(TAG, SEND_DRONE_CONTROL + "Landing");
        }
    }

    private void sendHoldAlt(float altitude) {
        if (getController() != null) {
            getController().holdAlt(altitude);
            if (altitude == DEFAULT_UNLOCK_HOLD_ALTITUDE) {
                Log.d(TAG, SEND_DRONE_CONTROL + "Hold Altitude Unlock");
            } else {
                Log.d(TAG, SEND_DRONE_CONTROL + "Hold Altitude Lock " + altitude);
            }
        }
    }

    private void sendReturnToLaunch() {
        if (getController() != null) {
            getController().returnToLaunch();
            Log.d(TAG, SEND_DRONE_CONTROL + "Return to Launch");
        }
    }

    private void sendFlip() {
        if (getController() != null) {
            getController().flip(null);
            Log.d(TAG, SEND_DRONE_CONTROL + "Flip");
        }
    }

    private void sendTakePhoto() {
        if (getController() != null) {
            getController().takePhoto(this);
            Log.d(TAG, SEND_DRONE_CONTROL + "Take Photo");
        }
    }

    private void sendStartRecord() {
        if (getController() != null) {
            getController().startRecord(this);
            Log.d(TAG, SEND_DRONE_CONTROL + "Start Recording");
        }
    }

    private void sendStopRecord() {
        if (getController() != null) {
            getController().stopRecord(this);
            Log.d(TAG, SEND_DRONE_CONTROL + "Stop Recording");
        }
    }

    private void sendSelfie() {
//        if (getController() != null) {
//            Log.d(TAG, SEND_DRONE_CONTROL + "Smile Photo");
//        }
    }

    private void sendAutoDocking() {
        if (getController() != null) {
            getController().triggerAutoControl(DroneController.AutoControlMode.AUTO_DUCKING, null);
            Log.d(TAG, SEND_DRONE_CONTROL + "Auto Docking");
        }
    }

    private DroneController getController() {
        return activity.getDroneController();
    }

    private void resetHoldAltBtn() {
        btnHoldAlt.setSelected(HOLD_ALTITUDE_UNLOCK);
        sendHoldAlt(DEFAULT_UNLOCK_HOLD_ALTITUDE);
    }

    private void initialG2Setting() {
        for (Setting setting : activity.getSettings()) {
            if (setting.getParameterType() != null) {
                activity.setParameters(setting.getParameterType(), setting.getParameter());
                Log.d(TAG, "Initial G2 parameter" + setting.getParameterType() + ", " + setting.getParameter().getValue());
            }
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
            if (msg.what == VIDEO_SIZE_CHANGED) {
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
                sendControl();
            } else if (msg.what == HANDLER_RECORDING_COUNTER) {
                tvRecordingTime.setText((AppUtils.stringForTime(++recordingTime)) + "");
                this.sendEmptyMessageDelayed(HANDLER_RECORDING_COUNTER, 1000);
            }
        }
    }

    private class ControlWrap {
        public final static int DEFAULT_VALUE = 0;
        public final static int DEFAULT_RADIUS = 100;
        private float pitch = 0;
        private float roll = 0;
        private float throttle = 0;
        private float yaw = 0;

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