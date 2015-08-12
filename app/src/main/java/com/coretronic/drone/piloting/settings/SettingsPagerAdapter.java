package com.coretronic.drone.piloting.settings;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by jiaLian on 15/7/14.
 */
public class SettingsPagerAdapter extends FragmentPagerAdapter {
    private static final int PAGE_COUNT = 4;
    private static final int STATUS_PAGE = 3;

    private String[] titles = {
            "PERSONAL SETTINGS",
            "FLIGHT SETTINGS",
            "PILOTING MODE",
            "STATUS"
    };

    public SettingsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new PersonalSettingsFragment();
                break;
            case 1:
                fragment = new FlightSettingsFragment();
                break;
            case 2:
                fragment = new PilotingSettingsFragment();
                break;
            case 3:
                fragment = new StatusSettingsFragment();
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
