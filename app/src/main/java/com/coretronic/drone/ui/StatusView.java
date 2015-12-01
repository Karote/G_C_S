package com.coretronic.drone.ui;

import android.content.Context;
import android.os.Handler;
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
    private ProgressBar mBatteryProgressBar;
    private TextView mBatteryTextView;
    private TextView mGpsCountTextView;
    private Handler mTranslateHandler;
    private Runnable mTranslateRunnable;
    private CommunicateLightState mCommunicateLightState;

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
        mBatteryProgressBar = (ProgressBar) view.findViewById(R.id.progress_battery);
        mBatteryTextView = (TextView) view.findViewById(R.id.tv_battery);
        mCommunicateLightState = new CommunicateLightState((ImageView) view.findViewById(R.id.iv_communication_light));
        addView(view);
    }

    public void setRFStatus(int rssi) {
        mRfStatusImageView.setImageLevel(Utils.calculateLevel(MAX_RF_VALUE, MIN_RF_VALUE, rssi, MAX_LEVEL_RF_STATUS));
    }

    public void setGpsStatus(int satellites) {
        mGpsStatus.setImageLevel(calculateGpsLevel(satellites));
        if (calculateGpsLevel(satellites) > 0) {
            mGpsCountTextView.setText(satellites + "");
            mGpsCountTextView.setVisibility(View.VISIBLE);
        } else {
            mGpsCountTextView.setVisibility(View.GONE);
        }
    }

    public void setBatteryStatus(final int progress) {
        mBatteryTextView.setText(progress + "%");
        mBatteryProgressBar.setProgress(progress);
    }

    public void updateCommunicateLight() {
        mCommunicateLightState.flash();
    }

    public void onDisconnect() {
        mCommunicateLightState.onDisconnect();
        setBatteryStatus(0);
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

        private final static int UPDATE_GAP_NO_SIGNAL = 5 * 1000;
        private final static int UPDATE_GAP_WEAK = 3 * 1000;
        private final static int UPDATE_PERIOD = 1 * 1000;

        private ImageView mCommunicateLightImageView;
        private long mLastUpdateTime;
        private int mCurrentLevel = LEVEL_NO_CONNECT;

        public CommunicateLightState(ImageView communicateLightImageView) {
            mCommunicateLightImageView = communicateLightImageView;
            mLastUpdateTime = System.currentTimeMillis();
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
            long gap = System.currentTimeMillis() - mLastUpdateTime;
            int newLevel = gap > UPDATE_GAP_NO_SIGNAL ? LEVEL_NO_CONNECT : gap > UPDATE_GAP_WEAK ? LEVEL_WEAK : LEVEL_NORMAL;
            if (newLevel != mCurrentLevel) {
                mCommunicateLightImageView.setImageLevel(newLevel);
                mCurrentLevel = newLevel;
            }
        }

        private void flash() {
            mLastUpdateTime = System.currentTimeMillis();
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
}
