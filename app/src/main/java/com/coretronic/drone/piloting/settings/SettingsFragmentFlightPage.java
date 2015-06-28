package com.coretronic.drone.piloting.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.piloting.Setting;
import com.coretronic.drone.service.Parameter;
import com.coretronic.drone.ui.SeekBarTextView;
import com.coretronic.drone.ui.ViewManager;

/**
 * Created by jiaLian on 15/4/1.
 */
public class SettingsFragmentFlightPage extends UnBindDrawablesFragment {

    private MainActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings_flight_page, container, false);
        SeekBarTextView.assignSettingSeekBarTextView(activity,fragmentView, R.id.setting_bar_rotation_max, Setting.SettingType.ROTATION_SPEED_MAX, Parameter.Type.ROTATION_SPEED_MAX);
        SeekBarTextView.assignSettingSeekBarTextView(activity,fragmentView, R.id.setting_bar_tilt_angle_max, Setting.SettingType.TILT_ANGLE_MAX, Parameter.Type.ANGLE_MAX);
        SeekBarTextView.assignSettingSeekBarTextView(activity,fragmentView, R.id.setting_bar_vertical_speed_max, Setting.SettingType.VERTICAL_SPEED_MAX, Parameter.Type.VERTICAL_SPEED_MAX);
        SeekBarTextView.assignSettingSeekBarTextView(activity,fragmentView, R.id.setting_bar_altitude_limit, Setting.SettingType.ALTITUDE_LIMIT, Parameter.Type.ALTITUDE_LIMIT);

        return fragmentView;
    }
}
