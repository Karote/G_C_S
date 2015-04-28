package com.coretronic.drone.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.main.DroneG2Application;
import com.coretronic.drone.ui.ViewManager;

/**
 * Created by jiaLian on 15/4/1.
 */
public class SettingsFragmentPilotingModePage extends UnBindDrawablesFragment {
    private static final String TAG = SettingsFragmentPilotingModePage.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings_piloting_mode_page, container, false);

        ViewManager.assignSwitchView(fragmentView, R.id.switch_joypad, DroneG2Application.SettingType.JOYPAD_MODE);
        ViewManager.assignSwitchView(fragmentView, R.id.switch_left_handed, DroneG2Application.SettingType.LEFT_HANDED);

        return fragmentView;
    }
}
