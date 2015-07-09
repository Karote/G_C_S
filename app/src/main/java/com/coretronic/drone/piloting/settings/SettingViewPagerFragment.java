package com.coretronic.drone.piloting.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.piloting.PilotingFragment;
import com.coretronic.drone.piloting.Setting;
import com.coretronic.drone.ui.JoyStickSurfaceView;
import com.coretronic.drone.ui.PageIndicator;

/**
 * Created by jiaLian on 15/4/1.
 */
public class SettingViewPagerFragment extends UnBindDrawablesFragment implements ViewPager.OnPageChangeListener {

    private static final int PAGE_COUNT = 4;
    public static final int STATUS_PAGE = 3;

    private static String[] titleString = {
            "PERSONAL SETTINGS",
            "FLIGHT SETTINGS",
            "PILOTING MODE",
            "STATUS"
    };

    private TextView tvTitle;
    private PageIndicator pageIndicator;
    private LinearLayout defaultSetting;

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
        ViewPager mViewPager = (ViewPager) fragmentView.findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setAdapter(pagerAdapter);

        pageIndicator = (PageIndicator) fragmentView.findViewById(R.id.page_indicator);
        pageIndicator.setPageCount(PAGE_COUNT);

        Button btnBack = (Button) fragmentView.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        tvTitle = (TextView) fragmentView.findViewById(R.id.tv_title);
        tvTitle.setText(titleString[mViewPager.getCurrentItem()]);

        defaultSetting = (LinearLayout) fragmentView.findViewById(R.id.default_set);

        defaultSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Confirm")
                        .setMessage("Are you sure restore default Settings?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((MainActivity) getActivity()).resetSettings();
                                getFragmentManager().popBackStack();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        PilotingFragment.markView.setBackgroundColor(Color.BLACK);
        PilotingFragment.markView.setAlpha(0.85f);

        for (JoyStickSurfaceView joyStickSurfaceView : PilotingFragment.joyStickSurfaceViews) {
            joyStickSurfaceView.setVisibility(View.INVISIBLE);
        }
        return fragmentView;
    }

       @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) getActivity()).saveSettingsValue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PilotingFragment.markView.setBackgroundColor(Color.TRANSPARENT);
        PilotingFragment.markView.setAlpha(1);

        for (JoyStickSurfaceView joyStickSurfaceView : PilotingFragment.joyStickSurfaceViews) {
            joyStickSurfaceView.setPaintPressedAlpha(((MainActivity) getActivity()).getSettingValue(Setting.SettingType.INTERFACE_OPACITY) / 100f);
            joyStickSurfaceView.setVisibility(View.VISIBLE);
        }

        PilotingFragment.initialJoypadMode((MainActivity) getActivity());
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        tvTitle.setText(titleString[i]);
        pageIndicator.setCurrentItem(i);
        if (i == STATUS_PAGE) {
            defaultSetting.setVisibility(View.GONE);
        } else {
            defaultSetting.setVisibility(View.VISIBLE);
        }
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