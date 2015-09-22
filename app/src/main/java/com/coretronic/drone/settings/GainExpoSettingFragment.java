package com.coretronic.drone.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.util.ViewManager;

/**
 * Created by Morris on 15/8/14.
 */
public class GainExpoSettingFragment extends UnBindDrawablesFragment {
    private static final String TAG = GainExpoSettingFragment.class.getSimpleName();
    private MainActivity mMainActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMainActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_gain_expo_page, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewManager.assignSettingSeekBarTextView(mMainActivity, view, R.id.setting_bar_basic_aileron_gain, Setting.SettingType.BASIC_AILERON_GAIN);
        ViewManager.assignSettingSeekBarTextView(mMainActivity, view, R.id.setting_bar_basic_elevator_gain, Setting.SettingType.BASIC_ELEVATOR_GAIN);
        ViewManager.assignSettingSeekBarTextView(mMainActivity, view, R.id.setting_bar_basic_rudder_gain, Setting.SettingType.BASIC_RUDDER_GAIN);
        ViewManager.assignSettingSeekBarTextView(mMainActivity, view, R.id.setting_bar_attitude_aileron_gain, Setting.SettingType.ATTITUDE_AILERON_GAIN);
        ViewManager.assignSettingSeekBarTextView(mMainActivity, view, R.id.setting_bar_attitude_elevator_gain, Setting.SettingType.ATTITUDE_ELEVATOR_GAIN);
        ViewManager.assignSettingSeekBarTextView(mMainActivity, view, R.id.setting_bar_attitude_rudder_gain, Setting.SettingType.ATTITUDE_RUDDER_GAIN);
        ViewManager.assignSettingSeekBarTextView(mMainActivity, view, R.id.setting_bar_attitude_gain, Setting.SettingType.ATTITUDE_GAIN);

    }
}
