package com.coretronic.drone.piloting.settings;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by jiaLian on 15/7/14.
 */
public class SettingsPagerAdapter extends FragmentPagerAdapter {
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
            case 0:
                fragment = new FlightSettingsFragment();
                break;
            case 1:
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
