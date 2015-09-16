package com.coretronic.drone.missionplan.fragments;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
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
public class FollowMeFragment extends MavInfoFragment implements DroneController.FollowMeStateListener {
    private static final int DEFAULT_ALTITUDE = 8;
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 20;

    private AbstractWheel altitudeWheel = null;
    private RelativeLayout startFollowMe = null;
    private Button mStopFollowMeButton = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMapViewFragment = (MapViewFragment) getParentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follow_me_dialog, container, false);
        initMavInfoView(view, R.id.altitude_text, R.id.speed_text, R.id.location_lat_text, R.id.location_lng_text, R.id.flight_time_text);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // FollowMe Control Panel
        startFollowMe = (RelativeLayout) view.findViewById(R.id.rl_start_follow);
        startFollowMe.setOnClickListener(onFollowBtnClickListener);

        mStopFollowMeButton = (Button) view.findViewById(R.id.btn_stop_follow);
        mStopFollowMeButton.setOnClickListener(onFollowBtnClickListener);
        mStopFollowMeButton.setVisibility(View.GONE);

        altitudeWheel = (AbstractWheel) view.findViewById(R.id.follow_me_altitude_wheel);
        final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(getActivity().getBaseContext(), MIN_VALUE, MAX_VALUE, "%01d");
        altitudeAdapter.setItemResource(R.layout.text_wheel_number);
        altitudeWheel.setViewAdapter(altitudeAdapter);
        altitudeWheel.setCyclic(false);
        altitudeWheel.setCurrentItem(DEFAULT_ALTITUDE - MIN_VALUE);
        altitudeWheel.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(AbstractWheel wheel) {

            }

            @Override
            public void onScrollingFinished(AbstractWheel wheel) {
                if (startFollowMe.getVisibility() == View.GONE) {
                    ((MainActivity) getActivity()).getDroneController().startFollowMe(altitudeWheel.getCurrentItem() + MIN_VALUE, FollowMeFragment.this);
                }
            }
        });

        // Location Button Panel
        view.findViewById(R.id.my_location_button).setOnClickListener(onFollowBtnClickListener);

        view.findViewById(R.id.drone_location_button).setOnClickListener(onFollowBtnClickListener);

        view.findViewById(R.id.fit_map_button).setOnClickListener(onFollowBtnClickListener);
    }

    private View.OnClickListener onFollowBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final DroneController drone = ((MainActivity) getActivity()).getDroneController();
            switch (v.getId()) {
                case R.id.rl_start_follow:
                    if (drone != null) {
                        drone.startFollowMe(altitudeWheel.getCurrentItem() + MIN_VALUE, FollowMeFragment.this);
                    }
                    startFollowMe.setVisibility(View.GONE);
                    mStopFollowMeButton.setVisibility(View.VISIBLE);
                    break;
                case R.id.btn_stop_follow:
                    if (drone != null) {
                        drone.startFollowMe(0, FollowMeFragment.this);
                    }
                    startFollowMe.setVisibility(View.VISIBLE);
                    mStopFollowMeButton.setVisibility(View.GONE);
                    break;
                case R.id.my_location_button:
                    mMapViewFragment.setMapToMyLocation();
                    break;
                case R.id.drone_location_button:
                    mMapViewFragment.setMapToDrone();
                    break;
                case R.id.fit_map_button:
                    mMapViewFragment.fitMapShowDroneAndMe();
                    break;
            }
        }
    };

    // Implement DroneController.FollowMeStateListener
    @Override
    public void onStart(float latOffset, float longOffset) {
        Toast.makeText(getActivity(), "Start Follow Me", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMissionItemUpdated(float lat, float lon, float yaw, int clock) {

    }

    @Override
    public void onLocationUpdated(float lat, float lon) {

    }

    @Override
    public void onLocationUpdated(Location location) {

    }
    // End DroneController.FollowMeStateListener
}
