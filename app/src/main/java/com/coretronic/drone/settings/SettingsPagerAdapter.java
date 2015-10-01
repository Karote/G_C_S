package com.coretronic.drone.settings;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by jiaLian on 15/7/14.
 */
public class SettingsPagerAdapter extends FragmentPagerAdapter {
    private final static int FLIGHT_SETTING_POSITION = 0;
    private final static int GAIN_EXPO_SETTING_POSITION = 1;

    private static final int PAGE_COUNT = 2;
    private static final int STATUS_PAGE = 2;

    private String[] titles = {
            "Basic Settings",
            "Gain & Expo Settings"
    };

    public SettingsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case FLIGHT_SETTING_POSITION:
                fragment = new FlightSettingsFragment();
                break;
            case GAIN_EXPO_SETTING_POSITION:
                fragment = new GainExpoSettingFragment();
                break;
            default:
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    public String getTitle(int position) {
        return titles[position];
    }

    public int getStatusPage() {
        return STATUS_PAGE;
    }
}
