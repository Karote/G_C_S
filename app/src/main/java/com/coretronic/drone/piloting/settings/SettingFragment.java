package com.coretronic.drone.piloting.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.ui.PageIndicator;

/**
 * Created by jiaLian on 15/4/1.
 */
public class SettingFragment extends UnBindDrawablesFragment implements ViewPager.OnPageChangeListener {

    private TextView mTitleTextView;
    private PageIndicator mPageIndicator;
    private TextView mDefaultSettingTextView;
    private SettingsPagerAdapter mSettingsPagerAdapter;
    private ProgressDialog mResetProgressDialog;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mResetProgressDialog = new ProgressDialog(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_view_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSettingsPagerAdapter = new SettingsPagerAdapter(getChildFragmentManager());
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.addOnPageChangeListener(this);
        viewPager.setAdapter(mSettingsPagerAdapter);

        mPageIndicator = (PageIndicator) view.findViewById(R.id.page_indicator);
        mPageIndicator.setPageCount(mSettingsPagerAdapter.getCount());

        view.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        mTitleTextView = (TextView) view.findViewById(R.id.tv_title);
        mTitleTextView.setText(mSettingsPagerAdapter.getTitle(viewPager.getCurrentItem()));

        mDefaultSettingTextView = (TextView) view.findViewById(R.id.default_set);
        mDefaultSettingTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Confirm")
                        .setMessage("Are you sure restore default settings?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mResetProgressDialog.show();
                                ((MainActivity) getActivity()).resetSettings();
                                mResetProgressDialog.dismiss();
                                refreshFragment();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        mTitleTextView.setText(mSettingsPagerAdapter.getTitle(i));
        mPageIndicator.setCurrentItem(i);
        if (i == mSettingsPagerAdapter.getStatusPage()) {
            mDefaultSettingTextView.setVisibility(View.GONE);
        } else {
            mDefaultSettingTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private void refreshFragment() {
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }
}