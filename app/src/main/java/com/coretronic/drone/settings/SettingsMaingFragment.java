package com.coretronic.drone.settings;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.ibs.drone.MavlinkLibBridge.DroneParameter;

/**
 * Created by karot.chuang on 2016/2/15.
 */
public class SettingsMaingFragment extends UnBindDrawablesFragment {
    private final static int ANIMATION_DURATION_IN_SECONDS = (int) (0.2 * 1000);
    private final static float ONE_DP_IN_PIXEL = Resources.getSystem().getDisplayMetrics().densityDpi / 160f;

    private final static int ANIMATION_TRANSLATE_X = 0;
    private final static int ANIMATION_TRANSLATE_X_DELTA = 0;

    private final static int SETTINGS_PAGE_RC_TXRX = 1;
    private final static int SETTINGS_PAGE_GAIN = 2;
    private final static int SETTINGS_PAGE_BATTERY_AND_FAILSAFE = 3;
    private final static int SETTINGS_PAGE_GENERAL = 4;

    private View mFocusBar;
    private TextView mRcTxRxTextView;
    private TextView mGainTextView;
    private TextView mBatteryFailsafeTextView;
    private TextView mGeneralTextView;
    private TextView mRCConnectedIndicatorTextView;
    private TextView mDroneConnectedIndicatorTextView;
    private Fragment mCurrentFragment;
    private int mCurrentPage;

    private Handler mHeartbeatHandler;
    private Runnable mHeartbeatRunnable;
    private long mLastHeartbeatTimestamp;

    private float mFocusBarYpos = 0;

