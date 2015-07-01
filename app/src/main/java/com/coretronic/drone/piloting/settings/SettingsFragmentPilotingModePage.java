package com.coretronic.drone.piloting.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.coretronic.drone.DroneApplication;
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
public class SettingsFragmentPilotingModePage extends UnBindDrawablesFragment {
    private static final String TAG = SettingsFragmentPilotingModePage.class.getSimpleName();
    private MainActivity activity;
    private int currentJoypadModeValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings_piloting_mode_page, container, false);
        ViewManager.assignSwitchView(activity, fragmentView, R.id.switch_headless, Setting.SettingType.HEADLESS, Parameter.Type.ABSOLUTE_CONTROL);
        ViewManager.assignSwitchView(fragmentView, R.id.switch_left_handed, Setting.SettingType.LEFT_HANDED);
        SeekBarTextView.assignSettingSeekBarTextView(fragmentView, R.id.setting_bar_phone_tilt_max, Setting.SettingType.PHONE_TILT);

        final Button[] buttons = new Button[3];
        buttons[Setting.JOYPAD_MODE_JAPAN] = (Button) fragmentView.findViewById(R.id.btn_japan);
        buttons[Setting.JOYPAD_MODE_JAPAN].setTag(Setting.JOYPAD_MODE_JAPAN);

        buttons[Setting.JOYPAD_MODE_USA] = (Button) fragmentView.findViewById(R.id.btn_usa);
        buttons[Setting.JOYPAD_MODE_USA].setTag(Setting.JOYPAD_MODE_USA);

        buttons[Setting.JOYPAD_MODE_KINESICS] = (Button) fragmentView.findViewById(R.id.btn_kinesics);
        buttons[Setting.JOYPAD_MODE_KINESICS].setTag(Setting.JOYPAD_MODE_KINESICS);

        currentJoypadModeValue = DroneApplication.settings[Setting.SettingType.JOYPAD_MODE.ordinal()].getValue();
        for (Button btn : buttons) {
            refreshBackground(currentJoypadModeValue, btn);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentJoypadModeValue = (int) view.getTag();
                    DroneApplication.settings[Setting.SettingType.JOYPAD_MODE.ordinal()].setValue(currentJoypadModeValue);
                    for (Button btn : buttons) {
                        refreshBackground(currentJoypadModeValue, btn);
                    }
                }
            });
        }
        return fragmentView;
    }

    private void refreshBackground(int currentValue, Button btn) {
        if ((int) btn.getTag() == currentValue) {
            btn.setBackgroundColor(getResources().getColor(R.color.blue_sky));
        } else {
            btn.setBackgroundResource(R.drawable.btn_bg);
        }
    }
}
