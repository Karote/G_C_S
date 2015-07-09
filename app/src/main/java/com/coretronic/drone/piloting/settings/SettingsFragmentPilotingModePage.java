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
import com.coretronic.drone.ui.ViewManager;

/**
 * Created by jiaLian on 15/4/1.
 */
public class SettingsFragmentPilotingModePage extends UnBindDrawablesFragment {
    private static final String TAG = SettingsFragmentPilotingModePage.class.getSimpleName();
    private MainActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings_piloting_mode_page, container, false);
        ViewManager.assignSwitchView(activity, fragmentView, R.id.switch_headless, Setting.SettingType.ABSOLUTE_CONTROL);
        ViewManager.assignSwitchView(activity, fragmentView, R.id.switch_left_handed, Setting.SettingType.LEFT_HANDED);

        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_phone_tilt_max, Setting.SettingType.PHONE_TILT);

        ViewManager.assignSingleSelectionButton(activity, fragmentView, Setting.SettingType.JOYPAD_MODE,
                new int[]{
                        R.id.btn_japan,
                        R.id.btn_usa,
                        R.id.btn_kinesics},
                new int[]{
                        Setting.JOYPAD_MODE_JAPAN,
                        Setting.JOYPAD_MODE_USA,
                        Setting.JOYPAD_MODE_KINESICS});

        return fragmentView;
    }
}
