package com.coretronic.drone.piloting.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.ui.PageIndicator;

/**
 * Created by jiaLian on 15/4/1.
 */
public class SettingViewPagerFragment extends UnBindDrawablesFragment implements ViewPager.OnPageChangeListener {
    private TextView tvTitle;
    private PageIndicator pageIndicator;
    private LinearLayout defaultSetting;
    private SettingsPagerAdapter settingsPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings_view_pager, container, false);

        settingsPagerAdapter = new SettingsPagerAdapter(getChildFragmentManager());
        ViewPager viewPager = (ViewPager) fragmentView.findViewById(R.id.pager);
        viewPager.addOnPageChangeListener(this);
        viewPager.setAdapter(settingsPagerAdapter);

        pageIndicator = (PageIndicator) fragmentView.findViewById(R.id.page_indicator);
        pageIndicator.setPageCount(settingsPagerAdapter.getCount());

        Button btnBack = (Button) fragmentView.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        tvTitle = (TextView) fragmentView.findViewById(R.id.tv_title);
        tvTitle.setText(settingsPagerAdapter.getTitle(viewPager.getCurrentItem()));

        defaultSetting = (LinearLayout) fragmentView.findViewById(R.id.default_set);
        defaultSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Confirm")
                        .setMessage("Are you sure restore default settings?")
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

        return fragmentView;
    }

    @Override
    public void onPause() {
        super.onPause();
//        ((MainActivity) getActivity()).saveSettingsValue();
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        tvTitle.setText(settingsPagerAdapter.getTitle(i));

        pageIndicator.setCurrentItem(i);
        if (i == settingsPagerAdapter.getStatusPage()) {
            defaultSetting.setVisibility(View.GONE);
        } else {
            defaultSetting.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}