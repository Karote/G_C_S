package com.coretronic.drone.piloting.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.piloting.Setting;
import com.coretronic.drone.ui.SeekBarTextView;

/**
 * Created by Morris on 15/8/14.
 */
public class GainExpoSettingFragment extends UnBindDrawablesFragment {
    private static final String TAG = GainExpoSettingFragment.class.getSimpleName();
    private MainActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings_gain_expo_page, container, false);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_basic_aileron_gain, Setting.SettingType.BASIC_AILERON_GAIN);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_basic_elevator_gain, Setting.SettingType.BASIC_ELEVATOR_GAIN);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_basic_rudder_gain, Setting.SettingType.BASIC_RUDDER_GAIN);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_attitude_aileron_gain, Setting.SettingType.ATTITUDE_AILERON_GAIN);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_attitude_elevator_gain, Setting.SettingType.ATTITUDE_ELEVATOR_GAIN);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_attitude_rudder_gain, Setting.SettingType.ATTITUDE_RUDDER_GAIN);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_attitude_gain, Setting.SettingType.ATTITUDE_GAIN);

        return fragmentView;
    }
}
