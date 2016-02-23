package com.coretronic.drone.settings;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;

/**
 * Created by karot.chuang on 2016/2/15.
 */
public class SettingsMaingFragment extends UnBindDrawablesFragment {
    private final static int ANIMATION_DURATION_IN_SECONDS = (int) (0.2 * 1000);
    private final static float ONE_DP_IN_PIXEL = Resources.getSystem().getDisplayMetrics().densityDpi / 160f ;

    private final static int ANIMATION_TRANSLATE_X = 0;
    private final static int ANIMATION_TRANSLATE_X_DELTA = 0;

    private final static int SETTINGS_PAGE_RC_TXRX = 1;
    private final static int SETTINGS_PAGE_GAIN = 2;
    private final static int SETTINGS_PAGE_BATTERY_AND_FAILSAFE = 3;
    private final static int SETTINGS_PAGE_GENERAL = 4;

    private View mFocusBar;
    private TextView mRcTxRxTextView, mGainTextView, mBatteryFailsafeTextView, mGeneralTextView;
    private Fragment mCurrentFragment;
    private int mCurrentPage;

    private int mAnimationDuration = ANIMATION_DURATION_IN_SECONDS;
    private float mFocusBarYpos = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        mCurrentPage = SETTINGS_PAGE_RC_TXRX;
        mCurrentFragment = new RcTxRxSettingFragment();
        changePage(mCurrentPage);
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
    }

    private View.OnClickListener onLabelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int changeTo = mCurrentPage;
            switch (v.getId()) {
                case R.id.rc_txrx:
                    changeTo = SETTINGS_PAGE_RC_TXRX;
                    mCurrentFragment = new RcTxRxSettingFragment();
                    break;
                case R.id.gain:
                    changeTo = SETTINGS_PAGE_GAIN;
                    mCurrentFragment = new GainSettingFragment();
                    break;
                case R.id.battery_and_failsafe:
                    changeTo = SETTINGS_PAGE_BATTERY_AND_FAILSAFE;
                    mCurrentFragment = new BatteryAndFailsafeSettingFragment();
                    break;
                case R.id.general:
                    changeTo = SETTINGS_PAGE_GENERAL;
                    mCurrentFragment = new GeneralSettingFragment();
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

        am.setDuration(mAnimationDuration);
        am.setFillAfter(true);
        mFocusBar.startAnimation(am);
        mFocusBarYpos = y;
    }
}
