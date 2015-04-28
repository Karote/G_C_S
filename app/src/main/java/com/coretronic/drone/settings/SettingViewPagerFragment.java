package com.coretronic.drone.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.main.PilotingActivity;
import com.coretronic.drone.ui.JoyStickSurfaceView;

/**
 * Created by jiaLian on 15/4/1.
 */
public class SettingViewPagerFragment extends UnBindDrawablesFragment implements ViewPager.OnPageChangeListener {
    private static final int PAGE_COUNT = 4;
    private static String[] titleString = {
            "Personal Settings",
            "Flight Settings",
            "Piloting Mode",
            "Status"
    };
    private ViewPager mViewPager;
    private TextView tvTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings_view_pager, container, false);
        fragmentView.findViewById(R.id.root_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager());
        mViewPager = (ViewPager) fragmentView.findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setAdapter(pagerAdapter);

        ImageButton btnBack = (ImageButton) fragmentView.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
//                DroneG2Application.isSettingsMode=false;
            }
        });
        tvTitle = (TextView) fragmentView.findViewById(R.id.tv_title);
        tvTitle.setText(titleString[mViewPager.getCurrentItem()]);

        PilotingActivity.markView.setBackgroundColor(Color.BLACK);
        PilotingActivity.markView.setAlpha(0.7f);
        for (JoyStickSurfaceView joyStickSurfaceView : PilotingActivity.joyStickSurfaceViews) {
            joyStickSurfaceView.setVisibility(View.INVISIBLE);
        }
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PilotingActivity.markView.setBackgroundColor(Color.TRANSPARENT);
        PilotingActivity.markView.setAlpha(1);
        for (JoyStickSurfaceView joyStickSurfaceView : PilotingActivity.joyStickSurfaceViews) {
            joyStickSurfaceView.setVisibility(View.VISIBLE);
        }
        PilotingActivity.initialJoypadMode();
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        tvTitle.setText(titleString[i]);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            switch (i) {
                case 0:
                    fragment = new SettingsFragmentPersonalPage();
                    break;
                case 1:
                    fragment = new SettingsFragmentFlightPage();
                    break;
                case 2:
                    fragment = new SettingsFragmentPilotingModePage();
                    break;
                case 3:
                    fragment = new SettingsFragmentStatusPage();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }
}