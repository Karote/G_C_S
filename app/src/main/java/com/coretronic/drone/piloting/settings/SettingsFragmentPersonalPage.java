package com.coretronic.drone.piloting.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.piloting.Setting;
import com.coretronic.drone.ui.ViewManager;

/**
 * Created by jiaLian on 15/4/1.
 */
public class SettingsFragmentPersonalPage extends UnBindDrawablesFragment {
    private static final String TAG = SettingsFragmentPersonalPage.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings_personal_page, container, false);
        ViewManager.assignSettingSeekBarTextView(fragmentView, R.id.setting_bar_opacity, Setting.SettingType.INTERFACE_OPACTITY);
        ViewManager.assignSwitchView(fragmentView, R.id.switch_sdcard_enable, Setting.SettingType.SD_RECORD);
        ViewManager.assignSwitchView(fragmentView, R.id.switch_flip_enable, Setting.SettingType.FLIP_ENABLE);

        return fragmentView;
    }
}