    private DroneParameter mDroneParameter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setDroneConnectedIndicator(false);
        mLastHeartbeatTimestamp = Long.MAX_VALUE;
        mHeartbeatHandler = new Handler();
        mHeartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (((System.currentTimeMillis() / 1000) - mLastHeartbeatTimestamp) > 5) {
                    setDroneConnectedIndicator(false);
                }
                mHeartbeatHandler.postDelayed(mHeartbeatRunnable, 1000);
            }
        };
        mHeartbeatHandler.post(mHeartbeatRunnable);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDroneParameter = ((MainActivity) getActivity()).getDroneController().getAllParameters();
        initView(view);
        mRcTxRxTextView.performClick();
        changePage(mCurrentPage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mHeartbeatRunnable != null) {
            mHeartbeatHandler.removeCallbacks(mHeartbeatRunnable);
            mHeartbeatRunnable = null;
        }
    }

    private void initView(View view) {
        mFocusBar = view.findViewById(R.id.focus_bar);

        view.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        mRcTxRxTextView = (TextView) view.findViewById(R.id.rc_txrx);
        mRcTxRxTextView.setOnClickListener(onLabelClickListener);
        mGainTextView = (TextView) view.findViewById(R.id.gain);
        mGainTextView.setOnClickListener(onLabelClickListener);
        mBatteryFailsafeTextView = (TextView) view.findViewById(R.id.battery_and_failsafe);
        mBatteryFailsafeTextView.setOnClickListener(onLabelClickListener);
        mGeneralTextView = (TextView) view.findViewById(R.id.general);
        mGeneralTextView.setOnClickListener(onLabelClickListener);

        mRCConnectedIndicatorTextView = (TextView) view.findViewById(R.id.rc_connected_indicator_text);
        mDroneConnectedIndicatorTextView = (TextView) view.findViewById(R.id.drone_connected_indicator_text);
    }

    private View.OnClickListener onLabelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int changeTo = mCurrentPage;
            switch (v.getId()) {
                case R.id.rc_txrx:
                    changeTo = SETTINGS_PAGE_RC_TXRX;
                    mCurrentFragment = RcTxRxSettingFragment.newInstance(mDroneParameter);
                    break;
                case R.id.gain:
                    changeTo = SETTINGS_PAGE_GAIN;
                    mCurrentFragment = GainSettingFragment.newInstance(mDroneParameter);
                    break;
                case R.id.battery_and_failsafe:
                    changeTo = SETTINGS_PAGE_BATTERY_AND_FAILSAFE;
                    mCurrentFragment = BatteryAndFailsafeSettingFragment.newInstance(mDroneParameter);
                    break;
                case R.id.general:
                    changeTo = SETTINGS_PAGE_GENERAL;
                    mCurrentFragment = GeneralSettingFragment.newInstance(mDroneParameter);
                    break;
            }
            if (mCurrentPage == changeTo) {
                return;
            }
            changePage(changeTo);
        }
    };

    private void changePage(int changeTo) {
        float y = 0;
        mCurrentPage = changeTo;

        mRcTxRxTextView.setTextColor(getResources().getColor(R.color.settings_main_title_text_normal_color));
        mGainTextView.setTextColor(getResources().getColor(R.color.settings_main_title_text_normal_color));
        mBatteryFailsafeTextView.setTextColor(getResources().getColor(R.color.settings_main_title_text_normal_color));
        mGeneralTextView.setTextColor(getResources().getColor(R.color.settings_main_title_text_normal_color));

        switch (mCurrentPage) {
            case SETTINGS_PAGE_RC_TXRX:
                mRcTxRxTextView.setTextColor(getResources().getColor(R.color.white));
                y = mRcTxRxTextView.getTop();
                break;
            case SETTINGS_PAGE_GAIN:
                mGainTextView.setTextColor(getResources().getColor(R.color.white));
                y = mGainTextView.getTop();
                break;
            case SETTINGS_PAGE_BATTERY_AND_FAILSAFE:
                mBatteryFailsafeTextView.setTextColor(getResources().getColor(R.color.white));
                y = mBatteryFailsafeTextView.getTop();
                break;
            case SETTINGS_PAGE_GENERAL:
                mGeneralTextView.setTextColor(getResources().getColor(R.color.white));
                y = mGeneralTextView.getTop();
                break;
        }

        setFocusBar(y - ONE_DP_IN_PIXEL);

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.settings_fragment_container, mCurrentFragment, null).commit();
    }

    private void setFocusBar(float y) {
        Animation am = new TranslateAnimation(ANIMATION_TRANSLATE_X, ANIMATION_TRANSLATE_X_DELTA, mFocusBarYpos, y);

        am.setDuration(ANIMATION_DURATION_IN_SECONDS);
        am.setFillAfter(true);
        mFocusBar.startAnimation(am);
        mFocusBarYpos = y;
    }

    public void setRCConnectedIndicator(boolean isConnected) {
        if (isConnected) {
            mRCConnectedIndicatorTextView.setText(getResources().getString(R.string.rc_connected));
            mRCConnectedIndicatorTextView.setTextColor(getResources().getColor(R.color.white));
            mRCConnectedIndicatorTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_connected, 0);
        } else {
            mRCConnectedIndicatorTextView.setText(getResources().getString(R.string.rc_disconnected));
            mRCConnectedIndicatorTextView.setTextColor(getResources().getColor(R.color.white_transparent_50));
            mRCConnectedIndicatorTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_disconnected, 0);
        }
    }

    public void updateHeartbeatTimeStamp(long heartbeatTimestamp) {
        mLastHeartbeatTimestamp = heartbeatTimestamp;
        setDroneConnectedIndicator(true);
    }

    private void setDroneConnectedIndicator(boolean isConnected) {
        if (isConnected) {
            mDroneConnectedIndicatorTextView.setText(getResources().getString(R.string.drone_connected));
            mDroneConnectedIndicatorTextView.setTextColor(getResources().getColor(R.color.white));
            mDroneConnectedIndicatorTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_connected, 0);
        } else {
            mDroneConnectedIndicatorTextView.setText(getResources().getString(R.string.drone_disconnected));
            mDroneConnectedIndicatorTextView.setTextColor(getResources().getColor(R.color.white_transparent_50));
            mDroneConnectedIndicatorTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_disconnected, 0);
        }
    }
}
