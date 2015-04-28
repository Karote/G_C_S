package com.coretronic.drone.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;

/**
 * Created by jiaLian on 15/4/1.
 */
public class SettingsFragmentPersonalPage extends UnBindDrawablesFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings_personal_page, container, false);
        return fragmentView;
    }
}
