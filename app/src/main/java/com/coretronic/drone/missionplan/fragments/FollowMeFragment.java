package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelScrollListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class FollowMeFragment extends MapChildFragment implements DroneController.FollowMeStateListener {
    private static final int DEFAULT_ALTITUDE = 8;
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 20;

    private AbstractWheel mAltitudeWheel = null;
    private View mStartFollowMePanel = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMapViewFragment = (MapViewFragment) getParentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_follow_me_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAltitudeWheel = (AbstractWheel) view.findViewById(R.id.follow_me_altitude_wheel);
        final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(getActivity().getBaseContext(), MIN_VALUE, MAX_VALUE, "%01d");
        altitudeAdapter.setItemResource(R.layout.text_wheel_number);
        mAltitudeWheel.setViewAdapter(altitudeAdapter);
        mAltitudeWheel.setCyclic(false);
        mAltitudeWheel.setCurrentItem(DEFAULT_ALTITUDE - MIN_VALUE);
        mAltitudeWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {

            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                if (mStartFollowMePanel.getVisibility() == View.GONE) {
                    ((MainActivity) getActivity()).getDroneController().startFollowMe(mAltitudeWheel.getCurrentItem() + MIN_VALUE, FollowMeFragment.this);
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fit_map_button:
                mMapViewFragment.fitMapShowDroneAndMe();
                break;
        }
    }

    @Override
    public void onStart(float latOffset, float longOffset) {
        Toast.makeText(getActivity(), "Start Follow Me", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDroneTargetUpdated(float lat, float lon, float yaw, int clock) {

    }
}
