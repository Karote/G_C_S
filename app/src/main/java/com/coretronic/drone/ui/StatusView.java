package com.coretronic.drone.ui;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.util.Utils;

/**
 * Created by jiaLian on 15/6/9.
 */
public class StatusView extends LinearLayout {

    private final static int GPS_UPDATE_GAP_NO_SIGNAL = 5 * 1000;
    private final static int GPS_UPDATE_PERIOD = 1 * 1000;

    private final static int[] DRONE_BATTERY_LOW_THRESHOLD_ARRAY = {21, 11, 6, 2, 1};

    private final static int GPS_LEVEL_0_SATELLITE_COUNT = 0;
    private final static int GPS_LEVEL_1_SATELLITE_COUNT = 4;
    private final static int GPS_LEVEL_2_SATELLITE_COUNT = 6;
    private final static int GPS_LEVEL_3_SATELLITE_COUNT = 8;
    private final static int GPS_LEVEL_4_SATELLITE_COUNT = 10;
    private final static int GPS_LEVEL_5_SATELLITE_COUNT = 12;

    private final static int MAX_LEVEL_RF_STATUS = 6;
    private final static int MAX_RF_VALUE = 100;
    private final static int MIN_RF_VALUE = 0;

    private ImageView mGpsStatus;
    private ImageView mRfStatusImageView;
    private View mBatteryFrameView;
    private ProgressBar mBatteryProgressBar;
    private TextView mBatteryTextView;
    private TextView mGpsCountTextView;
    private Handler mTranslateHandler;
    private Runnable mTranslateRunnable;
    private CommunicateLightState mCommunicateLightState;

    private int mGpsCurrentLevel;
    private Handler mGpsAlarmHandler;
    private Runnable mGpsAlarmRunnable;
    private long mGpsAlarmTimestamp;
    private StatusAlarmListener mStatusAlarmListener;
    private int mDroneBatteryAlarmLevel;

    public StatusView(Context context) {
        super(context);
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.status_view, null);
        mGpsStatus = (ImageView) view.findViewById(R.id.iv_gps);
        mGpsCountTextView = (TextView) view.findViewById(R.id.tv_gps);
        mRfStatusImageView = (ImageView) view.findViewById(R.id.iv_rf);
        mBatteryFrameView = view.findViewById(R.id.progress_battery_frame);
        mBatteryProgressBar = (ProgressBar) view.findViewById(R.id.progress_battery);
        mBatteryTextView = (TextView) view.findViewById(R.id.tv_battery);
        mCommunicateLightState = new CommunicateLightState((ImageView) view.findViewById(R.id.iv_communication_light));

        mGpsAlarmHandler = new Handler();
        mGpsCurrentLevel = 0;
        mGpsAlarmTimestamp = System.currentTimeMillis();
        mDroneBatteryAlarmLevel = 4;

