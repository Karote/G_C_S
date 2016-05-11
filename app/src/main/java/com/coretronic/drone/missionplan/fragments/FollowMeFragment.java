package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.coretronic.drone.Drone;
import com.coretronic.drone.DroneController;
import com.coretronic.drone.FollowMeUpdater;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.annotation.Callback;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelScrollListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;
import com.coretronic.drone.util.ConstantValue;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class FollowMeFragment extends MapChildFragment implements DroneController.FollowMeStateListener {
    private final static int FOLLOW_ME_RADIUS_MIN = 3;
    private final static int FOLLOW_ME_RADIUS_MAX = 10;
    private final static int FOLLOW_ME_RADIUS_DEFAULT = 5;

    private AbstractWheel mAltitudeWheel;
    private AbstractWheel mRadiusWheel;
    private View mFollowMeDialog;

    private float mDroneLat;
    private float mDroneLon;
    private FollowMeUpdater mFollowMeUpdater;
    private View mStopFollowMeButton;
    private int mTargetAltitude = ConstantValue.ALTITUDE_DEFAULT_VALUE;
    private int mTargetRadius = FOLLOW_ME_RADIUS_DEFAULT;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMapViewFragment = (MapViewFragment) getParentFragment();
        mFollowMeUpdater = new FollowMeUpdater(FollowMeFragment.this, ((MainActivity) getActivity()).getDroneController());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mission_plan_follow_me, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFollowMeDialog = view.findViewById(R.id.layout_follow_me);

        mAltitudeWheel = (AbstractWheel) view.findViewById(R.id.follow_me_altitude_wheel);
        final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(getActivity().getBaseContext(), ConstantValue.ALTITUDE_MIN_VALUE, ConstantValue.ALTITUDE_MAX_VALUE, "%01d");
        altitudeAdapter.setItemResource(R.layout.text_wheel_number);
        mAltitudeWheel.setViewAdapter(altitudeAdapter);
        mAltitudeWheel.setCyclic(false);
        mAltitudeWheel.setCurrentItem(ConstantValue.ALTITUDE_DEFAULT_VALUE - ConstantValue.ALTITUDE_MIN_VALUE);
        mAltitudeWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {

            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                mTargetAltitude = wheel.getCurrentItem() + ConstantValue.ALTITUDE_MIN_VALUE;
            }
        });


        mRadiusWheel = (AbstractWheel) view.findViewById(R.id.follow_me_radius_wheel);
        final NumericWheelAdapter radiusAdapter = new NumericWheelAdapter(getActivity().getBaseContext(), FOLLOW_ME_RADIUS_MIN, FOLLOW_ME_RADIUS_MAX, "%01d");
        radiusAdapter.setItemResource(R.layout.text_wheel_number);
        mRadiusWheel.setViewAdapter(radiusAdapter);
        mRadiusWheel.setCyclic(false);
        mRadiusWheel.setCurrentItem(FOLLOW_ME_RADIUS_DEFAULT - FOLLOW_ME_RADIUS_MIN);
        mRadiusWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {

            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                mTargetRadius = wheel.getCurrentItem() + FOLLOW_ME_RADIUS_MIN;
            }
        });

        view.findViewById(R.id.tx_start_follow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFollowMeUpdater.setTargetAltitude(mTargetAltitude);
                mFollowMeUpdater.setTargetRadius(mTargetRadius);
                mFollowMeUpdater.startFollowMe();
            }
        });

        view.findViewById(R.id.follow_me_land_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).getDroneController().land(mDroneLat, mDroneLon);
            }
        });

        mStopFollowMeButton = view.findViewById(R.id.follow_me_stop_button);
        mStopFollowMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFollowMeUpdater.stopFollowMe();
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFollowMeUpdater.destroy();
    }

    @Override
    public void onStart(float latOffset, float longOffset) {
        Toast.makeText(getActivity(), "Start Follow Me", Toast.LENGTH_LONG).show();
        mFollowMeDialog.setVisibility(View.GONE);
        mStopFollowMeButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDroneTargetUpdated(float lat, float lon, float yaw, int clock) {

    }

    @Override
    public void updateDroneLocation(float droneLat, float droneLon) {
        mDroneLat = droneLat;
        mDroneLon = droneLon;
    }

    public void onStopFollowMe(){
        mStopFollowMeButton.setVisibility(View.GONE);
        mFollowMeDialog.setVisibility(View.VISIBLE);
    }
}
