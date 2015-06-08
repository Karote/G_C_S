package com.coretronic.drone.piloting;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
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
import android.widget.ImageButton;
import android.widget.TextView;

import com.coretronic.drone.Drone;
import com.coretronic.drone.DroneG2Application;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.piloting.settings.SettingViewPagerFragment;
import com.coretronic.drone.ui.JoyStickSurfaceView;
import com.coretronic.drone.ui.SemiCircleProgressBarView;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import java.lang.ref.WeakReference;


/**
 * Created by jiaLian on 15/4/1.
 */
public class PilotingFragment extends UnBindDrawablesFragment implements Drone.StatusChangedListener {
    private static final String TAG = PilotingFragment.class.getSimpleName();
    private static final float M_S2KM_H = 3.6f;

    //    private static final String VIDEO_FILE_PATH = "rtsp://mm2.pcslab.com/mm/7m1000.mp4";
    private static final String VIDEO_FILE_PATH = "rtsp://192.168.42.1/live";
//    private static final String VIDEO_FILE_PATH = "rtsp://192.168.1.171:8086";

    public static final float ORIENTATION_SENSOR_SCALE = 2.5f;
    public static final int ORIENTATION_SENSOR_ANGLE_MAX = 30;

    public static final String TAKE_OFF = "Take Off";
    public static final String LANDING = "Landing";
    public static final int MAX_SPEED = 50;

    public static JoyStickSurfaceView[] joyStickSurfaceViews = new JoyStickSurfaceView[2];
    public static View markView;
    private TextView tvPitch;
    private TextView tvRoll;
    private SemiCircleProgressBarView semiCircleProgressBarView;
    private TextView tvBattery;
    private TextView tvAltitude;
    private Button btnAction;

    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private FragmentManager childFragmentManager;
    private SensorManager sensorManager;
    private FragmentActivity fragmentActivity;

    private LibVLC libvlc;
    private int videoWidth;
    private int videoHeight;
    private final static int VideoSizeChanged = -1;

    private float[] magneticValues = new float[3];
    private float[] accelerometerValues = new float[3];
    private int pitch;
    private int roll;
    private int startPitch;
    private int startRoll;
    private boolean isOnOrientationSensorMode = false;
    private boolean isTakeOff = false;
    private ControlWrap mControlWrap;
    private int radius = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentActivity = getActivity();
        childFragmentManager = getChildFragmentManager();
        sensorManager = (SensorManager) fragmentActivity.getSystemService(Context.SENSOR_SERVICE);
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
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        createPlayer(VIDEO_FILE_PATH);
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
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
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
        Button btnEmergency = (Button) view.findViewById(R.id.emergency);
        btnAction = (Button) view.findViewById(R.id.take_off);
        btnEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmergency();
                isTakeOff = false;
                btnAction.setText(TAKE_OFF);
                Log.d(TAG, "Emergency");
            }
        });
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                if (btn.getText().equals(TAKE_OFF)) {
                    sendTakeoff();
                    isTakeOff = true;
                    btn.setText(LANDING);
                    Log.d(TAG, TAKE_OFF);
                } else {
                    sendLanding();
                    isTakeOff = false;
                    btn.setText(TAKE_OFF);
                    Log.d(TAG, LANDING);
                }
            }
        });
        ImageButton btnBack = (ImageButton) view.findViewById(R.id.btn_back);
        ImageButton btnSettings = (ImageButton) view.findViewById(R.id.btn_settings);
        markView = view.findViewById(R.id.settings_mark_view);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = childFragmentManager.beginTransaction();
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
        semiCircleProgressBarView.setMaxProgress(MAX_SPEED);

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
                    libvlc.attachSurface(holder.getSurface(), new IVideoPlayer() {
                        @Override
                        public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
                            Message msg = Message.obtain(handler, VideoSizeChanged, width, height);
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
        final TextView tvX = (TextView) moduleView.findViewById(R.id.tv_dx);
        final TextView tvY = (TextView) moduleView.findViewById(R.id.tv_dy);
        joyStickSurfaceViews[moduleIndex] = (JoyStickSurfaceView) moduleView.findViewById(R.id.joystick);
        joyStickSurfaceViews[moduleIndex].setOnStickListener(
                new JoyStickSurfaceView.OnStickListener() {
                    @Override
                    public void onStickMoveEvent(View view, int action, int dx, int dy) {
                        if (((JoyStickSurfaceView) view).getControlType() == JoyStickSurfaceView.CONTROL_TYPE_PITCH_ROLL) {
                            tvX.setText("Roll: " + mControlWrap.changeRoll(dx));
                            tvY.setText("Pitch: " + mControlWrap.changePitch(-dy));

                            Log.d(TAG, "onStickMoveEvent Pitch: " + (-dy));
                            Log.d(TAG, "onStickMoveEvent Roll: " + dx);
                        } else {
                            tvX.setText("Yaw: " + mControlWrap.changeYaw(dx));
                            tvY.setText("Throttle: " + mControlWrap.changeThrottle(dy));
                            Log.d(TAG, "onStickMoveEvent Throttle: " + dy);
                            Log.d(TAG, "onStickMoveEvent Yaw: " + dx);
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
                    if (rcPitch > ORIENTATION_SENSOR_ANGLE_MAX) {
                        rcPitch = ORIENTATION_SENSOR_ANGLE_MAX;
                    } else if (rcPitch < -ORIENTATION_SENSOR_ANGLE_MAX) {
                        rcPitch = -ORIENTATION_SENSOR_ANGLE_MAX;
                    }
                    if (rcRoll > ORIENTATION_SENSOR_ANGLE_MAX) {
                        rcRoll = ORIENTATION_SENSOR_ANGLE_MAX;
                    } else if (rcRoll < -ORIENTATION_SENSOR_ANGLE_MAX) {
                        rcRoll = -ORIENTATION_SENSOR_ANGLE_MAX;
                    }
                    mControlWrap.pitch = rcPitch * ORIENTATION_SENSOR_SCALE;
                    mControlWrap.roll = rcRoll * ORIENTATION_SENSOR_SCALE;
                    tvPitch.setText("pitch: " + mControlWrap.pitch);
                    tvRoll.setText("roll: " + mControlWrap.roll);
                    sendControl();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void sendControl() {
        if (getController() != null && isTakeOff) {
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

    private void setSize(int width, int height) {
        videoWidth = width;
        videoHeight = height;
        holder.setFixedSize(videoWidth, videoHeight);
    }

    private void createPlayer(String media) {
        releasePlayer();
        try {
            // Create a new media player
            libvlc = LibVLC.getInstance();
            libvlc.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
            LibVLC.restart(getActivity());
            EventHandler.getInstance().addHandler(handler);
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
        EventHandler.getInstance().removeHandler(handler);
        libvlc.stop();
        libvlc.detachSurface();
        holder = null;
        libvlc.closeAout();
        libvlc.destroy();
        libvlc = null;

        videoWidth = 0;
        videoHeight = 0;
    }

    private Handler handler = new VlcHandler(this);

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
            this.yaw = changeValue(yaw) * 0.7f;
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
}