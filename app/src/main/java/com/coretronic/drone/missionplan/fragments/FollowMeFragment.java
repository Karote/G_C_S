package com.coretronic.drone.missionplan.fragments;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.DroneController;
import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelChangedListener;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelScrollListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class FollowMeFragment extends MavInfoFragment implements DroneController.FollowMeStateListener {
    private static final String TAG = FollowMeFragment.class.getSimpleName();

    private static final int DEFAULT_ALTITUDE = 8;
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 20;

    private TextView tvAltitude = null;
    private TextView tvSpeed = null;
    private TextView tvLatLng = null;

    OnFollowMeClickListener mCallback;

    private AbstractWheel altitudeWheel = null;
    private NumericWheelAdapter altitudeAdapter = null;
    private RelativeLayout startFollowMe = null;
    private Button stopFollowMe = null;

    public interface OnFollowMeClickListener extends PlanningFragment.MissionAdapterListener {
        void fitMapShowDroneAndMe();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCallback = (OnFollowMeClickListener) getActivity().getSupportFragmentManager().findFragmentByTag("fragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mission_plan_follow_me, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // FollowMe Control Panel
        startFollowMe = (RelativeLayout) view.findViewById(R.id.rl_start_follow);
        startFollowMe.setOnClickListener(onFollowBtnClickListener);

        stopFollowMe = (Button) view.findViewById(R.id.btn_stop_follow);
        stopFollowMe.setOnClickListener(onFollowBtnClickListener);
        stopFollowMe.setVisibility(View.GONE);

        altitudeWheel = (AbstractWheel) view.findViewById(R.id.follow_me_altitude_wheel);
        altitudeAdapter = new NumericWheelAdapter(getActivity().getBaseContext(), MIN_VALUE, MAX_VALUE, "%01d");
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
                    Log.d(TAG, "onScrollingFinished: " + (altitudeWheel.getCurrentItem() + MIN_VALUE));
                    ((MainActivity) getActivity()).getDroneController().startFollowMe(altitudeWheel.getCurrentItem() + MIN_VALUE, FollowMeFragment.this);
                }
            }
        });

        // Location Button Panel
        final Button myLocationButton = (Button) view.findViewById(R.id.button_my_location);
        myLocationButton.setOnClickListener(onFollowBtnClickListener);

        final Button droneLocationButton = (Button) view.findViewById(R.id.button_drone_location);
        droneLocationButton.setOnClickListener(onFollowBtnClickListener);

        final Button fitMapButton = (Button) view.findViewById(R.id.button_fit_map);
        fitMapButton.setOnClickListener(onFollowBtnClickListener);

        // MAV Info
        tvAltitude = (TextView) view.findViewById(R.id.altitude_text);
        tvAltitude.setText("0m");
        tvSpeed = (TextView) view.findViewById(R.id.speed_text);
        tvSpeed.setText("0 km/h");
        tvLatLng = (TextView) view.findViewById(R.id.location_text);
        tvLatLng.setText("0.000000, 0.000000");
    }

    @Override
    public void setMavInfoAltitude(float altitude) {
        if(tvAltitude == null)
            return;
        String tx_alt = String.format("%d", (int) altitude);
        tvAltitude.setText(tx_alt + "m");
    }

    @Override
    public void setMavInfoSpeed(float groundSpeed) {
        if(tvSpeed == null)
            return;
        tvSpeed.setText(groundSpeed + " km/h");
    }

    @Override
    public void setMavInfoLocation(double droneLat, double droneLng) {
        if(tvLatLng == null)
            return;
        tvLatLng.setText(String.valueOf(droneLat) + ", " + String.valueOf(droneLng));
    }

    View.OnClickListener onFollowBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final DroneController drone = ((MainActivity) getActivity()).getDroneController();
            if (drone == null) {
                return;
            }
            switch (v.getId()) {
                case R.id.rl_start_follow:
                    drone.startFollowMe(altitudeWheel.getCurrentItem() + MIN_VALUE, FollowMeFragment.this);
                    startFollowMe.setVisibility(View.GONE);
                    stopFollowMe.setVisibility(View.VISIBLE);
                    break;
                case R.id.btn_stop_follow:
                    drone.startFollowMe(0, FollowMeFragment.this);
                    startFollowMe.setVisibility(View.VISIBLE);
                    stopFollowMe.setVisibility(View.GONE);
                    break;
                case R.id.button_my_location:
                    mCallback.setMapToMyLocation();
                    break;
                case R.id.button_drone_location:
                    mCallback.setMapToDrone();
                    break;
                case R.id.button_fit_map:
                    mCallback.fitMapShowDroneAndMe();
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