        addView(view);
    }

    public void setRFStatus(int rssi) {
        int rfLevel = Utils.calculateLevel(MAX_RF_VALUE, MIN_RF_VALUE, rssi, MAX_LEVEL_RF_STATUS);
        mRfStatusImageView.setImageLevel(rfLevel);

        if (rfLevel == 0)
            if (mStatusAlarmListener != null) {
                mStatusAlarmListener.onRemoteControllerDisconnect();
            }
    }

    public void setGpsStatus(int satellites) {
        final int gpsNewLevel = calculateGpsLevel(satellites);

        mGpsStatus.setImageLevel(gpsNewLevel);
        if (gpsNewLevel > 0) {
            mGpsCountTextView.setText(satellites + "");
            mGpsCountTextView.setVisibility(View.VISIBLE);
        } else {
            mGpsCountTextView.setVisibility(View.GONE);
        }


        if (mGpsAlarmRunnable != null) {
            return;
        }
        mGpsAlarmRunnable = new Runnable() {
            @Override
            public void run() {
                if (mGpsCurrentLevel == gpsNewLevel) {
                    mGpsAlarmTimestamp = System.currentTimeMillis();
                    return;
                }

                long gap = System.currentTimeMillis() - mGpsAlarmTimestamp;
                if (gap > GPS_UPDATE_GAP_NO_SIGNAL) {
                    if (gpsNewLevel > 0) {
                        if (mStatusAlarmListener != null) {
                            mStatusAlarmListener.onGpsSignalRecover();
                        }
                    } else {
                        if (mStatusAlarmListener != null) {
                            mStatusAlarmListener.onGpsNoSignalAlarm();
                        }
                    }
                    mGpsCurrentLevel = gpsNewLevel;
                    mGpsAlarmTimestamp = System.currentTimeMillis();
                }
                mGpsAlarmHandler.postDelayed(this, GPS_UPDATE_PERIOD);
            }
        };
        mGpsAlarmHandler.postDelayed(mGpsAlarmRunnable, GPS_UPDATE_PERIOD);
    }

    public void setBatteryRemainingPercentage(final int batteryRemainingPercentage) {
        int progress = batteryRemainingPercentage < 0 ? 0 : batteryRemainingPercentage;
        mBatteryTextView.setText(progress + "%");
        mBatteryProgressBar.setProgress(progress);

        if (mDroneBatteryAlarmLevel > 3) {
            initialDroneBatteryAlarmLevel(progress);
        }

        if (progress < DRONE_BATTERY_LOW_THRESHOLD_ARRAY[mDroneBatteryAlarmLevel]) {
            if (mStatusAlarmListener != null) {
                mStatusAlarmListener.onBatteryLowAlarm(progress);
            }
            if (progress < 10 && batteryRemainingPercentage >= 0) {
                setBatteryViewWarning();
            }
            mDroneBatteryAlarmLevel++;
        }
    }

    private void setBatteryViewWarning() {
        mBatteryFrameView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.status_bar_battery_progress_frame_low_battery_drawable, null));
        mBatteryFrameView.startAnimation(AnimationUtils.loadAnimation(mBatteryFrameView.getContext(), R.anim.fade_communicate_light));
    }

    private void initialDroneBatteryAlarmLevel(int initValue) {
        if (initValue > DRONE_BATTERY_LOW_THRESHOLD_ARRAY[0]) {
            mDroneBatteryAlarmLevel = 0;
        } else if (initValue > DRONE_BATTERY_LOW_THRESHOLD_ARRAY[1]) {
            mDroneBatteryAlarmLevel = 1;
        } else if (initValue > DRONE_BATTERY_LOW_THRESHOLD_ARRAY[2]) {
            mDroneBatteryAlarmLevel = 2;
        } else if (initValue > DRONE_BATTERY_LOW_THRESHOLD_ARRAY[3]) {
            mDroneBatteryAlarmLevel = 3;
        } else {
            mDroneBatteryAlarmLevel = 4;
        }
    }

    public void updateCommunicateLight(long heartbeatTimeStamp) {
        mCommunicateLightState.flash(heartbeatTimeStamp);
    }

    public void onDisconnect() {

        if (mGpsAlarmRunnable != null) {
            mGpsAlarmHandler.removeCallbacks(mGpsAlarmRunnable);
            mGpsAlarmRunnable = null;
        }

        mCommunicateLightState.onDisconnect();
        setBatteryRemainingPercentage(-1);
        setGpsStatus(-1);
        setRFStatus(0);
    }

    private int calculateGpsLevel(int satellites) {

        if (satellites < GPS_LEVEL_1_SATELLITE_COUNT) {
            return 0;
        }
        if (satellites < GPS_LEVEL_2_SATELLITE_COUNT) {
            return 1;
        }
        if (satellites < GPS_LEVEL_3_SATELLITE_COUNT) {
            return 2;
        }
        if (satellites < GPS_LEVEL_4_SATELLITE_COUNT) {
            return 3;
        }
        if (satellites < GPS_LEVEL_5_SATELLITE_COUNT) {
            return 4;
        }
        return 5;
    }

    private class CommunicateLightState {

        private final static int LEVEL_NO_CONNECT = 0;
        private final static int LEVEL_WEAK = 1;
        private final static int LEVEL_NORMAL = 2;
        private final static int UPDATE_GAP_NO_SIGNAL = 5;
        private final static int UPDATE_GAP_WEAK = 3;
        private final static int UPDATE_PERIOD = 1 * 1000;

        private ImageView mCommunicateLightImageView;
        private long mLastUpdateTime;
        private int mCurrentLevel = LEVEL_NO_CONNECT;

        public CommunicateLightState(ImageView communicateLightImageView) {
            mCommunicateLightImageView = communicateLightImageView;
            mLastUpdateTime = Long.MAX_VALUE;
            mTranslateHandler = new Handler();
        }

        private void onDisconnect() {
            if (mTranslateRunnable != null) {
                mTranslateHandler.removeCallbacks(mTranslateRunnable);
                mTranslateRunnable = null;
                mCurrentLevel = LEVEL_NO_CONNECT;
            }
            mCommunicateLightImageView.clearAnimation();
            mCommunicateLightImageView.setImageLevel(LEVEL_NO_CONNECT);
        }

        private void update() {
            long gap = (System.currentTimeMillis() / 1000) - mLastUpdateTime;
            int newLevel = gap > UPDATE_GAP_NO_SIGNAL ? LEVEL_NO_CONNECT : gap > UPDATE_GAP_WEAK ? LEVEL_WEAK : LEVEL_NORMAL;
            if (newLevel != mCurrentLevel) {
                mCommunicateLightImageView.setImageLevel(newLevel);
                mCurrentLevel = newLevel;


                if (newLevel == LEVEL_NO_CONNECT) {
                    if (mStatusAlarmListener != null) {
                        mStatusAlarmListener.onDroneDisconnect();
                    }
                }
            }
        }

        private void flash(long heartbeatTimeStamp) {
            mLastUpdateTime = heartbeatTimeStamp;
            update();
            if (mTranslateRunnable != null) {
                return;
            }
            mTranslateRunnable = new Runnable() {
                @Override
                public void run() {
                    update();
                    mTranslateHandler.postDelayed(this, UPDATE_PERIOD);
                }
            };
            mTranslateHandler.postDelayed(mTranslateRunnable, UPDATE_PERIOD);
            mCommunicateLightImageView.startAnimation(AnimationUtils.loadAnimation(mCommunicateLightImageView.getContext(), R.anim.fade_communicate_light));
        }

    }

    public interface StatusAlarmListener {
        void onGpsNoSignalAlarm();

        void onGpsSignalRecover();

        void onBatteryLowAlarm(int batteryRemaining);

        void onRemoteControllerDisconnect();

        void onDroneDisconnect();
    }

    public void setStatusAlarmListener(final StatusAlarmListener listener) {
        mStatusAlarmListener = listener;
    }
}
